<script setup>
import { ref, computed, nextTick } from 'vue'

// 模拟当前登录用户
const currentUser = { id: 'u1', name: '我', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix' }

// 模拟联系人列表 (增加数据量以展示滚动效果)
const contacts = ref([
  { id: 'c1', name: '王师傅', role: '农机主', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Toby', machine: '东方红 LX2204 拖拉机', lastMessage: '明天早上8点可以把车送过去。', time: '10:30', unread: 2 },
  { id: 'c2', name: '李老板', role: '租赁公司', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Max', machine: '大疆 T60 植保无人机', lastMessage: '押金已经收到，谢谢合作！', time: '昨天', unread: 0 },
  { id: 'c3', name: '张村长', role: '农户', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Leo', machine: '久保田 PRO1408 收割机', lastMessage: '这个机型带自动驾驶吗？', time: '星期二', unread: 0 },
  { id: 'c4', name: '赵大爷', role: '农户', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack', machine: '雷沃谷神联合收割机', lastMessage: '收割机刀片有点磨损，能换新的吗？', time: '星期一', unread: 1 },
  { id: 'c5', name: '农机服务中心-小刘', role: '平台客服', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mia', machine: '', lastMessage: '您的实名认证已经通过。', time: '上周', unread: 0 },
  { id: 'c6', name: '陈哥', role: '农机主', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Oscar', machine: '极飞 V50 农业无人车', lastMessage: '好的，按您说的时间来。', time: '上周', unread: 0 },
  { id: 'c7', name: '顺丰农机物流', role: '承运方', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Buster', machine: '', lastMessage: '设备预计今天下午送达指定农田。', time: '上周', unread: 0 },
  { id: 'c8', name: '周大姐', role: '农户', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lola', machine: '约翰迪尔 8R 拖拉机', lastMessage: '租金可以用微信支付吗？', time: '3月12日', unread: 0 }
])

const searchQuery = ref('')

// 计算属性：实现左侧联系人搜索过滤
const filteredContacts = computed(() => {
  if (!searchQuery.value) return contacts.value
  return contacts.value.filter(c => c.name.includes(searchQuery.value) || c.role.includes(searchQuery.value))
})

// 选中的联系人，默认选中第一个
const activeContact = ref(contacts.value[0])

// 模拟聊天记录数据
const chatHistories = ref({
  'c1': [
    { id: 1, senderId: 'u1', text: '王师傅您好，请问东方红拖拉机这周末有空档吗？我想租两天。', time: '10:15' },
    { id: 2, senderId: 'c1', text: '有的，周末刚好闲置。您在哪个村？', time: '10:20' },
    { id: 3, senderId: 'u1', text: '在李家屯南边的麦田，大概50亩地。', time: '10:22' },
    { id: 4, senderId: 'c1', text: '好的，距离不远。明天早上8点可以把车送过去。', time: '10:30' }
  ],
  'c2': [
    { id: 1, senderId: 'u1', text: '无人机我已经按时归还了。', time: '昨天 14:00' },
    { id: 2, senderId: 'c2', text: '好的，设备检查无误。', time: '昨天 14:30' },
    { id: 3, senderId: 'c2', text: '押金已经收到，谢谢合作！', time: '昨天 14:35' }
  ]
})

const inputText = ref('')
const chatBoxRef = ref(null)

// 切换联系人
const selectContact = (contact) => {
  activeContact.value = contact
  contact.unread = 0 
  scrollToBottom()
}

// 发送消息
const sendMessage = () => {
  if (!inputText.value.trim()) return

  const newMessage = {
    id: Date.now(),
    senderId: currentUser.id,
    text: inputText.value,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  if (!chatHistories.value[activeContact.value.id]) {
    chatHistories.value[activeContact.value.id] = []
  }
  chatHistories.value[activeContact.value.id].push(newMessage)
  
  activeContact.value.lastMessage = inputText.value
  activeContact.value.time = newMessage.time

  inputText.value = ''
  scrollToBottom()
}

// 滚动到底部
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
            <h2>消息中心</h2>
            <button class="btn-icon">➕</button>
          </div>
          <div class="search-bar">
            <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
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
            </div>
            <div class="contact-info">
              <div class="contact-top">
                <span class="name">{{ contact.name }}</span>
                <span class="time">{{ contact.time }}</span>
              </div>
              <div class="contact-bottom">
                <span class="role-tag" v-if="contact.role === '农户'">农</span>
                <span class="role-tag blue" v-else-if="contact.role === '农机主'">主</span>
                <span class="role-tag green" v-else>企</span>
                <p class="last-msg">{{ contact.lastMessage }}</p>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <main class="chat-main" v-if="activeContact">
        <header class="chat-header">
          <div class="chat-title">
            <h3>{{ activeContact.name }}</h3>
            <span class="status-dot"></span>
            <span class="status-text">在线</span>
          </div>
          <div class="machine-ref" v-if="activeContact.machine">
            <span class="icon">🚜</span> {{ activeContact.machine }}
            <button class="btn-view">查看订单</button>
          </div>
        </header>

        <div class="chat-history" ref="chatBoxRef">
          <div class="date-divider">今天</div>
          
          <div 
            v-for="msg in chatHistories[activeContact.id] || []" 
            :key="msg.id"
            :class="['message-row', msg.senderId === currentUser.id ? 'mine' : 'theirs']"
          >
            <img v-if="msg.senderId !== currentUser.id" :src="activeContact.avatar" class="msg-avatar" />
            
            <div class="msg-content">
              <div class="bubble">{{ msg.text }}</div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
            
            <img v-if="msg.senderId === currentUser.id" :src="currentUser.avatar" class="msg-avatar" />
          </div>
        </div>

        <div class="chat-input-area">
          <div class="input-toolbar">
            <button class="tool-btn" title="表情">😊</button>
            <button class="tool-btn" title="发送图片">🖼️</button>
            <button class="tool-btn" title="发送文件">📎</button>
            <button class="tool-btn" title="常用语">💬</button>
          </div>
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
/* ================= 全屏布局 ================= */
.messages-page-full {
  /* 高度为屏幕高度减去导航栏80px */
  height: calc(100vh - 80px);
  width: 100%;
  background-color: #ffffff;
  overflow: hidden; /* 防止出现外部滚动条 */
}

.chat-container-full {
  display: flex;
  width: 100%;
  height: 100%;
}

/* ================= 自定义滚动条样式 ================= */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 4px; }
::-webkit-scrollbar-thumb:hover { background: #9ca3af; }

/* ================= 左侧侧边栏 ================= */
.sidebar {
  width: 300px; /* 固定宽度 */
  background-color: #fafafa;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  height: 100%;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 15px 20px;
  background-color: #f3f4f6;
  border-bottom: 1px solid #e5e7eb;
}
.header-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.header-top h2 { font-size: 1.2rem; font-weight: 700; color: var(--text-dark); margin: 0; }
.btn-icon { background: transparent; border: none; cursor: pointer; font-size: 1.1rem; filter: grayscale(1); }
.btn-icon:hover { filter: grayscale(0); }

.search-bar { position: relative; width: 100%; }
.search-bar .search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); width: 16px; height: 16px; color: #9ca3af; }
.search-bar input { width: 100%; padding: 8px 10px 8px 32px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 0.9rem; outline: none; background: white; transition: border-color 0.2s; }
.search-bar input:focus { border-color: var(--primary-color); }

.contact-list {
  flex: 1; /* 占据剩余所有高度 */
  overflow-y: auto; /* 核心：超出范围自动滚动 */
}

.contact-item {
  display: flex;
  padding: 15px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f3f4f6;
  transition: background-color 0.2s;
}
.contact-item:hover { background-color: #f3f4f6; }
.contact-item.active { background-color: #eef2ff; border-left: 4px solid var(--primary-color); padding-left: 16px; }

.avatar-box { position: relative; width: 44px; height: 44px; border-radius: 50%; margin-right: 12px; flex-shrink: 0; }
.avatar-box img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.badge { position: absolute; top: -2px; right: -2px; background-color: #ef4444; color: white; font-size: 0.7rem; font-weight: bold; width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; border-radius: 50%; border: 2px solid white; }

.contact-info { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; }
.contact-top { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 4px; }
.contact-top .name { font-weight: 600; color: var(--text-dark); font-size: 1rem; }
.contact-top .time { font-size: 0.75rem; color: #9ca3af; }
.contact-bottom { display: flex; align-items: center; gap: 6px; }

.role-tag { font-size: 0.65rem; padding: 2px 4px; border-radius: 4px; color: white; background: #f59e0b; flex-shrink: 0; }
.role-tag.blue { background: var(--primary-color); }
.role-tag.green { background: #10b981; }

.last-msg { color: #6b7280; font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin: 0; }

/* ================= 右侧主聊天区 ================= */
.chat-main {
  flex: 1; /* 占据剩余所有宽度 */
  display: flex;
  flex-direction: column;
  background-color: #f8fafc; /* 非常浅的蓝灰色背景 */
  height: 100%;
}

.chat-header {
  padding: 15px 25px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 65px; /* 固定头部高度 */
  flex-shrink: 0;
}
.chat-title { display: flex; align-items: center; gap: 8px; }
.chat-title h3 { font-size: 1.1rem; font-weight: 700; color: var(--text-dark); margin: 0; }
.status-dot { width: 8px; height: 8px; background-color: #10b981; border-radius: 50%; }
.status-text { font-size: 0.85rem; color: #10b981; }

.machine-ref { display: flex; align-items: center; gap: 8px; font-size: 0.85rem; color: var(--text-medium); background: #f3f4f6; padding: 6px 12px; border-radius: 6px; }
.btn-view { background: transparent; border: none; color: var(--primary-color); cursor: pointer; font-weight: 600; font-size: 0.85rem; }

/* 聊天历史滚动区 */
.chat-history {
  flex: 1; /* 占据中间所有可用高度 */
  padding: 25px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
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

/* ================= 底部输入区 (压缩尺寸) ================= */
.chat-input-area {
  background: white;
  border-top: 1px solid #e5e7eb;
  padding: 10px 25px 15px; /* 减小内边距 */
  display: flex;
  flex-direction: column;
  flex-shrink: 0; /* 绝对不压缩这个区域 */
  height: auto;
}

.input-toolbar { display: flex; gap: 12px; margin-bottom: 8px; }
.tool-btn { background: transparent; border: none; font-size: 1.1rem; cursor: pointer; filter: grayscale(1); transition: 0.2s; padding: 0; }
.tool-btn:hover { filter: grayscale(0); transform: translateY(-2px); }

textarea {
  width: 100%;
  height: 60px; /* 极大地压缩文本框高度 */
  border: none;
  resize: none;
  outline: none;
  font-size: 0.95rem;
  font-family: inherit;
  color: var(--text-dark);
  background: transparent;
}
textarea::placeholder { color: #cbd5e1; }

.input-footer { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 5px; }
.hint { font-size: 0.75rem; color: #9ca3af; }
.send-btn { padding: 8px 25px; border-radius: 6px; font-size: 0.9rem; }

/* 占位空状态 */
.empty-chat { justify-content: center; align-items: center; background: #f8fafc; }
.empty-content { text-align: center; color: #9ca3af; }
.empty-content .icon { font-size: 3rem; margin-bottom: 15px; opacity: 0.5; }
.empty-content h3 { color: var(--text-medium); margin-bottom: 10px; font-size: 1.2rem; }
</style>