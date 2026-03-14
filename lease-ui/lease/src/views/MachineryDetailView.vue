<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMachineryDetailAPI } from '@/api/machinery' // 引入刚写的详情接口
import { showMessage } from '@/utils/message'

const route = useRoute()
const router = useRouter()

// ================= 1. 真实数据容器 =================
// 初始设置为空对象，避免在请求未返回前出现 undefined 报错
const machine = ref({
  id: null,
  price: 0,
  deposit: 0,
  images: [],
  parameters: {}
})

// 加载状态
const isLoading = ref(true)

// 获取当前选中的大图索引
const activeImageIndex = ref(0)

// ================= 2. 发起网络请求获取真实数据 =================
const fetchMachineDetail = async () => {
  const targetId = route.params.id
  if (!targetId) return
  
  isLoading.value = true
  try {
    const res = await getMachineryDetailAPI(targetId)
    const data = res.data

    // 核心处理 1：处理图片列表
    // 数据库可能存的是逗号分隔的字符串："img1.jpg,img2.jpg"
    let parsedImages = ['http://localhost:9191/images/demo.jpg'] // 默认兜底图
    if (data.images) {
      parsedImages = data.images.split(',').filter(img => img.trim() !== '')
    }

    // 核心处理 2：处理 JSON 参数
    // 数据库 parameters 字段存的是 JSON 字符串
    let parsedParams = {}
    if (data.parameters) {
      if (typeof data.parameters === 'string') {
        try { parsedParams = JSON.parse(data.parameters) } catch (e) { console.warn('参数解析失败') }
      } else {
        parsedParams = data.parameters // 如果后端框架直接帮你转成了对象
      }
    }

    // 拼装最终响应式数据
    machine.value = {
      ...data,
      images: parsedImages,
      parameters: parsedParams
    }
  } catch (error) {
    console.error('获取设备详情失败:', error)
  } finally {
    isLoading.value = false
  }
}

// ================= 3. 日期与天数计算逻辑 =================
const getTodayStr = () => new Date().toISOString().split('T')[0]
const getTomorrowStr = () => {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
}

const today = ref(getTodayStr())
const startDate = ref(getTodayStr())
const endDate = ref(getTomorrowStr())

// 监听起租时间：如果起租时间选到了结束时间之后，自动把结束时间往后顺延一天
watch(startDate, (newStart) => {
  if (endDate.value <= newStart) {
    const d = new Date(newStart)
    d.setDate(d.getDate() + 1)
    endDate.value = d.toISOString().split('T')[0]
  }
})

// 自动计算租赁天数
const rentDays = computed(() => {
  const start = new Date(startDate.value)
  const end = new Date(endDate.value)
  const diffTime = Math.abs(end - start)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays > 0 ? diffDays : 1 
})

// 自动计算总租金
const totalPrice = computed(() => {
  return ((machine.value.price || 0) * rentDays.value).toFixed(2)
})


// ================= 4. 按钮交互逻辑 =================
const handleRent = () => {
  // 【鉴权拦截】如果未登录，立刻踢去登录页
  const userInfo = localStorage.getItem('user_info')
  if (!userInfo) {
    showMessage('请先登录后再进行租赁操作', 'warning')
    router.push('/login')
    return
  }

  // 携带该商品的真实参数跳转结算页
  router.push({
    path: '/confirm-order',
    query: {
      machineId: machine.value.id,
      ownerId: machine.value.ownerId, 
      startDate: startDate.value,
      endDate: endDate.value,
      days: rentDays.value,
      price: machine.value.price,
      deposit: machine.value.deposit
    }
  })
}

const handleContact = () => {
  const userInfo = localStorage.getItem('user_info')
  if (!userInfo) {
    showMessage('请先登录', 'warning')
    router.push('/login')
    return
  }
  
  const currentUser = JSON.parse(userInfo)
  // 防呆：不能自己跟自己聊天
  if (currentUser.userId === machine.value.ownerId) {
    showMessage('这是您自己发布的设备哦！', 'warning')
    return
  }

  // 🌟 核心：跳转时带上机主的 ID
  router.push({
    path: '/messages',
    query: { targetId: machine.value.ownerId }
  })
}

// 页面挂载时初始化
onMounted(() => {
  window.scrollTo({ top: 0 })
  fetchMachineDetail() // 调用请求数据
})
</script>

