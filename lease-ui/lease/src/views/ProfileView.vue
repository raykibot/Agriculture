<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { logoutAPI } from '@/api/user' // 引入退出登录接口
import { showMessage } from '@/utils/message' // 引入全局弹窗

const router = useRouter()

// 当前激活的左侧菜单栏
const currentTab = ref('info') 

// ================= 1. 个人基础信息数据 =================
const userInfo = ref({
  name: '李建国',
  phone: '138****5678',
  status: '已认证',
  regDate: '2026-01-15',
  region: '河南省 驻马店市 确山县',
  address: '河南省 驻马店市 确山县 盘龙街道 智慧农业产业园A区 102栋' 
})

// ================= 2. 我的订单数据 =================
const orderRole = ref('rented') 
const orderStatus = ref('all') 

const orders = ref([
  { id: 'ORD-20260221-01', role: 'rented', status: 'ongoing', machine: '东方红 LX2204 拖拉机', image: 'https://images.unsplash.com/photo-1592982537447-6f296312a022?auto=format&fit=crop&q=80&w=400', date: '2026-02-20', days: 3, total: 1350 },
  { id: 'ORD-20260215-02', role: 'rented', status: 'completed', machine: '大疆 T60 植保无人机', image: 'https://images.unsplash.com/photo-1579824225022-77e8a93e3630?auto=format&fit=crop&q=80&w=400', date: '2026-02-15', days: 1, total: 300 },
  { id: 'ORD-20260225-03', role: 'rented', status: 'unpaid', machine: '极飞 V50 农业无人车', image: 'https://images.unsplash.com/photo-1586771107445-d3ca888129ff?auto=format&fit=crop&q=80&w=400', date: '2026-02-25', days: 2, total: 300 },
  { id: 'ORD-20260210-04', role: 'leased', status: 'completed', machine: '久保田 PRO1408 收割机', image: 'https://images.unsplash.com/photo-1605615781358-1db2a197772d?auto=format&fit=crop&q=80&w=400', date: '2026-02-10', days: 5, total: 4000 },
  { id: 'ORD-20260222-05', role: 'leased', status: 'ongoing', machine: '久保田 PRO1408 收割机', image: 'https://images.unsplash.com/photo-1605615781358-1db2a197772d?auto=format&fit=crop&q=80&w=400', date: '2026-02-21', days: 2, total: 1600 }
])

const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    const matchRole = order.role === orderRole.value
    const matchStatus = orderStatus.value === 'all' || order.status === orderStatus.value
    return matchRole && matchStatus
  })
})

// ================= 3. 我发布的农机数据 =================
const publishedMachines = ref([
  { id: 'M-001', name: '久保田 PRO1408 收割机', image: 'https://images.unsplash.com/photo-1605615781358-1db2a197772d?auto=format&fit=crop&q=80&w=400', status: 'renting', publishTime: '2026-01-10 09:30', rentOutTime: '2026-02-21 08:00', returnTime: '2026-02-23 18:00', offlineTime: '-', price: 800 },
  { id: 'M-002', name: '雷沃谷神 联合收割机', image: 'https://images.unsplash.com/photo-1625246333195-78d9c38ad449?auto=format&fit=crop&q=80&w=400', status: 'available', publishTime: '2026-02-01 14:20', rentOutTime: '-', returnTime: '-', offlineTime: '-', price: 600 },
  { id: 'M-003', name: '旧款 东方红小型拖拉机', image: 'https://images.unsplash.com/photo-1589923188900-85dae523342b?auto=format&fit=crop&q=80&w=400', status: 'offline', publishTime: '2025-10-05 10:00', rentOutTime: '-', returnTime: '-', offlineTime: '2026-01-20 12:00', price: 200 }
])

const statusMap = {
  'unpaid': { text: '待付款', color: '#f59e0b', bg: '#fef3c7' },
  'ongoing': { text: '进行中', color: '#3b82f6', bg: '#eff6ff' },
  'completed': { text: '已完成', color: '#10b981', bg: '#ecfdf5' },
  'available': { text: '闲置可租', color: '#10b981', bg: '#ecfdf5' },
  'renting': { text: '正在租赁中', color: '#3b82f6', bg: '#eff6ff' },
  'offline': { text: '已下架', color: '#6b7280', bg: '#f3f4f6' }
}

// ================= 4. 退出登录逻辑 =================
const handleLogout = async () => {
  // 加入一个原生的确认框，防止用户误触
  if (!confirm('确定要退出当前账号吗？')) return

  try {
    // 调用后端退出接口清除服务端的 token 状态（如果有的话）
    await logoutAPI()
  } catch (error) {
    console.warn('后端退出异常，强制清理本地数据', error)
  } finally {
    // 无论后端是否报错，前端必须清除本地凭证
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_info')
    
    // 弹出提示并跳回登录页
    showMessage('您已安全退出登录', 'success')
    router.push('/login')
  }
}
</script>

