<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  logoutAPI, getUserProfileAPI, updateUserProfileAPI,
  getAddressListAPI, addAddressAPI, setDefaultAddressAPI, deleteAddressAPI 
} from '@/api/user' 
import { showMessage } from '@/utils/message'

const router = useRouter()
const currentTab = ref('info') 

// ================= 1. 个人基础信息与地址数据 =================
const userInfo = ref({
  name: '加载中...',
  phone: '加载中...',
  idCard: '加载中...',
  status: '加载中...',
  regDate: '加载中...',
  address: '加载中...' 
})

const addressList = ref([]) 

// ================= 2. 获取数据逻辑 =================
const fetchUserProfileAndAddresses = async () => {
  const localUserInfo = localStorage.getItem('user_info')
  if (!localUserInfo) {
    showMessage('登录已过期，请重新登录', 'error')
    router.push('/login')
    return
  }
  const parsedUser = JSON.parse(localUserInfo)
  try {
    const [profileRes, addressRes] = await Promise.all([
      getUserProfileAPI(parsedUser.userId),
      getAddressListAPI(parsedUser.userId)
    ])
    
    const data = profileRes.data
    userInfo.value = {
      name: data.realName,
      phone: data.phone,
      idCard: data.idCard,
      status: data.status,
      regDate: data.regDate,
      address: data.defaultAddress
    }
    
    addressList.value = addressRes.data || []
  } catch (error) {
    console.error('获取信息失败:', error)
  }
}

onMounted(() => {
  fetchUserProfileAndAddresses()
})

// ================= 3. 编辑基础资料 (实名认证) =================
const showEditProfileModal = ref(false)
const profileForm = ref({ realName: '', idCard: '' })
const isSubmittingProfile = ref(false)

const openProfileModal = () => {
  profileForm.value = { realName: '', idCard: '' }
  showEditProfileModal.value = true
}

const submitProfileUpdate = async () => {
  const userId = JSON.parse(localStorage.getItem('user_info')).userId
  isSubmittingProfile.value = true
  try {
    await updateUserProfileAPI({ userId, ...profileForm.value })
    showMessage('实名信息更新成功！', 'success')
    showEditProfileModal.value = false
    await fetchUserProfileAndAddresses()
  } catch (error) {
    console.error('更新实名失败:', error)
  } finally {
    isSubmittingProfile.value = false
  }
}

// ================= 4. 地址簿管理逻辑 =================
const showAddressModal = ref(false)
const isSubmittingAddress = ref(false)
const addressForm = ref({
  receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0
})

const openAddressModal = () => {
  addressForm.value = { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 }
  showAddressModal.value = true
}

// 修复点：添加关闭弹窗的方法
const closeAddressModal = () => {
  showAddressModal.value = false
}

const submitAddAddress = async () => {
  const userId = JSON.parse(localStorage.getItem('user_info')).userId
  isSubmittingAddress.value = true
  try {
    await addAddressAPI({ userId, ...addressForm.value })
    showMessage('新增地址成功！', 'success')
    closeAddressModal() // 提交成功后关闭
    await fetchUserProfileAndAddresses() 
  } catch (error) {
    console.error('新增地址失败:', error)
  } finally {
    isSubmittingAddress.value = false
  }
}

const setAsDefault = async (addressId) => {
  const userId = JSON.parse(localStorage.getItem('user_info')).userId
  try {
    await setDefaultAddressAPI(userId, addressId)
    showMessage('已设置为默认地址', 'success')
    await fetchUserProfileAndAddresses()
  } catch (error) {
    console.error('设置默认失败:', error)
  }
}

const deleteAddr = async (addressId) => {
  if (!confirm('确定要删除该地址吗？')) return
  const userId = JSON.parse(localStorage.getItem('user_info')).userId
  try {
    await deleteAddressAPI(userId, addressId)
    showMessage('删除成功', 'success')
    await fetchUserProfileAndAddresses()
  } catch (error) {
    console.error('删除地址失败:', error)
  }
}


