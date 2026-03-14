<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showMessage } from '@/utils/message'
import { getAddressListAPI } from '@/api/user'   
import { createOrderAPI, getPayHtmlAPI } from '@/api/order'     
import { getMachineryDetailAPI } from '@/api/machinery' 

const route = useRoute()
const router = useRouter()

// ================= 1. 接收路由参数 (交易核心数据) =================
const orderParams = ref({
  machineId: route.query.machineId || '',
  ownerId: route.query.ownerId || '',
  startDate: route.query.startDate || '',
  endDate: route.query.endDate || '',
  days: parseInt(route.query.days || 1),
  price: parseFloat(route.query.price || 0),
  deposit: parseFloat(route.query.deposit || 0)
})

// 计算金额
const rentTotal = computed(() => orderParams.value.price * orderParams.value.days)
const grandTotal = computed(() => rentTotal.value + orderParams.value.deposit)
const buyerRemark = ref('')

// ================= 2. 动态拉取商品展示信息 (新增配置参数解析) =================
const machineInfo = ref({
  name: '加载商品信息中...',
  brand: '加载中...',
  image: 'http://localhost:9191/images/demo.jpg', 
  parameters: {} // 专门用于存放机器的各种规格参数
})

const fetchMachineInfo = async () => {
  if (!orderParams.value.machineId) return
  
  try {
    const res = await getMachineryDetailAPI(orderParams.value.machineId)
    const data = res.data
    
    // 解析第一张图片
    let firstImage = 'http://localhost:9191/images/demo.jpg'
    if (data.images) {
      if (Array.isArray(data.images) && data.images.length > 0) {
        firstImage = data.images[0]
      } else if (typeof data.images === 'string') {
        firstImage = data.images.split(',')[0]
      }
    }

    // 解析规格参数 JSON
    let parsedParams = {}
    if (data.parameters) {
      if (typeof data.parameters === 'string') {
        try { parsedParams = JSON.parse(data.parameters) } catch (e) {}
      } else {
        parsedParams = data.parameters
      }
    }

    machineInfo.value = {
      name: data.name,
      brand: data.brand || '未知品牌',
      image: firstImage,
      parameters: parsedParams // 保存到响应式对象中
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
  }
}

// ================= 3. 地址簿管理逻辑 =================
const addressList = ref([])         
const selectedAddress = ref(null)   
const showAddressModal = ref(false) 

const fetchUserAddresses = async () => {
  const localUserInfo = localStorage.getItem('user_info')
  if (!localUserInfo) {
    showMessage('登录已过期，请重新登录', 'error')
    router.push('/login')
    return
  }
  const userId = JSON.parse(localUserInfo).userId
  
  try {
    const res = await getAddressListAPI(userId)
    addressList.value = res.data || []
    
    if (addressList.value.length > 0) {
      const defaultAddr = addressList.value.find(addr => addr.isDefault === 1)
      selectedAddress.value = defaultAddr || addressList.value[0]
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
  }
}

const chooseAddress = (addr) => {
  selectedAddress.value = addr
  showAddressModal.value = false
}

// ================= 4. 提交订单逻辑 (严格对接后端Result格式) =================
const isSubmitting = ref(false)
const submitOrder = async () => {
  if (!selectedAddress.value) {
    showMessage('请先选择或添加收货地址！', 'error')
    return
  }

  const userId = JSON.parse(localStorage.getItem('user_info')).userId
  isSubmitting.value = true
  
  try {
    // ⬇️ 核心修复点：这里必须是真实的字段映射，不能是那句注释
    const payload = {
      buyerId: userId,
      machineryId: orderParams.value.machineId,
      ownerId: orderParams.value.ownerId,
      addressId: selectedAddress.value.id,
      rentDays: orderParams.value.days,
      leaseStartTime: orderParams.value.startDate,
      leaseEndTime: orderParams.value.endDate,
      totalAmount: grandTotal.value,
      depositAmount: orderParams.value.deposit,
      remark: buyerRemark.value 
    }

    // 1. 发起下单请求 (Redis预扣减 -> 锁定库存 -> 生成订单号)
    const res = await createOrderAPI(payload)
    
    if (res.code === 200) {
      const orderNo = res.data // 拿到刚生成的订单流水号
      showMessage('订单生成成功，正在拉起支付宝...', 'success')
      
      // 2. 发起支付请求，换取支付宝的 HTML 表单
      const payRes = await getPayHtmlAPI(orderNo)
      
      if (payRes.code === 200) {
        // 3. 【核心绝招】直接用 document.write 渲染支付宝返回的表单代码
        // 浏览器会自动执行这段代码里的 submit()，瞬间跳转到支付宝收银台！
        document.write(payRes.data) 
      } else {
        showMessage(payRes.message, 'error')
      }
      
    } else {
      showMessage(res.message || '创建订单失败，请稍后再试', 'error')
    }
  } catch (error) {
    console.error('下单异常:', error)
    
    // 🌟 核心修复：尝试从 error 对象中提取后端真实的报错信息 (兼容不同的拦截器抛出格式)
    const errorMsg = error.message || error.msg || error.response?.data?.message || '网络或系统异常，创建订单失败'
    
    // 把硬编码替换成动态解析出来的错误信息
    showMessage(errorMsg, 'error')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  window.scrollTo({ top: 0 })
  fetchUserAddresses()
  fetchMachineInfo()
})
</script>

<template>
  <div class="confirm-page">
    <div class="wide-container align-center">
      
      <div class="checkout-wrapper">
        <h2 class="page-title">确认订单信息</h2>
        
        <div class="checkout-section address-section">
          <div class="section-header">
            <h3>收货/接机场地</h3>
            <span class="edit-link" @click="showAddressModal = true">修改地址</span>
          </div>
          
          <div v-if="selectedAddress" class="address-card" @click="showAddressModal = true">
            <div class="address-icon">📍</div>
            <div class="address-content">
              <div class="user-contact">
                <span class="user-name">{{ selectedAddress.receiverName }}</span>
                <span class="user-phone">{{ selectedAddress.receiverPhone }}</span>
                <span v-if="selectedAddress.isDefault === 1" class="tag-default">默认</span>
              </div>
              <p class="address-text">
                {{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detailAddress }}
              </p>
            </div>
            <div class="arrow-right">›</div>
          </div>

          <div v-else class="address-card empty-address" @click="router.push('/profile')">
            <div class="address-icon">➕</div>
            <div class="address-content">
              <span class="empty-text">您还没有设置收货地址，点击前往个人中心添加</span>
            </div>
            <div class="arrow-right">›</div>
          </div>
        </div>

        <div class="checkout-section integrated-section">
          <div class="section-header">
            <h3>租赁明细</h3>
          </div>
          
          <div class="item-card">
            <img :src="machineInfo.image" class="item-img" />
            <div class="item-details">
              <h4 class="item-title">{{ machineInfo.name }}</h4>
              
              <div class="item-specs-container">
                <span class="spec-tag brand-tag">品牌: {{ machineInfo.brand }}</span>
                <span class="spec-tag" v-for="(val, key) in machineInfo.parameters" :key="key">
                  {{ key }}: {{ val }}
                </span>
              </div>
              
              <div class="rent-period-box">
                <div class="period-item">
                  <span class="lbl">起租日</span>
                  <span class="val">{{ orderParams.startDate }}</span>
                </div>
                <div class="period-line">
                  <span class="line"></span>
                  <span class="days">{{ orderParams.days }} 天</span>
                  <span class="line"></span>
                </div>
                <div class="period-item">
                  <span class="lbl">结束日</span>
                  <span class="val">{{ orderParams.endDate }}</span>
                </div>
              </div>
            </div>
            <div class="item-price-side">
              <span class="price">￥{{ orderParams.price.toFixed(2) }}</span>
              <span class="unit">/天</span>
            </div>
          </div>
          
          <div class="form-row">
            <label>买家留言：</label>
            <input type="text" v-model="buyerRemark" placeholder="选填：对本次交易的说明（限50字）" maxlength="50" />
          </div>

          <div class="dashed-divider"></div>

          <div class="cost-breakdown">
            <div class="cost-row">
              <span>租赁金额 ({{ orderParams.days }} 天)</span>
              <span>￥{{ rentTotal.toFixed(2) }}</span>
            </div>
            <div class="cost-row">
              <span>设备押金 <span class="hint">(归还设备后全额退还)</span></span>
              <span>￥{{ orderParams.deposit.toFixed(2) }}</span>
            </div>
            <div class="cost-row">
              <span>运费/调度费</span>
              <span>包邮 / 免费上门</span>
            </div>
            <div class="cost-row sub-total-row">
              <span>小计：</span>
              <span class="highlight-price">￥{{ grandTotal.toFixed(2) }}</span>
            </div>
          </div>
          
        </div>

      </div>
    </div>

    <div class="bottom-action-bar">
      <div class="bar-content wide-container">
        <div class="bar-left">
          <span class="lbl">实付款：</span>
          <span class="final-price">￥{{ grandTotal.toFixed(2) }}</span>
        </div>
        <button class="btn-submit" @click="submitOrder" :disabled="isSubmitting || !selectedAddress">
          {{ isSubmitting ? '处理中...' : '提交订单' }}
        </button>
      </div>
    </div>

    <div v-if="showAddressModal" class="modal-overlay" @click.self="showAddressModal = false">
      <div class="modal-content fade-in slide-up">
        <div class="modal-header">
          <h3>选择收货地址</h3>
          <span class="close-btn" @click="showAddressModal = false">&times;</span>
        </div>
        <div class="modal-body address-scroll">
          <div v-if="addressList.length === 0" class="modal-empty">
            暂无地址，请去个人中心添加
          </div>
          <div 
            v-else
            v-for="addr in addressList" 
            :key="addr.id"
            :class="['address-select-item', { active: selectedAddress?.id === addr.id }]"
            @click="chooseAddress(addr)"
          >
            <div class="icon-radio">
              <div v-if="selectedAddress?.id === addr.id" class="inner-dot"></div>
            </div>
            <div class="addr-info">
              <div class="contact">
                <span class="name">{{ addr.receiverName }}</span>
                <span class="phone">{{ addr.receiverPhone }}</span>
                <span v-if="addr.isDefault === 1" class="tag-default">默认</span>
              </div>
              <div class="detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}</div>
            </div>
          </div>
        </div>
        <div class="modal-footer center-footer">
          <button class="btn-outline w-full" @click="router.push('/profile')">管理其他地址</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.confirm-page { background-color: #f3f4f6; min-height: calc(100vh - 80px); padding: 15px 0 70px 0; }
.align-center { display: flex; justify-content: center; }
.checkout-wrapper { width: 100%; max-width: 850px; }
.page-title { font-size: 1.2rem; font-weight: 800; color: var(--text-dark); margin: 0 0 12px 0; }

.checkout-section { background: white; border-radius: 8px; padding: 16px 20px; margin-bottom: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.section-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 12px; }
.section-header h3 { margin: 0; font-size: 0.95rem; color: var(--text-dark); font-weight: 700; }
.edit-link { color: var(--primary-color); font-size: 0.85rem; cursor: pointer; }

.address-card { display: flex; align-items: center; padding: 10px 15px; border: 1px solid #e2e8f0; border-radius: 6px; cursor: pointer; transition: 0.2s; background: #f8fafc; }
.address-card:hover { border-color: var(--primary-color); }
.address-icon { font-size: 1.3rem; margin-right: 12px; } 
.address-content { flex: 1; }
.user-contact { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; } 
.user-name { font-weight: 700; font-size: 0.95rem; color: var(--text-dark); }
.user-phone { font-weight: 600; font-size: 0.85rem; color: var(--text-medium); }
.tag-default { background: #fee2e2; color: #ef4444; font-size: 0.7rem; padding: 2px 4px; border-radius: 3px; font-weight: bold; }
.address-text { margin: 0; font-size: 0.85rem; color: #64748b; line-height: 1.4; } 
.arrow-right { font-size: 1.2rem; color: #cbd5e1; }
.empty-address { justify-content: center; text-align: center; border-style: dashed; padding: 20px; }
.empty-text { color: var(--primary-color); font-weight: 600; font-size: 0.95rem; }

.integrated-section { padding-bottom: 10px; }
.item-card { display: flex; gap: 15px; margin-bottom: 12px; }
.item-img { width: 110px; height: 80px; border-radius: 6px; object-fit: cover; border: 1px solid #f1f5f9; }
.item-details { flex: 1; }
.item-title { margin: 0 0 6px 0; font-size: 0.95rem; font-weight: 700; color: var(--text-dark); }

/* ==== 核心修改：极其精致的参数标签样式 ==== */
.item-specs-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.spec-tag {
  font-size: 0.75rem;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 3px 8px;
  border-radius: 4px;
}
.brand-tag {
  color: var(--primary-color);
  background-color: #eef2ff;
  font-weight: 600;
}

.rent-period-box { display: flex; align-items: center; background: #f8fafc; padding: 6px 12px; border-radius: 4px; width: fit-content; }
.period-item { display: flex; flex-direction: column; text-align: center; }
.period-item .lbl { font-size: 0.7rem; color: #94a3b8; margin-bottom: 2px; }
.period-item .val { font-size: 0.85rem; font-weight: 700; color: var(--text-dark); }
.period-line { display: flex; align-items: center; margin: 0 10px; }
.period-line .line { width: 20px; height: 1px; background: #cbd5e1; }
.period-line .days { font-size: 0.75rem; color: var(--primary-color); font-weight: bold; margin: 0 6px; }

.item-price-side { text-align: right; }
.item-price-side .price { display: block; font-size: 1.05rem; font-weight: 800; color: var(--text-dark); }
.item-price-side .unit { font-size: 0.75rem; color: #94a3b8; }

.form-row { display: flex; align-items: center; }
.form-row label { font-size: 0.85rem; font-weight: 600; color: var(--text-dark); width: 70px; }
.form-row input { flex: 1; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 4px; font-size: 0.85rem; outline: none; transition: 0.2s; background: #fdfdfd; }
.form-row input:focus { border-color: var(--primary-color); background: white; }

.dashed-divider { height: 1px; border-bottom: 1px dashed #cbd5e1; margin: 15px 0; }

.cost-breakdown { display: flex; flex-direction: column; gap: 6px; }
.cost-row { display: flex; justify-content: space-between; font-size: 0.85rem; color: var(--text-medium); }
.cost-row .hint { font-size: 0.75rem; color: #94a3b8; margin-left: 4px; }
.sub-total-row { margin-top: 4px; padding-top: 8px; border-top: 1px solid #f1f5f9; font-size: 0.95rem; font-weight: 700; color: var(--text-dark); align-items: center; }
.highlight-price { font-size: 1.3rem; color: #ef4444; font-weight: 800; }

.bottom-action-bar { position: fixed; bottom: 0; left: 0; width: 100%; height: 55px; background: white; box-shadow: 0 -2px 10px rgba(0,0,0,0.05); display: flex; align-items: center; z-index: 1000; }
.bar-content { display: flex; justify-content: flex-end; align-items: center; width: 100%; max-width: 850px; margin: 0 auto; }
.bar-left { display: flex; align-items: baseline; margin-right: 20px; }
.bar-left .lbl { font-size: 0.9rem; color: var(--text-dark); font-weight: 600; }
.bar-left .final-price { font-size: 1.4rem; color: #ef4444; font-weight: 900; }
.btn-submit { background: var(--primary-color); color: white; border: none; padding: 0 30px; height: 38px; border-radius: 19px; font-size: 0.95rem; font-weight: 700; cursor: pointer; transition: 0.3s; box-shadow: 0 2px 10px rgba(84, 101, 255, 0.2); }
.btn-submit:hover:not(:disabled) { background: var(--hover-black); transform: translateY(-1px); }
.btn-submit:disabled { background: #94a3b8; cursor: not-allowed; box-shadow: none; }

.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.5); display: flex; justify-content: center; align-items: center; z-index: 2000; }
.modal-content { background: #ffffff; width: 440px; border-radius: 12px; overflow: hidden; display: flex; flex-direction: column; max-height: 80vh; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.1rem; color: var(--text-dark); font-weight: 800; }
.close-btn { font-size: 1.5rem; color: #94a3b8; cursor: pointer; line-height: 1; }
.close-btn:hover { color: #ef4444; }

.address-scroll { flex: 1; overflow-y: auto; padding: 10px 0; background: #f8fafc; }
.address-select-item { display: flex; align-items: center; padding: 15px 20px; background: white; border-bottom: 1px solid #f1f5f9; cursor: pointer; transition: 0.2s; }
.address-select-item:hover { background: #fdfdfd; }
.address-select-item.active { background: #eef2ff; }

.icon-radio { width: 18px; height: 18px; border-radius: 50%; border: 1px solid #cbd5e1; margin-right: 15px; display: flex; align-items: center; justify-content: center; }
.active .icon-radio { border-color: var(--primary-color); }
.inner-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--primary-color); }

.addr-info { flex: 1; }
.addr-info .contact { margin-bottom: 4px; display: flex; align-items: center; gap: 10px;}
.addr-info .name { font-weight: 700; font-size: 0.95rem; color: var(--text-dark); }
.addr-info .phone { font-size: 0.85rem; color: var(--text-medium); font-weight: 600;}
.addr-info .detail { font-size: 0.8rem; color: #64748b; line-height: 1.4; }

.modal-empty { padding: 40px; text-align: center; color: var(--text-medium); font-size: 0.9rem; }
.modal-footer.center-footer { padding: 15px; background: white; border-top: 1px solid #f1f5f9; display: flex; justify-content: center; }
.w-full { width: 100%; border: 1px solid var(--primary-color); color: var(--primary-color); background: white; padding: 10px; border-radius: 6px; cursor: pointer; font-weight: 600; }
.w-full:hover { background: var(--primary-color); color: white; }

@media (max-width: 768px) {
  .checkout-section { padding: 12px 15px; }
  .item-card { flex-direction: column; gap: 10px; }
  .item-price-side { text-align: left; }
  .bar-content { padding: 0 15px; justify-content: space-between; }
  .modal-content { width: 100%; height: 60vh; position: absolute; bottom: 0; border-radius: 16px 16px 0 0; }
}
</style>