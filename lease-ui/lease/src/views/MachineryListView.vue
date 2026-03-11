<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMachineryListAPI } from '@/api/machinery'

const route = useRoute()
const router = useRouter()

const machineryList = ref([])
const loading = ref(false)

// 强转为数字类型，如果没有传则默认为 null 或 0
const currentCategoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : null)

// 调用分页接口拉取数据
const fetchMachineryList = async () => {
  loading.value = true
  try {
    // 请求第1页，12条，传入当前分类 ID
    const res = await getMachineryListAPI(1, 12, currentCategoryId.value)
    
    // 取出分页中的记录列表
    machineryList.value = res.data.records || res.data.list || []
  } catch (error) {
    console.error('获取列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 页面挂载时拉取
onMounted(() => {
  fetchMachineryList()
})

// 监听路由参数变化（在顶导点击其他分类时无缝刷新）
watch(
  () => route.query.categoryId,
  (newVal) => {
    currentCategoryId.value = newVal ? Number(newVal) : null
    fetchMachineryList()
  }
)

// 图片处理 (使用样板图)
const getFirstImage = (imagesStr) => {
  return 'http://localhost:9191/images/demo.jpg'
}

// 跳转详情页
const goToDetail = (id) => {
  router.push(`/machinery/${id}`)
}
</script>

<template>
  <div class="list-page">
    <div class="wide-container">
      
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>正在加载设备...</p>
      </div>

      <div v-else-if="machineryList.length === 0" class="empty-state">
        <div class="icon">🔍</div>
        <h3>暂无该分类下的设备</h3>
        <p>机主们正在火速上架中，敬请期待！</p>
        <button class="btn-primary" @click="router.push('/')">返回首页</button>
      </div>

      <div v-else class="machinery-grid">
        <div class="machine-card" v-for="item in machineryList" :key="item.id">
          
          <div class="img-box" @click="goToDetail(item.id)">
            <img :src="getFirstImage(item.images)" :alt="item.name" />
            <span :class="['status-badge', item.status === 0 ? 'available' : 'rented']">
              {{ item.status === 0 ? '待租' : '已租' }}
            </span>
          </div>
          
          <div class="info-box">
            <h3 class="name" :title="item.name" @click="goToDetail(item.id)">{{ item.name }}</h3>
            <p class="brand">品牌：{{ item.brand || '暂无数据' }}</p>
            
            <div class="price-row">
              <span class="price">
                <span class="symbol">¥</span>{{ item.price }}<span class="unit">/天</span>
              </span>
              <button class="btn-rent" @click="goToDetail(item.id)">立即查看</button>
            </div>
          </div>
          
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* 保持上一版的 CSS 不变 */
.list-page { background-color: #f8fafc; min-height: calc(100vh - 80px); padding: 40px 0 80px 0; }
.loading-state, .empty-state { background: white; border-radius: 12px; padding: 80px 0; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.03); }
.spinner { width: 40px; height: 40px; border: 4px solid #e2e8f0; border-top-color: var(--primary-color); border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 15px; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state .icon { font-size: 4rem; opacity: 0.5; margin-bottom: 20px; }
.empty-state h3 { font-size: 1.4rem; color: var(--text-dark); margin-bottom: 10px; }
.empty-state p { color: #94a3b8; margin-bottom: 25px; }
.empty-state .btn-primary { padding: 10px 25px; border-radius: 6px; background: var(--primary-color); color: white; border: none; cursor: pointer; }

.machinery-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.machine-card { background: white; border-radius: 12px; overflow: hidden; border: 1px solid #f1f5f9; box-shadow: 0 4px 15px rgba(0,0,0,0.04); transition: transform 0.3s ease, box-shadow 0.3s ease; display: flex; flex-direction: column; }
.machine-card:hover { transform: translateY(-8px); box-shadow: 0 12px 25px rgba(0,0,0,0.1); }
.img-box { width: 100%; height: 220px; position: relative; cursor: pointer; overflow: hidden; }
.img-box img { display: block; width: 100%; height: 100%; object-fit: cover; object-position: center; transition: transform 0.5s ease; }
.machine-card:hover .img-box img { transform: scale(1.08); }
.status-badge { position: absolute; top: 15px; right: 15px; padding: 5px 12px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; color: white; z-index: 10; box-shadow: 0 2px 6px rgba(0,0,0,0.15); }
.status-badge.available { background-color: #10b981; } 
.status-badge.rented { background-color: #f59e0b; } 
.info-box { padding: 20px; display: flex; flex-direction: column; flex: 1; }
.name { font-size: 1.15rem; font-weight: 700; color: var(--text-dark); margin: 0 0 8px 0; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.name:hover { color: var(--primary-color); }
.brand { font-size: 0.85rem; color: var(--text-medium); margin: 0 0 15px 0; }
.price-row { display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed #e2e8f0; padding-top: 15px; margin-top: auto; }
.price { color: #ef4444; font-size: 1.4rem; font-weight: 800; }
.symbol { font-size: 0.9rem; margin-right: 2px; }
.unit { font-size: 0.8rem; color: #9ca3af; font-weight: normal; margin-left: 2px; }
.btn-rent { background: #eef2ff; color: var(--primary-color); border: none; padding: 8px 16px; border-radius: 6px; font-weight: 600; font-size: 0.9rem; cursor: pointer; transition: 0.2s; }
.btn-rent:hover { background: var(--primary-color); color: white; }

@media (max-width: 1200px) { .machinery-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .machinery-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .machinery-grid { grid-template-columns: 1fr; } }
</style>