<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="user-brief">
        <div class="avatar">
          <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="User Avatar" />
        </div>
        <h3 class="user-name">{{ userInfo.name }}</h3>
        <span class="user-role">农户 / 租赁方</span>
      </div>

      <nav class="sidebar-nav">
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'info' }]" @click="currentTab = 'info'">
          <span class="icon">👤</span> 个人信息
        </a>
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'orders' }]" @click="currentTab = 'orders'">
          <span class="icon">📋</span> 历史订单
        </a>
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'published' }]" @click="currentTab = 'published'">
          <span class="icon">🚜</span> 我发布的农机
        </a>
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'wallet' }]" @click="currentTab = 'wallet'">
          <span class="icon">💳</span> 钱包与资金
        </a>
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'settings' }]" @click="currentTab = 'settings'">
          <span class="icon">⚙️</span> 账号设置
        </a>
      </nav>
      
      <div class="sidebar-footer">
        <button class="btn-logout" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <main class="main-content">
      
      <div v-if="currentTab === 'info'" class="tab-pane fade-in">
        <div class="content-card">
          <div class="card-header">
            <h2>基础档案</h2>
            <button class="btn-edit">编辑资料</button>
          </div>
          <div class="info-grid">
            <div class="info-item"><span class="label">真实姓名</span><span class="value">{{ userInfo.name }}</span></div>
            <div class="info-item"><span class="label">手机号码</span><span class="value">{{ userInfo.phone }}</span></div>
            <div class="info-item"><span class="label">实名认证状态</span><span class="value status-verified">{{ userInfo.status }} ✓</span></div>
            <div class="info-item"><span class="label">注册时间</span><span class="value">{{ userInfo.regDate }}</span></div>
            <div class="info-item"><span class="label">主要作业地区</span><span class="value">{{ userInfo.region }}</span></div>
            <div class="info-item full-width address-box">
              <span class="label">默认收货/接机场地地址</span>
              <span class="value">{{ userInfo.address }}</span>
            </div>
          </div>
        </div>

        <div class="stats-grid mt-30">
          <div class="stat-card"><div class="stat-num">12</div><div class="stat-label">累计租赁次数</div></div>
          <div class="stat-card"><div class="stat-num">450</div><div class="stat-label">作业亩数 (亩)</div></div>
          <div class="stat-card"><div class="stat-num">¥ 0.00</div><div class="stat-label">当前待支付</div></div>
        </div>
      </div>

      <div v-if="currentTab === 'orders'" class="tab-pane fade-in">
        <div class="content-card full-height">
          <div class="card-header border-none">
            <h2>订单管理</h2>
          </div>
          
          <div class="role-tabs">
            <button :class="['role-tab', { active: orderRole === 'rented' }]" @click="orderRole = 'rented'">我租赁的机器 (我是租客)</button>
            <button :class="['role-tab', { active: orderRole === 'leased' }]" @click="orderRole = 'leased'">别人租赁我的 (我是机主)</button>
          </div>

          <div class="filter-pills">
            <span :class="['pill', { active: orderStatus === 'all' }]" @click="orderStatus = 'all'">全部</span>
            <span :class="['pill', { active: orderStatus === 'unpaid' }]" @click="orderStatus = 'unpaid'">待付款</span>
            <span :class="['pill', { active: orderStatus === 'ongoing' }]" @click="orderStatus = 'ongoing'">进行中</span>
            <span :class="['pill', { active: orderStatus === 'completed' }]" @click="orderStatus = 'completed'">已完成</span>
          </div>

          <div class="order-list">
            <div class="empty-state" v-if="filteredOrders.length === 0">暂无符合条件的订单记录</div>
            
            <div class="list-card" v-for="order in filteredOrders" :key="order.id">
              <div class="list-card-header">
                <span class="order-id">订单号：{{ order.id }}</span>
                <span class="status-badge" :style="{ color: statusMap[order.status].color, backgroundColor: statusMap[order.status].bg }">
                  {{ statusMap[order.status].text }}
                </span>
              </div>
              <div class="list-card-body">
                <img :src="order.image" class="item-img" />
                <div class="item-info">
                  <h4 class="item-title">{{ order.machine }}</h4>
                  <p class="item-desc">起租日期: {{ order.date }} &nbsp;|&nbsp; 租期: {{ order.days }} 天</p>
                </div>
                <div class="item-price">
                  <span class="label">订单总价</span>
                  <span class="amount">￥{{ order.total }}</span>
                </div>
                <div class="item-actions">
                  <button class="btn-outline" v-if="order.status === 'completed'">查看评价</button>
                  <button class="btn-primary-small" v-if="order.status === 'unpaid'">去支付</button>
                  <button class="btn-primary-small" v-if="order.status === 'ongoing'">确认完工</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="currentTab === 'published'" class="tab-pane fade-in">
        <div class="content-card full-height">
          <div class="card-header border-none">
            <h2>我发布的农机设备</h2>
            <button class="btn-primary-small">+ 发布新农机</button>
          </div>

          <div class="machine-list">
            <div class="list-card" v-for="machine in publishedMachines" :key="machine.id">
              <div class="list-card-body">
                <img :src="machine.image" class="item-img large-img" />
                <div class="item-info expand">
                  <div class="title-row">
                    <h4 class="item-title">{{ machine.name }}</h4>
                    <span class="status-badge" :style="{ color: statusMap[machine.status].color, backgroundColor: statusMap[machine.status].bg }">
                      {{ statusMap[machine.status].text }}
                    </span>
                  </div>
                  
                  <div class="timeline-grid">
                    <div class="time-item"><span>发布时间:</span> {{ machine.publishTime }}</div>
                    <div class="time-item" v-if="machine.status === 'renting'"><span>起租时间:</span> {{ machine.rentOutTime }}</div>
                    <div class="time-item" v-if="machine.status === 'renting'"><span>预计归还:</span> <strong style="color:var(--primary-color)">{{ machine.returnTime }}</strong></div>
                    <div class="time-item" v-if="machine.status === 'offline'"><span>下架时间:</span> {{ machine.offlineTime }}</div>
                  </div>
                </div>
                
                <div class="item-price border-left">
                  <span class="label">日租金</span>
                  <span class="amount">￥{{ machine.price }}</span>
                  <span class="unit">/天</span>
                </div>
                
                <div class="item-actions vertical">
                  <button class="btn-outline w-full" v-if="machine.status === 'available'">编辑信息</button>
                  <button class="btn-outline w-full" v-if="machine.status === 'available'">下架设备</button>
                  <button class="btn-primary-small w-full" v-if="machine.status === 'renting'">查看订单</button>
                  <button class="btn-primary-small w-full" v-if="machine.status === 'offline'">重新上架</button>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>

      <div v-if="currentTab === 'wallet' || currentTab === 'settings'" class="tab-pane fade-in flex-center">
        <div class="empty-state">
          <h2>🚧 功能开发中...</h2>
          <p>该模块正在快马加鞭建设中，敬请期待。</p>
        </div>
      </div>

    </main>
  </div>