// ================= 5. 模拟订单与农机数据 =================
const orderRole = ref('rented') 
const orderStatus = ref('all') 
const orders = ref([
  { id: 'ORD-20260221-01', role: 'rented', status: 'ongoing', machine: '东方红 LX2204 拖拉机', image: 'http://localhost:9191/images/demo.jpg', date: '2026-02-20', days: 3, total: 1350 },
  { id: 'ORD-20260215-02', role: 'rented', status: 'completed', machine: '大疆 T60 植保无人机', image: 'http://localhost:9191/images/demo.jpg', date: '2026-02-15', days: 1, total: 300 }
])
const filteredOrders = computed(() => {
  return orders.value.filter(o => o.role === orderRole.value && (orderStatus.value === 'all' || o.status === orderStatus.value))
})
const publishedMachines = ref([
  { id: 'M-001', name: '久保田 PRO1408 收割机', image: 'http://localhost:9191/images/demo.jpg', status: 'renting', publishTime: '2026-01-10 09:30', rentOutTime: '2026-02-21 08:00', returnTime: '2026-02-23 18:00', offlineTime: '-', price: 800 }
])
const statusMap = {
  'unpaid': { text: '待付款', color: '#f59e0b', bg: '#fef3c7' },
  'ongoing': { text: '进行中', color: '#3b82f6', bg: '#eff6ff' },
  'completed': { text: '已完成', color: '#10b981', bg: '#ecfdf5' },
  'available': { text: '闲置可租', color: '#10b981', bg: '#ecfdf5' },
  'renting': { text: '正在租赁中', color: '#3b82f6', bg: '#eff6ff' },
  'offline': { text: '已下架', color: '#6b7280', bg: '#f3f4f6' }
}

const handleLogout = async () => {
  if (!confirm('确定要退出当前账号吗？')) return
  try { await logoutAPI() } catch (e) {} 
  finally {
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_info')
    showMessage('您已安全退出登录', 'success')
    router.push('/login')
  }
}
</script>

