<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPopularMachineryAPI } from '@/api/machinery'

const router = useRouter()
const popularList = ref([])

// 拉取热门机型
const fetchPopular = async () => {
  try {
    const res = await getPopularMachineryAPI()
    popularList.value = res.data
  } catch (error) {
    console.error('获取热门机型失败', error)
  }
}

onMounted(() => {
  fetchPopular()
})

// 处理图片字符串 (强制使用你的 800*800 demo.jpg)
const getFirstImage = (imagesStr) => {
  if (!imagesStr) {
    // 兜底机制：如果数据库里没有图片，给一张高级灰的占位图防止页面错乱
    return 'https://via.placeholder.com/800x800?text=暂无实拍图'
  }
  // 后端传过来的是类似 "https://oss...1.jpg,https://oss...2.jpg"
  // 我们用逗号切开变成数组，只取第 0 个元素（第一张主图）展示在首页
  return imagesStr.split(',')[0]
}

// 跳转到详情页
const goToDetail = (id) => {
   router.push(`/machinery/${id}`)
  //alert('详情页正在火热开发中...')
}
</script>

<template>
  <div class="home-page">
    <section class="hero-banner">
      <div class="wide-container banner-content">
        <h1>共享农机，助力现代农业</h1>
        <p>打破重资产壁垒，海量现代化农机一键租赁，让科技惠及每一寸土地。</p>
        <div class="banner-actions">
          <button class="btn-primary" @click="router.push('/machinery-list')">浏览全部设备</button>
          <button class="btn-outline" @click="router.push('/publish')">我有设备出租</button>
        </div>
      </div>
    </section>

    <section class="popular-section">
      <div class="wide-container">
        <h2 class="section-title">精选热门租赁机型</h2>
        <p class="section-subtitle">从耕作到收获，全面覆盖您的农业生产需求</p>
        
        <div class="machinery-grid">
          <div class="machine-card" v-for="item in popularList" :key="item.id">
            
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
    </section>
  </div>
</template>

<style scoped>
.home-page {
  background-color: #f8fafc;
  min-height: 100vh;
  padding-bottom: 60px;
}

/* ================= Banner 样式 ================= */
.hero-banner {
  background: linear-gradient(to right, rgba(17,24,39,0.85), rgba(84, 101, 255, 0.7)), url('https://images.unsplash.com/photo-1625246333195-78d9c38ad449?auto=format&fit=crop&q=80&w=1600') center/cover;
  padding: 80px 0;
  color: white;
  text-align: center;
}
.banner-content h1 { font-size: 3rem; font-weight: 800; margin-bottom: 20px; }
.banner-content p { font-size: 1.2rem; opacity: 0.9; margin-bottom: 30px; }
.banner-actions { display: flex; justify-content: center; gap: 20px; }
.btn-primary { background: var(--primary-color); color: white; border: none; padding: 12px 30px; border-radius: 8px; font-size: 1.1rem; cursor: pointer; transition: 0.3s; }
.btn-primary:hover { background: var(--hover-black); }
.btn-outline { background: transparent; color: white; border: 2px solid white; padding: 12px 30px; border-radius: 8px; font-size: 1.1rem; cursor: pointer; transition: 0.3s; }
.btn-outline:hover { background: white; color: var(--text-dark); }

/* ================= 热门设备区块 ================= */
.popular-section {
  padding: 60px 0;
}
.section-title { text-align: center; font-size: 2rem; color: var(--text-dark); font-weight: 800; margin-bottom: 10px; }
.section-subtitle { text-align: center; color: var(--text-medium); margin-bottom: 40px; }

/* ================= 核心：一行三列紧凑卡片 ================= */
.machinery-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); 
  gap: 24px; 
}

.machine-card {
  background: white;
  border-radius: 12px;
  overflow: hidden; /* 保证铺满的图片不会超出卡片的圆角 */
  border: 1px solid #f1f5f9; 
  box-shadow: 0 4px 15px rgba(0,0,0,0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
}
.machine-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 25px rgba(0,0,0,0.1);
}

/* ================= 图片区域：无死角铺满 ================= */
.img-box {
  width: 100%;
  height: 240px; /* 增加高度，给无人机叶片留出足够的空间 */
  position: relative;
  cursor: pointer;
  overflow: hidden;
}
.img-box img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover; /* 核心：让图片像壁纸一样死死贴住边缘，绝不留白 */
  object-position: center; /* 确保画面的视觉中心（无人机）始终在正中间 */
  transition: transform 0.5s ease;
}
.machine-card:hover .img-box img {
  transform: scale(1.08); /* 悬浮时略微放大，增加互动感 */
}

/* 状态角标 */
.status-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  color: white;
  z-index: 10;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15); 
}
.status-badge.available { background-color: #10b981; } 
.status-badge.rented { background-color: #f59e0b; } 

/* ================= 信息区域 ================= */
.info-box {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1; /* 让信息区撑开剩下的空间 */
}
.name { 
  font-size: 1.15rem; 
  font-weight: 700; 
  color: var(--text-dark); 
  margin: 0 0 8px 0; 
  cursor: pointer;
  white-space: nowrap; 
  overflow: hidden; 
  text-overflow: ellipsis; 
}
.name:hover { color: var(--primary-color); }
.brand { 
  font-size: 0.85rem; 
  color: var(--text-medium); 
  margin: 0 0 15px 0; 
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed #e2e8f0; 
  padding-top: 15px;
  margin-top: auto; /* 把价格行推到卡片最底部 */
}
.price { 
  color: #ef4444; 
  font-size: 1.4rem; 
  font-weight: 800; 
}
.symbol { font-size: 0.9rem; margin-right: 2px; }
.unit { font-size: 0.8rem; color: #9ca3af; font-weight: normal; margin-left: 2px; }

.btn-rent {
  background: #eef2ff;
  color: var(--primary-color);
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-rent:hover {
  background: var(--primary-color);
  color: white;
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .machinery-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .machinery-grid { grid-template-columns: 1fr; }
}
</style>