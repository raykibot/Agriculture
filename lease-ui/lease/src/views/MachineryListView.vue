<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMachineryListAPI } from '@/api/machinery' // 引入封装好的接口

// 搜索和排序状态
const searchQuery = ref('')
const sortBy = ref('default')
const loading = ref(true)

// 存放接口返回的真实数据
const equipments = ref([])

// 在组件挂载时请求后端接口
onMounted(async () => {
  await fetchMachineryList()
})

// 获取农机列表数据 (使用 axios 封装的 API)
const fetchMachineryList = async () => {
  loading.value = true
  try {
    // 直接调用 API，响应拦截器已经帮我们去除了最外层的 response，直接拿到了 resData
    const resData = await getMachineryListAPI()
    
    // 将后端的 records 数组赋值给前端响应式变量
    equipments.value = resData.data.records
  } catch (error) {
    // 这里的 error 已经被拦截器处理过，如果是业务报错可以直接拿到 message
    console.error('获取农机列表失败:', error.message)
    // 接口报错时，提供一个后备的测试数据以防白屏
    loadFallbackData()
  } finally {
    loading.value = false
  }
}

// 模拟图片处理函数：开发阶段写死为一个随机农业相关的图片
const getImageUrl = (imageStr, id) => {
  return `https://images.unsplash.com/photo-1592982537447-6f296312a022?auto=format&fit=crop&q=80&w=800&sig=${id}`
}

// 限制参数字典只显示前 4 个，避免卡片被撑得太长
const getTopParameters = (params) => {
  if (!params) return []
  return Object.entries(params).slice(0, 4)
}

// 计算属性：前端搜索与排序过滤
const filteredAndSortedEquipments = computed(() => {
  let result = equipments.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(item => 
      item.name.toLowerCase().includes(query) || 
      item.brand.toLowerCase().includes(query)
    )
  }

  if (sortBy.value === 'price-asc') {
    result = [...result].sort((a, b) => a.price - b.price)
  } else if (sortBy.value === 'price-desc') {
    result = [...result].sort((a, b) => b.price - a.price)
  }

  return result
})

// 防止后端未启动时的后备测试数据
const loadFallbackData = () => {
  equipments.value = [
    {
      id: 4,
      name: "东方红LX2204轮式拖拉机",
      brand: "东方红",
      model: "LX2204",
      price: 450.00,
      description: "大马力重型拖拉机，适合大面积开荒与深耕作业。",
      parameters: { "马力": "220HP", "驱动": "四轮驱动", "燃油": "400L", "间隙": "450mm" },
      avgRating: 5.0
    }
  ]
}
</script>

<template>
  <div class="list-page-wrapper bg-light">
    <div class="wide-container">
      
      <div class="action-bar">
        <div class="search-box">
          <input type="text" v-model="searchQuery" placeholder="搜索农机名称或品牌..." />
          <button class="search-btn">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
            </svg>
          </button>
        </div>

        <div class="sort-box">
          <select v-model="sortBy">
            <option value="default">默认排序</option>
            <option value="price-asc">日租金由低到高</option>
            <option value="price-desc">日租金由高到低</option>
          </select>
        </div>
      </div>

      <div v-if="loading" class="empty-state">
        <p>正在努力加载农机数据中...</p>
      </div>

      <div v-else class="cards-grid">
        <div class="equip-card" v-for="item in filteredAndSortedEquipments" :key="item.id">
          
          <div class="card-img-box">
            <img :src="getImageUrl(item.images, item.id)" :alt="item.name" />
          </div>
          
          <div class="card-info">
            <div class="card-header">
              <h3 class="card-title">{{ item.name }}</h3>
              <span class="star-rating">
                ⭐ {{ item.avgRating ? item.avgRating.toFixed(1) : '5.0' }}
              </span>
            </div>
            
            <p class="card-desc">{{ item.description }}</p>

            <div class="specs-grid">
              <span class="spec-item"><span class="icon">🏷️</span> 品牌: {{ item.brand }}</span>
              <span class="spec-item"><span class="icon">🔖</span> 型号: {{ item.model }}</span>
              
              <span class="spec-item" v-for="([key, val]) in getTopParameters(item.parameters)" :key="key">
                <span class="icon">⚙️</span> {{ key }}: {{ val }}
              </span>
            </div>
            
            <div class="card-footer-action">
              <div class="price-display">
                <span class="currency">￥</span><span class="num">{{ item.price }}</span><span class="unit"> / 天</span>
              </div>
              
              <div class="btn-group">
                <button class="btn-primary rounded-pill">立即预订</button>
              </div>
            </div>
            
          </div>
        </div>
        
        <div v-if="filteredAndSortedEquipments.length === 0 && !loading" class="empty-state">
          <p>抱歉，没有找到符合条件的农机设备。</p>
        </div>
      </div>
      
    </div>
  </div>
