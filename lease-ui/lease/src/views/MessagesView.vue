<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getChatHistoryAPI, getBatchOnlineStatusAPI, markAsReadAPI } from '@/api/user' 

const router = useRouter()

const currentUser = ref({ userId: null, username: '未登录', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Me' })
let ws = null 
let statusTimer = null 

// 模拟全站用户字典
const allUsers = [
  { id: 1, name: '农民大叔 (用户1)', role: '农户', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix', lastMessage: '', time: '', unread: 0, isOnline: false },
  { id: 2, name: '农机主老板 (用户2)', role: '农机主', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Toby', lastMessage: '', time: '', unread: 0, isOnline: false }
]

const contacts = ref([])
const activeContact = ref(null)

const searchQuery = ref('')
const chatHistories = ref({}) 
const inputText = ref('')
const chatBoxRef = ref(null)

// ================= 核心 1：拉取历史记录 =================
const fetchChatHistory = async (targetUserId) => {
  try {
    const res = await getChatHistoryAPI(currentUser.value.userId, targetUserId)
    
    chatHistories.value[targetUserId] = res.data.map(msg => {
      const timeStr = new Date(msg.createTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      return {
        id: msg.id,
        senderId: msg.fromUserId,
        text: msg.content,
        time: timeStr,
        isRead: msg.isRead 
      }
    })
    scrollToBottom()
  } catch (error) {
    console.error('拉取历史记录失败:', error)
  }
}

// ================= 核心 2：批量获取在线状态 =================
const fetchOnlineStatus = async () => {
  if (contacts.value.length === 0) return
  try {
    const userIds = contacts.value.map(c => c.id)
    const res = await getBatchOnlineStatusAPI(userIds)
    const statusMap = res.data
    
    contacts.value.forEach(contact => {
      contact.isOnline = statusMap[contact.id] || false
    })
  } catch (error) {
    console.error('获取在线状态失败:', error)
  }
}

// ================= 核心 3：标记消息为已读 (附带回执机制) =================
const markMessagesAsRead = async (targetUserId) => {
  try {
    // 1. HTTP 告诉后端数据库修改状态
    await markAsReadAPI(targetUserId, currentUser.value.userId)
    
    // 2. 清空本地小红点
    const contact = contacts.value.find(c => c.id === targetUserId)
    if (contact) contact.unread = 0

    // 3. 【新增】：通过 WebSocket 悄悄给对方发一条“已读回执信号”
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({
        type: 2, // 2 代表回执信号
        toUserId: targetUserId
      }))
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// ================= WebSocket 逻辑 =================
const initWebSocket = () => {
  const userInfoStr = localStorage.getItem('user_info')
  if (!userInfoStr) {
    alert('请先登录！')
    router.push('/login')
    return
  }
  
  const userInfo = JSON.parse(userInfoStr)
  currentUser.value.userId = userInfo.userId
  currentUser.value.username = userInfo.username
  
  contacts.value = allUsers.filter(u => u.id !== currentUser.value.userId)
  
  if (contacts.value.length > 0) {
    activeContact.value = contacts.value[0]
    fetchChatHistory(activeContact.value.id) 
    markMessagesAsRead(activeContact.value.id) 
  }

  fetchOnlineStatus()
  statusTimer = setInterval(() => {
    fetchOnlineStatus()
  }, 10000)

  const wsUrl = `ws://localhost:9191/ws/chat/${currentUser.value.userId}`
  ws = new WebSocket(wsUrl)

  ws.onmessage = (event) => {
    const msgObj = JSON.parse(event.data)
    
    // 【核心改造】：判断收到的是聊天消息，还是已读回执
    if (msgObj.type === 2) {
      // 收到了已读回执 (代表对方 msgObj.fromUserId 看了我发的消息)
      const readerId = msgObj.fromUserId;
      // 把我发给 readerId 的所有消息在本地强行置为“已读” (绿色)
      if (chatHistories.value[readerId]) {
        chatHistories.value[readerId].forEach(msg => {
          if (msg.senderId === currentUser.value.userId) {
            msg.isRead = 1;
          }
        })
      }
      return; // 回执处理完毕，直接结束
    }

    // ========== 下面是原本处理正常聊天消息 (type = 1) 的逻辑 ==========
    const senderId = msgObj.fromUserId

    if (!chatHistories.value[senderId]) {
      chatHistories.value[senderId] = []
    }
    
    chatHistories.value[senderId].push({
      id: msgObj.id || Date.now(),
      senderId: senderId,
      text: msgObj.content,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      isRead: 0 
    })

    const contact = contacts.value.find(c => c.id === senderId)
    if (contact) {
      contact.lastMessage = msgObj.content
      contact.time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      
      if (activeContact.value?.id === senderId) {
        // 如果正在看这个人聊天，直接静默发已读请求（里面包含了发出回执信号）
        markMessagesAsRead(senderId)
      } else {
        contact.unread += 1
      }
    }
    
    scrollToBottom()
  }
}

onMounted(() => {
  initWebSocket()
})

onUnmounted(() => {
  if (ws) ws.close() 
  if (statusTimer) clearInterval(statusTimer) 
})

// ================= 交互逻辑 =================
const filteredContacts = computed(() => {
  if (!searchQuery.value) return contacts.value
  return contacts.value.filter(c => c.name.includes(searchQuery.value) || c.role.includes(searchQuery.value))
})

const selectContact = async (contact) => {
  activeContact.value = contact
  await fetchChatHistory(contact.id)
  await markMessagesAsRead(contact.id)
}

const sendMessage = () => {
  if (!inputText.value.trim()) return
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    alert('网络未连接，请刷新页面重试')
    return
  }

  const targetUserId = activeContact.value.id
  
  // 发送时指定 type = 1
  const payload = {
    type: 1, 
    toUserId: targetUserId,
    content: inputText.value
  }
  ws.send(JSON.stringify(payload))

  const newMessage = {
    id: Date.now(),
    senderId: currentUser.value.userId,
    text: inputText.value,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    isRead: 0 
  }

  if (!chatHistories.value[targetUserId]) {
    chatHistories.value[targetUserId] = []
  }
  chatHistories.value[targetUserId].push(newMessage)
  
  activeContact.value.lastMessage = inputText.value
  activeContact.value.time = newMessage.time

  inputText.value = ''
  scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatBoxRef.value) {
    chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
  }
}
</script>

<template>
  <div class="messages-page-full">
    <div class="chat-container-full">
      
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="header-top">
            <h2>消息中心 (我是: {{ currentUser.username }})</h2>
          </div>
          <div class="search-bar">
            <input type="text" v-model="searchQuery" placeholder="搜索联系人..." />
          </div>
        </div>
        
        <div class="contact-list">
          <div 
            v-for="contact in filteredContacts" 
            :key="contact.id"
            :class="['contact-item', { active: activeContact && activeContact.id === contact.id }]"
            @click="selectContact(contact)"
          >
            <div class="avatar-box">
              <img :src="contact.avatar" :alt="contact.name" />
              <span v-if="contact.unread > 0" class="badge">{{ contact.unread }}</span>
              <span :class="['avatar-status', { 'offline': !contact.isOnline }]"></span>
            </div>
            <div class="contact-info">
              <div class="contact-top">
                <span class="name">{{ contact.name }}</span>
                <span class="time">{{ contact.time }}</span>
              </div>
              <div class="contact-bottom">
                <span class="role-tag" v-if="contact.role === '农户'">农</span>
                <span class="role-tag blue" v-else>主</span>
                <p class="last-msg">{{ contact.lastMessage || '暂无消息' }}</p>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <main class="chat-main" v-if="activeContact">
        <header class="chat-header">
          <div class="chat-title">
            <h3>{{ activeContact.name }}</h3>
            <span :class="['status-dot', { 'offline': !activeContact.isOnline }]"></span>
            <span class="status-text">{{ activeContact.isOnline ? '在线' : '离线' }}</span>
          </div>
        </header>

        <div class="chat-history" ref="chatBoxRef">
          <div class="date-divider">今天</div>
          
          <div 
            v-for="msg in chatHistories[activeContact.id] || []" 
            :key="msg.id"
            :class="['message-row', msg.senderId === currentUser.userId ? 'mine' : 'theirs']"
          >
            <img v-if="msg.senderId !== currentUser.userId" :src="activeContact.avatar" class="msg-avatar" />
            
            <div class="msg-content">
              <div class="bubble">{{ msg.text }}</div>
              <div class="msg-time">
                {{ msg.time }}
                <span v-if="msg.senderId === currentUser.userId" 
                      :class="['read-status', msg.isRead === 1 ? 'is-read' : 'un-read']">
                  {{ msg.isRead === 1 ? '已读' : '未读' }}
                </span>
              </div>
            </div>
            
            <img v-if="msg.senderId === currentUser.userId" :src="currentUser.avatar" class="msg-avatar" />
          </div>
        </div>

        <div class="chat-input-area">
          <textarea 
            v-model="inputText" 
            placeholder="输入消息..."
            @keyup.enter.prevent="sendMessage"
          ></textarea>
          <div class="input-footer">
            <span class="hint">Enter 发送 / Shift+Enter 换行</span>
            <button class="btn-primary send-btn" @click="sendMessage">发送</button>
          </div>
        </div>
      </main>
      
      <main class="chat-main empty-chat" v-else>
        <div class="empty-content">
          <div class="icon">💬</div>
          <h3>选择一个联系人开始交流</h3>
        </div>
      </main>

    </div>
  </div>
</template>

<style scoped>
/* 离线状态变红/变灰 */
.status-dot.offline { background-color: #ef4444; }
.avatar-status { position: absolute; bottom: 0; right: 0; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white; background-color: #10b981; }
.avatar-status.offline { background-color: #9ca3af; }

/* 已读/未读状态样式 */
.read-status { margin-left: 8px; font-weight: 600; font-size: 0.7rem; }
.is-read { color: #10b981; } /* 绿色已读 */
.un-read { color: #ef4444; } /* 红色未读 */

/* 全量 CSS */
.messages-page-full { height: calc(100vh - 80px); width: 100%; background-color: #ffffff; overflow: hidden; }
.chat-container-full { display: flex; width: 100%; height: 100%; }
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 4px; }
::-webkit-scrollbar-thumb:hover { background: #9ca3af; }
.sidebar { width: 300px; background-color: #fafafa; border-right: 1px solid #e5e7eb; display: flex; flex-direction: column; height: 100%; flex-shrink: 0; }
.sidebar-header { padding: 15px 20px; background-color: #f3f4f6; border-bottom: 1px solid #e5e7eb; }
.header-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.header-top h2 { font-size: 1.1rem; font-weight: 700; color: var(--text-dark); margin: 0; }
.search-bar { position: relative; width: 100%; }
.search-bar input { width: 100%; padding: 8px 10px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 0.9rem; outline: none; background: white; transition: border-color 0.2s; }
.search-bar input:focus { border-color: var(--primary-color); }
.contact-list { flex: 1; overflow-y: auto; }
.contact-item { display: flex; padding: 15px 20px; cursor: pointer; border-bottom: 1px solid #f3f4f6; transition: background-color 0.2s; }
.contact-item:hover { background-color: #f3f4f6; }
.contact-item.active { background-color: #eef2ff; border-left: 4px solid var(--primary-color); padding-left: 16px; }
.avatar-box { position: relative; width: 44px; height: 44px; border-radius: 50%; margin-right: 12px; flex-shrink: 0; }
.avatar-box img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.badge { position: absolute; top: -2px; left: -2px; background-color: #ef4444; color: white; font-size: 0.7rem; font-weight: bold; width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; border-radius: 50%; border: 2px solid white; z-index: 10; }
.contact-info { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; }
.contact-top { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 4px; }
.contact-top .name { font-weight: 600; color: var(--text-dark); font-size: 1rem; }
.contact-top .time { font-size: 0.75rem; color: #9ca3af; }
.contact-bottom { display: flex; align-items: center; gap: 6px; }
.role-tag { font-size: 0.65rem; padding: 2px 4px; border-radius: 4px; color: white; background: #f59e0b; flex-shrink: 0; }
.role-tag.blue { background: var(--primary-color); }
.last-msg { color: #6b7280; font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin: 0; }
.chat-main { flex: 1; display: flex; flex-direction: column; background-color: #f8fafc; height: 100%; }
.chat-header { padding: 15px 25px; background: white; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; height: 65px; flex-shrink: 0; }
.chat-title { display: flex; align-items: center; gap: 8px; }
.chat-title h3 { font-size: 1.1rem; font-weight: 700; color: var(--text-dark); margin: 0; }
.status-dot { width: 8px; height: 8px; background-color: #10b981; border-radius: 50%; transition: 0.3s; }
.status-text { font-size: 0.85rem; color: #10b981; }
.status-text.offline { color: #ef4444; }
.chat-history { flex: 1; padding: 25px; overflow-y: auto; display: flex; flex-direction: column; gap: 20px; }
.date-divider { text-align: center; font-size: 0.8rem; color: #9ca3af; margin: 10px 0; }
.message-row { display: flex; align-items: flex-start; gap: 12px; }
.message-row.mine { justify-content: flex-end; }
.msg-avatar { width: 36px; height: 36px; border-radius: 50%; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.msg-content { max-width: 60%; display: flex; flex-direction: column; }
.mine .msg-content { align-items: flex-end; }
.theirs .msg-content { align-items: flex-start; }
.bubble { padding: 10px 15px; border-radius: 12px; font-size: 0.95rem; line-height: 1.5; box-shadow: 0 1px 2px rgba(0,0,0,0.05); word-break: break-all; }
.mine .bubble { background-color: var(--primary-color); color: white; border-top-right-radius: 2px; }
.theirs .bubble { background-color: white; color: var(--text-dark); border: 1px solid #f1f5f9; border-top-left-radius: 2px; }
.msg-time { font-size: 0.7rem; color: #9ca3af; margin-top: 4px; }
.chat-input-area { background: white; border-top: 1px solid #e5e7eb; padding: 10px 25px 15px; display: flex; flex-direction: column; flex-shrink: 0; height: auto; }
textarea { width: 100%; height: 60px; border: none; resize: none; outline: none; font-size: 0.95rem; font-family: inherit; color: var(--text-dark); background: transparent; }
textarea::placeholder { color: #cbd5e1; }
.input-footer { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 5px; }
.hint { font-size: 0.75rem; color: #9ca3af; }
.send-btn { padding: 8px 25px; border-radius: 6px; font-size: 0.9rem; background: var(--primary-color); color: white; border: none; cursor: pointer; }
.empty-chat { justify-content: center; align-items: center; background: #f8fafc; }
.empty-content { text-align: center; color: #9ca3af; }
.empty-content .icon { font-size: 3rem; margin-bottom: 15px; opacity: 0.5; }
.empty-content h3 { color: var(--text-medium); margin-bottom: 10px; font-size: 1.2rem; }
</style>