</template>

<style scoped>
.dashboard-layout {
  display: flex;
  min-height: calc(100vh - 80px);
  background-color: #f3f4f6;
  width: 100%;
}

.sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  padding: 30px 20px;
  flex-shrink: 0;
}

.user-brief { text-align: center; margin-bottom: 30px; padding-bottom: 30px; border-bottom: 1px solid #f3f4f6; }
.avatar { width: 90px; height: 90px; border-radius: 50%; background: #f3f4f6; margin: 0 auto 15px; overflow: hidden; border: 3px solid white; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.avatar img { width: 100%; height: 100%; object-fit: cover; }
.user-name { font-size: 1.3rem; font-weight: 700; color: var(--text-dark); margin-bottom: 5px; }
.user-role { font-size: 0.85rem; color: var(--primary-color); font-weight: 600; background: rgba(84, 101, 255, 0.1); padding: 4px 12px; border-radius: 20px; }

.sidebar-nav { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.nav-item { display: flex; align-items: center; gap: 15px; padding: 14px 20px; border-radius: 10px; color: var(--text-medium); font-weight: 600; transition: 0.3s; font-size: 1.05rem; }
.nav-item:hover { background: #f9fafb; color: var(--primary-color); }
.nav-item.active { background: var(--primary-color); color: white; box-shadow: 0 4px 15px rgba(84, 101, 255, 0.3); }

.sidebar-footer { padding-top: 20px; border-top: 1px solid #f3f4f6; }
.btn-logout { width: 100%; background: transparent; color: #ef4444; border: 1px solid #fca5a5; padding: 12px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.3s; }
.btn-logout:hover { background: #fee2e2; }

.main-content {
  flex: 1;
  padding: 40px;
  overflow-y: auto; 
}

.tab-pane { display: flex; flex-direction: column; gap: 30px; height: 100%; }
.fade-in { animation: fadeIn 0.3s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.content-card { background: white; border-radius: 16px; padding: 35px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); }
.full-height { min-height: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; padding-bottom: 15px; border-bottom: 1px solid #f3f4f6; }
.card-header.border-none { border-bottom: none; padding-bottom: 0; margin-bottom: 25px; }
.card-header h2 { font-size: 1.4rem; color: var(--text-dark); font-weight: 800; }

.btn-edit { background: transparent; border: 2px solid var(--primary-color); color: var(--primary-color); padding: 8px 20px; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.3s; }
.btn-edit:hover { background: var(--primary-color); color: white; }
.btn-primary-small { background: var(--primary-color); color: white; border: none; padding: 8px 20px; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.3s; }
.btn-primary-small:hover { background: var(--hover-black); }
.btn-outline { background: white; color: var(--text-dark); border: 1px solid #d1d5db; padding: 8px 20px; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.3s; }
.btn-outline:hover { border-color: var(--primary-color); color: var(--primary-color); }
.w-full { width: 100%; }

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
.full-width { grid-column: 1 / -1; }
.info-item { display: flex; flex-direction: column; gap: 8px; }
.info-item .label { color: var(--text-light); font-size: 0.95rem; }
.info-item .value { color: var(--text-dark); font-size: 1.1rem; font-weight: 500; }
.status-verified { color: #10b981 !important; font-weight: 700 !important; }
.address-box { background: #f9fafb; padding: 20px; border-radius: 12px; border: 1px dashed #e5e7eb; }

.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.mt-30 { margin-top: 30px; }
.stat-card { background: white; padding: 30px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border-left: 4px solid var(--primary-color); }
.stat-num { font-size: 2.2rem; font-weight: 900; color: var(--text-dark); margin-bottom: 5px; }
.stat-label { color: var(--text-medium); font-weight: 500; }

.role-tabs { display: flex; border-bottom: 2px solid #f3f4f6; margin-bottom: 20px; }
.role-tab { background: transparent; border: none; padding: 10px 25px; font-size: 1.1rem; font-weight: 600; color: var(--text-medium); cursor: pointer; position: relative; bottom: -2px; }
.role-tab.active { color: var(--primary-color); border-bottom: 2px solid var(--primary-color); }

.filter-pills { display: flex; gap: 15px; margin-bottom: 25px; }
.pill { padding: 6px 18px; background: #f3f4f6; color: var(--text-medium); border-radius: 20px; font-size: 0.9rem; cursor: pointer; font-weight: 600; transition: 0.3s; }
.pill.active { background: var(--primary-color); color: white; }

.list-card { border: 1px solid #e5e7eb; border-radius: 12px; margin-bottom: 20px; overflow: hidden; transition: 0.3s; }
.list-card:hover { box-shadow: 0 10px 25px rgba(0,0,0,0.05); border-color: #d1d5db; }
.list-card-header { background: #f9fafb; padding: 12px 20px; display: flex; justify-content: space-between; border-bottom: 1px solid #e5e7eb; font-size: 0.9rem; }
.order-id { color: var(--text-medium); }
.status-badge { padding: 4px 10px; border-radius: 6px; font-weight: 700; font-size: 0.8rem; }

.list-card-body { padding: 20px; display: flex; align-items: center; gap: 20px; }
.item-img { width: 120px; height: 80px; border-radius: 8px; object-fit: cover; }
.item-img.large-img { width: 160px; height: 110px; }

.item-info { flex: 1; }
.item-info.expand { flex: 2; }
.item-title { font-size: 1.15rem; color: var(--text-dark); font-weight: 700; margin-bottom: 8px; }
.item-desc { color: var(--text-medium); font-size: 0.9rem; }
.title-row { display: flex; align-items: center; gap: 15px; margin-bottom: 15px; }

.timeline-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; font-size: 0.85rem; color: var(--text-medium); background: #f9fafb; padding: 10px 15px; border-radius: 8px; }
.time-item span { color: var(--text-light); margin-right: 5px; }

.item-price { text-align: right; padding-right: 20px; display: flex; flex-direction: column; justify-content: center; }
.item-price.border-left { padding-left: 20px; border-left: 1px dashed #e5e7eb; }
.item-price .label { font-size: 0.85rem; color: var(--text-light); }
.item-price .amount { font-size: 1.4rem; font-weight: 800; color: var(--text-dark); }
.item-price .unit { font-size: 0.8rem; color: var(--text-medium); }

.item-actions { display: flex; gap: 10px; }
.item-actions.vertical { flex-direction: column; width: 120px; }

.empty-state { text-align: center; padding: 60px 0; color: var(--text-medium); }
.flex-center { justify-content: center; align-items: center; }

@media (max-width: 1024px) {
  .dashboard-layout { flex-direction: column; }
  .sidebar { width: 100%; border-right: none; border-bottom: 1px solid #e5e7eb; padding: 20px; }
  .sidebar-nav { flex-direction: row; flex-wrap: wrap; justify-content: center; }
  .nav-item { padding: 10px 15px; }
  .main-content { padding: 20px; }
  .list-card-body { flex-direction: column; align-items: flex-start; }
  .item-price.border-left { border-left: none; border-top: 1px dashed #e5e7eb; padding: 15px 0 0 0; width: 100%; text-align: left; }
  .item-actions.vertical { width: 100%; flex-direction: row; }
}
</style>