<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="user-brief">
        <div class="avatar"><img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="Avatar" /></div>
        <h3 :class="['user-name', { 'text-danger': userInfo.name === '未绑定' }]">
          {{ userInfo.name === '未绑定' ? '未命名用户' : userInfo.name }}
        </h3>
        <span class="user-role">农户 / 租赁方</span>
      </div>

      <nav class="sidebar-nav">
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'info' }]" @click="currentTab = 'info'"><span class="icon">👤</span> 个人信息与地址</a>
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'orders' }]" @click="currentTab = 'orders'"><span class="icon">📋</span> 历史订单</a>
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'published' }]" @click="currentTab = 'published'"><span class="icon">🚜</span> 我发布的农机</a>
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'wallet' }]" @click="currentTab = 'wallet'"><span class="icon">💳</span> 钱包与资金</a>
        <a href="javascript:void(0)" :class="['nav-item', { active: currentTab === 'settings' }]" @click="currentTab = 'settings'"><span class="icon">⚙️</span> 账号设置</a>
      </nav>
      
      <div class="sidebar-footer">
        <button class="btn-logout" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <main class="main-content">
      
      <div v-if="currentTab === 'info'" class="tab-pane fade-in">
        
        <div class="stats-grid">
          <div class="stat-card"><div class="stat-num">12</div><div class="stat-label">累计租赁次数</div></div>
          <div class="stat-card"><div class="stat-num">450</div><div class="stat-label">作业亩数 (亩)</div></div>
          <div class="stat-card"><div class="stat-num">¥ 0.00</div><div class="stat-label">当前待支付</div></div>
        </div>

        <div class="content-card mt-20">
          <div class="card-header">
            <h2>基础档案</h2>
            <button class="btn-edit" @click="openProfileModal">编辑实名资料</button>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">真实姓名</span>
              <span :class="['value', { 'text-danger': userInfo.name === '未绑定' }]">
                {{ userInfo.name }}
                <span v-if="userInfo.name === '未绑定'" class="action-hint" @click="openProfileModal">(去实名绑定 ➔)</span>
              </span>
            </div>
            <div class="info-item"><span class="label">手机号码</span><span class="value">{{ userInfo.phone }}</span></div>
            <div class="info-item">
              <span class="label">身份证号码</span>
              <span :class="['value', { 'text-danger': userInfo.idCard === '未绑定' }]">
                {{ userInfo.idCard }}
                <span v-if="userInfo.idCard === '未绑定'" class="action-hint" @click="openProfileModal">(完成认证以解锁发布 ➔)</span>
              </span>
            </div>
            <div class="info-item">
              <span class="label">实名认证状态</span>
              <span :class="['value', userInfo.status === '已认证' ? 'status-verified' : 'text-danger']">
                {{ userInfo.status }} <span v-if="userInfo.status === '已认证'">✓</span>
              </span>
            </div>
            <div class="info-item"><span class="label">注册时间</span><span class="value">{{ userInfo.regDate }}</span></div>
            <div class="info-item">
              <span class="label">当前默认收货/接机地址</span>
              <span :class="['value', { 'text-danger': userInfo.address === '暂未设置默认地址' }]">
                {{ userInfo.address }}
              </span>
            </div>
          </div>
        </div>

        <div class="content-card mt-20">
          <div class="card-header">
            <h2>地址管理</h2>
            <button class="btn-primary-small" @click="openAddressModal">+ 新增地址</button>
          </div>
          
          <div class="address-list">
            <div v-if="addressList.length === 0" class="empty-state">暂无收货地址，请新增</div>
            
            <div class="address-item" v-for="addr in addressList" :key="addr.id">
              <div class="addr-info">
                <div class="addr-contact">
                  <span class="name">{{ addr.receiverName }}</span>
                  <span class="phone">{{ addr.receiverPhone }}</span>
                  <span v-if="addr.isDefault === 1" class="tag-default">默认</span>
                </div>
                <div class="addr-detail">
                  {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
                </div>
              </div>
              <div class="addr-actions">
                <button v-if="addr.isDefault === 0" class="btn-text" @click="setAsDefault(addr.id)">设为默认</button>
                <span v-if="addr.isDefault === 0" class="divider">|</span>
                <button class="btn-text text-danger" @click="deleteAddr(addr.id)">删除</button>
              </div>
            </div>
          </div>
        </div>

      </div>

      <div v-if="currentTab === 'orders'" class="tab-pane fade-in">
        <div class="content-card full-height">
          <div class="card-header border-none"><h2>订单管理</h2></div>
          <div class="role-tabs">
            <button :class="['role-tab', { active: orderRole === 'rented' }]" @click="orderRole = 'rented'">我租赁的机器</button>
            <button :class="['role-tab', { active: orderRole === 'leased' }]" @click="orderRole = 'leased'">别人租赁我的</button>
          </div>
          <div class="filter-pills">
            <span :class="['pill', { active: orderStatus === 'all' }]" @click="orderStatus = 'all'">全部</span>
            <span :class="['pill', { active: orderStatus === 'unpaid' }]" @click="orderStatus = 'unpaid'">待付款</span>
            <span :class="['pill', { active: orderStatus === 'ongoing' }]" @click="orderStatus = 'ongoing'">进行中</span>
            <span :class="['pill', { active: orderStatus === 'completed' }]" @click="orderStatus = 'completed'">已完成</span>
          </div>
          <div class="order-list">
            <div class="empty-state" v-if="filteredOrders.length === 0">暂无订单记录</div>
            <div class="list-card" v-for="order in filteredOrders" :key="order.id">
              <div class="list-card-header">
                <span class="order-id">订单号：{{ order.id }}</span>
                <span class="status-badge" :style="{ color: statusMap[order.status].color, backgroundColor: statusMap[order.status].bg }">{{ statusMap[order.status].text }}</span>
              </div>
              <div class="list-card-body">
                <img :src="order.image" class="item-img" />
                <div class="item-info">
                  <h4 class="item-title">{{ order.machine }}</h4>
                  <p class="item-desc">起租日期: {{ order.date }} &nbsp;|&nbsp; 租期: {{ order.days }} 天</p>
                </div>
                <div class="item-price">
                  <span class="label">订单总价</span><span class="amount">￥{{ order.total }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="currentTab === 'published'" class="tab-pane fade-in">
        <div class="content-card full-height">
          <div class="card-header border-none"><h2>我发布的农机</h2></div>
          <div class="machine-list">
            <div class="list-card" v-for="machine in publishedMachines" :key="machine.id">
              <div class="list-card-body">
                <img :src="machine.image" class="item-img large-img" />
                <div class="item-info expand">
                  <h4 class="item-title">{{ machine.name }}</h4>
                  <p class="item-desc">日租金: ￥{{ machine.price }} / 天</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="currentTab === 'wallet' || currentTab === 'settings'" class="tab-pane fade-in flex-center">
        <div class="empty-state"><h2>🚧 功能开发中...</h2></div>
      </div>
    </main>

    <div v-if="showEditProfileModal" class="modal-overlay">
      <div class="modal-content fade-in">
        <div class="modal-header">
          <h3>实名信息认证</h3>
          <span class="close-btn" @click="showEditProfileModal = false">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>真实姓名 <span class="required">*</span></label>
            <input type="text" v-model="profileForm.realName" placeholder="填写真实姓名以保障交易安全" />
          </div>
          <div class="form-group">
            <label>身份证号码 <span class="required">*</span></label>
            <input type="text" v-model="profileForm.idCard" placeholder="请输入18位二代身份证号码" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-outline" @click="showEditProfileModal = false">取消</button>
          <button class="btn-primary-small" @click="submitProfileUpdate" :disabled="isSubmittingProfile">
            {{ isSubmittingProfile ? '提交中...' : '保存实名信息' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showAddressModal" class="modal-overlay">
      <div class="modal-content fade-in">
        <div class="modal-header">
          <h3>新增收货地址</h3>
          <span class="close-btn" @click="closeAddressModal">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group-row">
            <div class="form-group half-width">
              <label>收货人姓名 <span class="required">*</span></label>
              <input type="text" v-model="addressForm.receiverName" placeholder="如：李先生" />
            </div>
            <div class="form-group half-width">
              <label>手机号码 <span class="required">*</span></label>
              <input type="text" v-model="addressForm.receiverPhone" placeholder="11位手机号" />
            </div>
          </div>
          <div class="form-group-row">
            <div class="form-group half-width"><label>省份</label><input type="text" v-model="addressForm.province" placeholder="例：河南省" /></div>
            <div class="form-group half-width"><label>城市</label><input type="text" v-model="addressForm.city" placeholder="例：驻马店市" /></div>
          </div>
          <div class="form-group">
            <label>所在区/县</label>
            <input type="text" v-model="addressForm.district" placeholder="例：确山县" />
          </div>
          <div class="form-group">
            <label>详细地址 <span class="required">*</span></label>
            <input type="text" v-model="addressForm.detailAddress" placeholder="街道、小区、农场具体位置" />
          </div>
          
          <div class="form-checkbox">
            <input type="checkbox" id="defaultCheck" v-model="addressForm.isDefault" :true-value="1" :false-value="0" />
            <label for="defaultCheck">将此地址设为默认地址</label>
          </div>

        </div>
        <div class="modal-footer">
          <button class="btn-outline" @click="closeAddressModal">取消</button>
          <button class="btn-primary-small" @click="submitAddAddress" :disabled="isSubmittingAddress">
            {{ isSubmittingAddress ? '保存中...' : '确认添加' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.text-danger { color: #ef4444 !important; font-weight: 700 !important; }
.action-hint { font-size: 0.8rem; color: #ef4444; margin-left: 8px; font-weight: 500; text-decoration: underline; cursor: pointer; transition: 0.2s; }
.action-hint:hover { opacity: 0.7; }

/* ================= 弹窗公共 CSS ================= */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(17, 24, 39, 0.6); backdrop-filter: blur(4px); display: flex; justify-content: center; align-items: center; z-index: 2000; }
.modal-content { background: #ffffff; width: 440px; border-radius: 12px; box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1); overflow: hidden; }
.modal-header { padding: 12px 20px; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.1rem; color: var(--text-dark); font-weight: 800; }
.close-btn { font-size: 1.4rem; color: #94a3b8; cursor: pointer; transition: color 0.2s; line-height: 1; }
.close-btn:hover { color: #ef4444; }

.modal-body { padding: 15px 20px; } 
.form-group { margin-bottom: 12px; display: flex; flex-direction: column; } 
.form-group-row { display: flex; gap: 12px; } 
.half-width { flex: 1; } 
.form-group label { font-size: 0.8rem; font-weight: 600; color: var(--text-medium); margin-bottom: 4px; } 
.form-group .required { color: #ef4444; }
.form-group input[type="text"] { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.85rem; outline: none; transition: border-color 0.2s; } 
.form-group input[type="text"]:focus { border-color: var(--primary-color); }

.form-checkbox { display: flex; align-items: center; margin-top: 5px; cursor: pointer; }
.form-checkbox input { margin-right: 8px; width: 16px; height: 16px; cursor: pointer; accent-color: var(--primary-color); }
.form-checkbox label { font-size: 0.85rem; color: var(--text-dark); font-weight: 600; cursor: pointer; }

.modal-footer { padding: 10px 20px; background: #f8fafc; display: flex; justify-content: flex-end; gap: 10px; border-top: 1px solid #f1f5f9; }
.modal-footer .btn-primary-small, .modal-footer .btn-outline { padding: 6px 16px; font-size: 0.85rem; }

/* ================= 地址簿列表样式 ================= */
.mt-20 { margin-top: 20px; }
.address-list { display: flex; flex-direction: column; gap: 15px; }
.address-item { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; border: 1px solid #e2e8f0; border-radius: 8px; background: #fdfdfd; transition: 0.2s; }
.address-item:hover { border-color: #cbd5e1; background: #ffffff; box-shadow: 0 4px 10px rgba(0,0,0,0.02); }
.addr-info { flex: 1; }
.addr-contact { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.addr-contact .name { font-size: 1rem; font-weight: 700; color: var(--text-dark); }
.addr-contact .phone { font-size: 0.9rem; font-weight: 600; color: var(--text-medium); }
.tag-default { background: #fee2e2; color: #ef4444; font-size: 0.7rem; padding: 2px 6px; border-radius: 4px; font-weight: bold; }
.addr-detail { font-size: 0.85rem; color: #64748b; line-height: 1.4; }
.addr-actions { display: flex; align-items: center; gap: 10px; }
.btn-text { background: transparent; border: none; color: var(--primary-color); font-size: 0.85rem; font-weight: 600; cursor: pointer; transition: 0.2s; }
.btn-text:hover { opacity: 0.7; }
.divider { color: #cbd5e1; font-size: 0.8rem; }

/* 保持全局样式不变 */
.dashboard-layout { display: flex; min-height: calc(100vh - 80px); background-color: #f3f4f6; width: 100%; }
.sidebar { width: 280px; background: white; border-right: 1px solid #e5e7eb; display: flex; flex-direction: column; padding: 30px 20px; flex-shrink: 0; }
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
.main-content { flex: 1; padding: 40px; overflow-y: auto; }
.tab-pane { display: flex; flex-direction: column; height: 100%; }
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
.item-price { text-align: right; padding-right: 20px; display: flex; flex-direction: column; justify-content: center; }
.item-price .label { font-size: 0.85rem; color: var(--text-light); }
.item-price .amount { font-size: 1.4rem; font-weight: 800; color: var(--text-dark); }
.empty-state { text-align: center; padding: 60px 0; color: var(--text-medium); }
.flex-center { justify-content: center; align-items: center; }
</style>