</template>

<style scoped>
/* 样式部分保持与之前一致，只增加了卡片描述文字的样式 */
.bg-light { background-color: #f8fafc; }
.list-page-wrapper { min-height: calc(100vh - 80px); padding: 40px 0 80px; }

.action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.search-box { position: relative; width: 100%; max-width: 350px; }
.search-box input { width: 100%; padding: 12px 40px 12px 18px; border: 1px solid #e2e8f0; border-radius: 10px; font-size: 0.95rem; color: var(--text-dark); outline: none; transition: all 0.3s; background-color: white; }
.search-box input:focus { border-color: var(--primary-color); box-shadow: 0 0 0 3px rgba(84, 101, 255, 0.1); }
.search-btn { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: transparent; border: none; color: var(--primary-color); cursor: pointer; }
.search-btn svg { width: 18px; height: 18px; }

.sort-box select { padding: 12px 35px 12px 18px; border: 1px solid #e2e8f0; border-radius: 10px; font-size: 0.95rem; color: var(--text-dark); outline: none; cursor: pointer; background-color: white; appearance: none; background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%234b5563'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 12px center; background-size: 14px; }

.cards-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 25px; }
.equip-card { background: white; border-radius: 14px; overflow: hidden; border: 1px solid #f1f5f9; box-shadow: 0 8px 20px rgba(0,0,0,0.03); transition: all 0.3s ease; display: flex; flex-direction: column; }
.equip-card:hover { transform: translateY(-6px); box-shadow: 0 15px 30px rgba(0,0,0,0.08); }

.card-img-box { height: 200px; overflow: hidden; }
.card-img-box img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
.equip-card:hover .card-img-box img { transform: scale(1.05); }

.card-info { padding: 20px; display: flex; flex-direction: column; flex: 1; }
.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
.card-title { font-size: 1.15rem; font-weight: 700; color: var(--text-dark); line-height: 1.4; }
.star-rating { color: #fbbf24; font-weight: 700; font-size: 0.9rem; flex-shrink: 0;}

/* 新增的描述文字样式 */
.card-desc { font-size: 0.85rem; color: var(--text-medium); margin-bottom: 15px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.specs-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #f1f5f9; }
.spec-item { color: var(--text-medium); font-size: 0.8rem; display: flex; align-items: center; gap: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;}
.spec-item .icon { color: var(--primary-color); opacity: 0.8; }

.card-footer-action { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }
.price-display .currency { color: var(--primary-color); font-weight: 600; font-size: 1rem;}
.price-display .num { font-size: 1.5rem; font-weight: 800; color: var(--primary-color); }
.price-display .unit { font-size: 0.85rem; color: var(--text-medium); }

.btn-group { display: flex; align-items: center; gap: 10px; }
.rounded-pill { border-radius: 30px; padding: 10px 18px; font-size: 0.85rem; font-weight: 600; border: none; background-color: var(--primary-color); color: white; cursor: pointer; transition: 0.3s; }
.rounded-pill:hover { background-color: var(--hover-black); }

.empty-state { grid-column: 1 / -1; text-align: center; padding: 60px 0; color: var(--text-medium); font-size: 1.1rem; }

@media (max-width: 1024px) { .cards-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .cards-grid { grid-template-columns: 1fr; } .action-bar { flex-direction: column; gap: 15px; align-items: stretch; } .search-box { max-width: 100%; } }
</style>