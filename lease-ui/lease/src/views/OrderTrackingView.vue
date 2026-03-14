<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderLogisticsAPI } from '@/api/order'
import { showMessage } from '@/utils/message'

const route = useRoute()
const router = useRouter()

// 1. 获取基础订单号
const orderNo = ref(route.query.orderNo || '')

// 2. 接收从个人中心传过来的丰富商品参数 (带有兜底默认值)
const orderBasicInfo = ref({
  machineName: route.query.machineName || '农用机械设备',
  image: route.query.image || 'http://localhost:9191/images/demo.jpg',
  totalAmount: route.query.total || '0.00',
  rentDays: route.query.days || '0',
  startDate: route.query.date || '未知日期' // 🌟 新增接收的起租日期
})

const logisticsList = ref([])
const isLoading = ref(true)

// 3. 拉取真实轨迹数据
const fetchLogistics = async () => {
  if (!orderNo.value) {
    showMessage('订单号异常', 'error')
    return
  }
  try {
    const res = await getOrderLogisticsAPI(orderNo.value)
    if (res.code === 200) {
      logisticsList.value = res.data || []
    }
  } catch (error) {
    console.error('获取轨迹失败', error)
  } finally {
    isLoading.value = false
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

onMounted(() => {
  window.scrollTo({ top: 0 })
  fetchLogistics()
})
</script>

<template>
  <div class="tracking-page">
    <div class="main-container">
      
      <div class="nav-header">
        <span class="back-btn" @click="router.push('/profile')">‹ 返回个人中心</span>
        <h2>调度与物流详情</h2>
        <span class="placeholder"></span> </div>

      <div class="content-card product-card">
        <div class="order-header">
          <span class="order-no">订单编号：{{ orderNo }}</span>
          <span class="copy-btn" @click="showMessage('单号已复制', 'success')">复制</span>
        </div>
        
        <div class="product-info">
          <img :src="orderBasicInfo.image" class="product-img" alt="商品主图" />
          <div class="product-details">
            <h3 class="product-title">{{ orderBasicInfo.machineName }}</h3>
            
            <div class="config-tags">
              <span class="tag brand-tag">平台优选</span>
              <span class="tag">起租日: {{ orderBasicInfo.startDate }}</span>
              <span class="tag">租期: {{ orderBasicInfo.rentDays }} 天</span>
            </div>
            
            <div class="price-row">
              <span class="label">实付款</span>
              <span class="amount">￥{{ orderBasicInfo.totalAmount }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="content-card timeline-card">
        <h3 class="card-title">调度动态</h3>
        
        <div v-if="isLoading" class="empty-state">
          <div class="spinner"></div>正在获取最新调度信息...
        </div>
        <div v-else-if="logisticsList.length === 0" class="empty-state">
          暂无调度轨迹信息，请等待机主发货。
        </div>
        
        <div v-else class="timeline">
          <div 
            class="timeline-item" 
            v-for="(item, index) in logisticsList" 
            :key="item.id"
            :class="{ 'is-latest': index === 0 }"
          >
            <div class="timeline-tail" v-if="index !== logisticsList.length - 1"></div>
            <div class="timeline-node"></div>
            
            <div class="timeline-content">
              <div class="desc">{{ item.description }}</div>
              <div class="time">{{ formatTime(item.createTime) }}</div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* ================= 基础页面布局 ================= */
.tracking-page {
  background-color: #f3f4f6;
  min-height: calc(100vh - 80px);
  padding: 30px 20px 60px 20px;
}
.main-container {
  max-width: 650px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px; /* 卡片之间的间距 */
}

/* ================= 顶部导航 ================= */
.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.nav-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-dark);
}
.back-btn {
  color: #64748b;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
  font-size: 0.95rem;
}
.back-btn:hover { color: var(--primary-color); }
.placeholder { width: 90px; }

/* ================= 通用卡片样式 ================= */
.content-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
}

/* ================= 商品简述卡片 ================= */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 12px;
  margin-bottom: 16px;
}
.order-no {
  font-size: 0.85rem;
  color: #64748b;
  font-family: monospace;
}
.copy-btn {
  font-size: 0.8rem;
  color: var(--primary-color);
  cursor: pointer;
  background: #eef2ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.product-info {
  display: flex;
  gap: 16px;
}
.product-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #f1f5f9;
}
.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.product-title {
  margin: 0 0 8px 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-dark);
}

/* 商品配置标签 */
.config-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.tag {
  font-size: 0.75rem;
  background: #f1f5f9;
  color: #64748b;
  padding: 3px 8px;
  border-radius: 4px;
}
.brand-tag {
  background: #fee2e2;
  color: #ef4444;
  font-weight: 600;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.price-row .label { font-size: 0.8rem; color: #94a3b8; }
.price-row .amount { font-size: 1.3rem; font-weight: 800; color: #ef4444; } /* 金额标红凸显 */

/* ================= 时间轴卡片 ================= */
.card-title {
  margin: 0 0 20px 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-dark);
}

.empty-state {
  text-align: center;
  color: #94a3b8;
  padding: 30px 0;
  font-size: 0.9rem;
}

.timeline { position: relative; margin-left: 10px; }
.timeline-item { position: relative; padding-bottom: 25px; }
.timeline-item:last-child { padding-bottom: 0; }

.timeline-tail {
  position: absolute;
  left: 6px;
  top: 18px;
  height: 100%;
  border-left: 2px dashed #e2e8f0;
}
.timeline-node {
  position: absolute;
  left: 0;
  top: 4px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background-color: #cbd5e1;
  border: 3px solid white;
  box-shadow: 0 0 0 2px #f8fafc;
  z-index: 2;
  transition: all 0.3s;
}

.timeline-content {
  margin-left: 35px;
  top: -2px;
  position: relative;
}
.timeline-content .desc {
  font-size: 0.95rem;
  color: #4b5563;
  line-height: 1.5;
  font-weight: 500;
}
.timeline-content .time {
  font-size: 0.8rem;
  color: #94a3b8;
  margin-top: 4px;
}

/* 💥 最新的第一条节点高亮特效 */
.timeline-item.is-latest .timeline-node {
  background-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(84, 101, 255, 0.2); 
}
.timeline-item.is-latest .timeline-content .desc {
  color: var(--primary-color);
  font-weight: 700;
}
.timeline-item.is-latest .timeline-content .time {
  color: var(--primary-color);
}
</style>