<template>
  <div class="detail-page">
    <div class="wide-container">
      
      <nav class="breadcrumb">
        <span @click="router.push('/')">首页</span>
        <span class="separator">/</span>
        <span @click="router.push('/machinery-list')">农机展厅</span>
        <span class="separator">/</span>
        <span class="current">{{ machine.name || '设备详情' }}</span>
      </nav>

      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>正在获取农机详情，请稍候...</p>
      </div>

      <div v-else-if="machine.id" class="detail-card">
        
        <div class="gallery-section">
          <div class="main-image-box">
            <img :src="machine.images[activeImageIndex]" :alt="machine.name" />
          </div>
          <div class="thumbnail-list">
            <div 
              v-for="(img, index) in machine.images" 
              :key="index"
              :class="['thumb-item', { active: activeImageIndex === index }]"
              @click="activeImageIndex = index"
              @mouseenter="activeImageIndex = index"
            >
              <img :src="img" alt="thumbnail" />
            </div>
          </div>
        </div>

        <div class="info-section">
          <div class="info-header">
            <h1 class="title">{{ machine.name }}</h1>
            <p class="desc">{{ machine.description || '机主很懒，没有填写详细描述...' }}</p>
          </div>

          <div class="price-panel">
            <div class="price-row main-price">
              <span class="label">每日租金</span>
              <div class="val">
                <span class="symbol">￥</span>
                <span class="num">{{ (machine.price || 0).toFixed(2) }}</span>
              </div>
            </div>
            <div class="price-row sub-price">
              <span class="label">设备押金</span>
              <div class="val">￥{{ (machine.deposit || 0).toFixed(2) }} <span class="hint">(归还后全额退还)</span></div>
            </div>
          </div>

          <div class="params-box">
            <h3 class="params-title">核心配置</h3>
            <ul class="params-list">
              <li><span class="param-name">品牌</span><span class="param-value">{{ machine.brand || '未知' }}</span></li>
              <li v-for="(val, key) in machine.parameters" :key="key">
                <span class="param-name">{{ key }}</span>
                <span class="param-value">{{ val }}</span>
              </li>
              <li v-if="Object.keys(machine.parameters).length === 0">
                <span class="param-value" style="color: #9ca3af;">暂无更多配置参数</span>
              </li>
            </ul>
          </div>

          <div class="action-panel">
            <div class="date-picker-row">
              <div class="date-item">
                <span class="label">起租时间</span>
                <input type="date" v-model="startDate" :min="today" />
              </div>
              <div class="date-item separator">至</div>
              <div class="date-item">
                <span class="label">结束时间</span>
                <input type="date" v-model="endDate" :min="startDate" />
              </div>
              <div class="date-item result-days">
                <span class="label">共计</span>
                <span class="days-val"><strong>{{ rentDays }}</strong> 天</span>
              </div>
            </div>
            
            <div class="total-row">
              <span class="label">预计总租金：</span>
              <span class="total-price">￥{{ totalPrice }}</span>
            </div>

            <div class="btn-group">
              <button class="btn-buy" @click="handleRent" :disabled="machine.status !== 0">
                {{ machine.status === 0 ? '立即租赁' : '该设备已被租用或下架' }}
              </button>
              <button class="btn-chat" @click="handleContact">
                <span class="icon">💬</span> 联系机主
              </button>
            </div>
          </div>
          
          <div class="service-tags">
            <span><i class="icon">🛡️</i> 平台担保交易</span>
            <span><i class="icon">🔧</i> 故障极速响应</span>
            <span><i class="icon">💰</i> 押金安全退还</span>
          </div>

        </div>
      </div>

      <div v-else class="loading-state">
        <h2 style="color: #ef4444;">抱歉，该农机已下架或不存在。</h2>
        <button class="btn-primary" style="margin-top: 20px;" @click="router.push('/machinery-list')">返回展厅</button>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* 保持 CSS 完全不变，直接复用刚才我们优化好的精美排版 */
.detail-page { background-color: #f8fafc; min-height: calc(100vh - 80px); padding-bottom: 40px; }
.breadcrumb { padding: 15px 0; font-size: 0.85rem; color: #64748b; }
.breadcrumb span { cursor: pointer; transition: color 0.2s; }
.breadcrumb span:hover { color: var(--primary-color); }
.breadcrumb .separator { margin: 0 8px; color: #cbd5e1; cursor: default; }
.breadcrumb .current { color: var(--text-dark); font-weight: 600; cursor: default; }

.detail-card { background: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.04); display: flex; padding: 24px; gap: 30px; }

.gallery-section { width: 400px; flex-shrink: 0; }
.main-image-box { width: 100%; height: 360px; border-radius: 8px; border: 1px solid #f1f5f9; padding: 15px; margin-bottom: 12px; display: flex; align-items: center; justify-content: center; }
.main-image-box img { width: 100%; height: 100%; object-fit: contain; mix-blend-mode: multiply; }

.thumbnail-list { display: flex; gap: 10px; }
.thumb-item { width: 64px; height: 64px; border: 2px solid transparent; border-radius: 6px; padding: 4px; cursor: pointer; transition: all 0.2s; background: #f8fafc; }
.thumb-item img { width: 100%; height: 100%; object-fit: contain; mix-blend-mode: multiply; }
.thumb-item.active, .thumb-item:hover { border-color: var(--primary-color); background: white; }

.info-section { flex: 1; display: flex; flex-direction: column; }
.title { font-size: 1.4rem; font-weight: 800; color: var(--text-dark); margin: 0 0 8px 0; }
.desc { font-size: 0.9rem; color: #64748b; line-height: 1.5; margin: 0 0 15px 0; }

.price-panel { background: linear-gradient(to right, #fff1f2, #fff7ed); padding: 12px 15px; border-radius: 8px; margin-bottom: 20px; }
.price-row { display: flex; align-items: baseline; gap: 15px; }
.price-row.main-price { margin-bottom: 6px; }
.price-row .label { width: 60px; font-size: 0.85rem; color: #94a3b8; }
.main-price .val { color: #ef4444; }
.main-price .symbol { font-size: 1.1rem; font-weight: 600; }
.main-price .num { font-size: 1.8rem; font-weight: 800; } 
.sub-price .val { font-size: 0.9rem; color: var(--text-dark); font-weight: 600; }
.sub-price .hint { font-weight: normal; color: #94a3b8; font-size: 0.75rem; margin-left: 5px; }

.params-box { margin-bottom: 20px; }
.params-title { font-size: 1rem; color: var(--text-dark); margin: 0 0 8px 0; padding-bottom: 6px; border-bottom: 1px solid #f1f5f9; }
.params-list { display: flex; flex-wrap: wrap; gap: 10px 20px; }
.params-list li { display: flex; width: calc(50% - 10px); font-size: 0.85rem; }
.param-name { color: #94a3b8; width: 80px; flex-shrink: 0; }
.param-value { color: var(--text-dark); font-weight: 500; }

.action-panel { margin-top: auto; border-top: 1px dashed #e2e8f0; padding-top: 20px; }

.date-picker-row { display: flex; align-items: center; gap: 15px; margin-bottom: 18px; }
.date-item { display: flex; flex-direction: column; }
.date-item .label { font-size: 0.8rem; color: #64748b; margin-bottom: 6px; font-weight: 600; }
.date-item input[type="date"] { padding: 10px 15px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.95rem; color: var(--text-dark); outline: none; transition: border-color 0.2s; font-family: inherit; cursor: pointer; }
.date-item input[type="date"]:focus { border-color: var(--primary-color); box-shadow: 0 0 0 3px rgba(84, 101, 255, 0.1); }
.date-picker-row .separator { margin-top: 20px; color: #94a3b8; font-weight: bold; font-size: 0.9rem; }
.result-days { margin-left: 10px; background: #f8fafc; padding: 8px 15px; border-radius: 8px; align-items: center; justify-content: center; border: 1px dashed #cbd5e1; }
.result-days .days-val { color: var(--text-dark); font-size: 0.9rem; }
.result-days .days-val strong { color: var(--primary-color); font-size: 1.2rem; }

.total-row { margin-bottom: 20px; font-size: 1rem; color: var(--text-dark); font-weight: 600; display: flex; align-items: center;}
.total-price { color: #ef4444; font-size: 1.4rem; font-weight: 800; margin-left: 10px; } 

.btn-group { display: flex; gap: 15px; }
.btn-buy { flex: 2; background: var(--primary-color); color: white; border: none; padding: 12px; border-radius: 8px; font-size: 1rem; font-weight: 600; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 12px rgba(84, 101, 255, 0.2); }
.btn-buy:hover:not(:disabled) { background: var(--hover-black); transform: translateY(-2px); }
.btn-buy:disabled { background: #cbd5e1; cursor: not-allowed; box-shadow: none; }

.btn-chat { flex: 1; background: #eef2ff; color: var(--primary-color); border: none; padding: 12px; border-radius: 8px; font-size: 0.95rem; font-weight: 600; cursor: pointer; transition: 0.3s; display: flex; align-items: center; justify-content: center; gap: 6px; }
.btn-chat:hover { background: #e0e7ff; }

.service-tags { display: flex; gap: 15px; margin-top: 20px; font-size: 0.8rem; color: #64748b; }
.service-tags .icon { font-style: normal; margin-right: 4px; }

.loading-state { text-align: center; padding: 100px 0; }
.spinner { width: 40px; height: 40px; border: 4px solid #e2e8f0; border-top-color: var(--primary-color); border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 15px; }
@keyframes spin { to { transform: rotate(360deg); } }
.btn-primary { background: var(--primary-color); color: white; border: none; padding: 10px 25px; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.3s; }

@media (max-width: 900px) {
  .detail-card { flex-direction: column; }
  .gallery-section { width: 100%; }
  .main-image-box { height: 350px; }
  .params-list li { width: 100%; }
  .date-picker-row { flex-wrap: wrap; }
}
</style>