<script setup>
import { ref } from 'vue'

// 模拟农机数据
const equipments = ref([
  { id: 1, name: '东方红 LX2204 大马力拖拉机', image: 'https://images.unsplash.com/photo-1592982537447-6f296312a022?auto=format&fit=crop&q=80&w=800', rating: '5.0', year: '2023', type: '燃油动力', price: '500' },
  { id: 2, name: '久保田 PRO1408 收割机', image: 'https://images.unsplash.com/photo-1605615781358-1db2a197772d?auto=format&fit=crop&q=80&w=800', rating: '4.9', year: '2024', type: '油电混动', price: '800' },
  { id: 3, name: '大疆 T60 农业植保无人机', image: 'https://images.unsplash.com/photo-1625246333195-78d9c38ad449?auto=format&fit=crop&q=80&w=800', rating: '5.0', year: '2025', type: '纯电续航', price: '300' },
  { id: 4, name: '雷沃谷神 轮式谷物联合收割机', image: 'https://images.unsplash.com/photo-1625246333195-78d9c38ad449?auto=format&fit=crop&q=80&w=800', rating: '4.8', year: '2023', type: '高效柴油', price: '750' }
])
</script>

<template>
  <div>
    <section class="hero-wrapper">
      <div class="hero-content wide-container">
        <div class="text-content">
          <span class="hero-badge">📢 春耕特惠：新用户首单 8 折起！</span>
          <h1 class="hero-title">
            让现代科技<br>
            <span class="highlight">耕耘您的每一寸土地</span>
          </h1>
          <p class="hero-subtitle">
            打破重资产壁垒，一键租赁专业农机。从播种到丰收，我们提供全流程机械化解决方案，助力农业生产降本增效。
          </p>
          <div class="hero-actions">
            <button class="btn-primary large-btn">立即预订农机</button>
            <button class="btn-secondary large-btn">了解服务流程</button>
          </div>
        </div>
        </div>
    </section>

    <section id="machinery" class="section-wrapper bg-light">
      <div class="wide-container">
        <div class="section-header">
          <span class="section-tag">FEATURED FLEETS</span>
          <h2 class="section-title">精选热门租赁机型</h2>
        </div>

        <div class="card-grid">
          <div class="equipment-card" v-for="item in equipments" :key="item.id">
            <div class="card-image">
              <img :src="item.image" :alt="item.name" />
              <span class="card-badge">{{ item.year }}款</span>
            </div>
            <div class="card-body">
              <div class="card-top">
                <h3 class="card-name">{{ item.name }}</h3>
                <div class="card-rating">⭐ {{ item.rating }}</div>
              </div>
              <div class="card-specs">
                <span> {{ item.year }}年产</span>
                <span> {{ item.type }}</span>
                <span> 智能辅助</span>
              </div>
              <div class="card-footer">
                <div class="price-box">
                  <span class="currency">￥</span>
                  <span class="amount">{{ item.price }}</span>
                  <span class="unit">/天起</span>
                </div>
                <button class="btn-icon-primary">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-6 h-6">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section id="service" class="section-wrapper">
      <div class="wide-container">
        <div class="section-header text-center">
          <span class="section-tag">HOW IT WORKS</span>
          <h2 class="section-title">简单四步，轻松作业</h2>
        </div>

        <div class="process-steps">
          <div class="step-item">
            <div class="step-icon-box">
              <span class="step-number">01</span>
              <span class="step-emoji">📍</span>
            </div>
            <h3>定位作业需求</h3>
            <p>提交您的农田位置、作业类型（耕/种/收）及预计时间。</p>
          </div>
          <div class="step-connector">➔</div>
          <div class="step-item">
            <div class="step-icon-box active">
              <span class="step-number">02</span>
              <span class="step-emoji">🚜</span>
            </div>
            <h3>匹配最优机型</h3>
            <p>系统智能推荐附近最适合的农机设备，一键下单预约。</p>
          </div>
           <div class="step-connector">➔</div>
          <div class="step-item">
            <div class="step-icon-box">
              <span class="step-number">03</span>
              <span class="step-emoji">💳</span>
            </div>
            <h3>安全快捷支付</h3>
            <p>在线支付租金与押金，平台担保交易，费用透明透明。</p>
          </div>
           <div class="step-connector">➔</div>
          <div class="step-item">
            <div class="step-icon-box">
              <span class="step-number">04</span>
              <span class="step-emoji">🌾</span>
            </div>
            <h3>高效进场作业</h3>
            <p>设备按时抵达，开始高效机械化作业，坐享丰收成果。</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* --- Section 通用样式 --- */
.section-wrapper {
  padding: 100px 0;
  width: 100%;
}
.bg-light { background-color: var(--bg-light); }

.section-header { margin-bottom: 50px; }
.section-header.text-center { text-align: center; }

.section-tag {
  display: block;
  color: var(--primary-color);
  font-weight: 700;
  letter-spacing: 2px;
  font-size: 0.9rem;
  margin-bottom: 10px;
  text-transform: uppercase;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-dark);
}

/* --- Hero 区域样式 --- */
.hero-wrapper {
  width: 100%;
  /* 使用深色渐变背景，模拟原图氛围 */
  background: linear-gradient(120deg, #0f172a 0%, #1e293b 100%);
  padding: 140px 0;
  color: white;
  position: relative;
  overflow: hidden;
}

/* 可以添加一个背景图层增加纹理 */
.hero-wrapper::before {
    content: '';
    position: absolute;
    top:0; left:0; right:0; bottom:0;
    background-image: url('https://images.unsplash.com/photo-1625246333195-78d9c38ad449?auto=format&fit=crop&q=60&w=2000&sat=-100&bri=-60'); /* 找一张农机图，去色变暗做背景 */
    background-size: cover;
    background-position: center;
    opacity: 0.1; /* 淡淡的纹理 */
    z-index: 0;
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
}

.text-content { max-width: 650px; }

.hero-badge {
  display: inline-block;
  background: rgba(79, 70, 229, 0.2); /* primary color transparent */
  color: #818cf8;
  padding: 8px 16px;
  border-radius: 30px;
  font-weight: 600;
  margin-bottom: 25px;
  border: 1px solid rgba(79, 70, 229, 0.3);
}

.hero-title {
  font-size: 4rem;
  line-height: 1.15;
  font-weight: 900;
  margin-bottom: 25px;
}

.highlight { color: var(--primary-color); }

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--text-light);
  margin-bottom: 45px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 20px;
}

.large-btn {
  padding: 16px 40px;
  font-size: 1.1rem;
}

/* 次要按钮样式及交互 */
.btn-secondary {
  background: transparent;
  color: white;
  border: 2px solid rgba(255,255,255,0.3);
  padding: 16px 40px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}
/* 次要按钮悬浮：变黑底白字，边框变黑 */
.btn-secondary:hover {
    background-color: var(--hover-black);
    border-color: var(--hover-black);
    color: white;
}

/* --- 卡片网格区域 --- */
.card-grid {
  display: grid;
  /* 响应式网格，卡片更宽 */
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 40px;
}

.equipment-card {
  background: white;
  border-radius: 16px; /* 更大的圆角 */
  overflow: hidden;
  /* 柔和的阴影 */
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 1px solid rgba(0,0,0,0.03);
}

/* 卡片悬浮浮动效果 */
.equipment-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}

.card-image {
  height: 240px;
  position: relative;
  overflow: hidden;
}
.card-image img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
.equipment-card:hover .card-image img { transform: scale(1.05); } /* 图片微放大 */

.card-badge {
    position: absolute;
    top: 15px; left: 15px;
    background: white;
    color: var(--text-dark);
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 700;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.card-body { padding: 25px; }

.card-top {
    display: flex;
    justify-content: space-between;
    margin-bottom: 15px;
}
.card-name { font-size: 1.3rem; font-weight: 700; }
.card-rating { color: #fbbf24; font-weight: 700; }

.card-specs {
    display: flex;
    gap: 15px;
    color: var(--text-medium);
    font-size: 0.95rem;
    margin-bottom: 25px;
}

.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 20px;
    border-top: 1px solid var(--bg-light);
}

.price-box .currency { font-size: 1.1rem; font-weight: 700; color: var(--primary-color); }
.price-box .amount { font-size: 1.8rem; font-weight: 800; color: var(--text-dark); }
.price-box .unit { color: var(--text-light); }

/* 卡片上的小图标按钮 */
.btn-icon-primary {
    background-color: var(--primary-color);
    color: white;
    border: none;
    width: 44px; height: 44px;
    border-radius: 12px;
    display: flex; align-items: center; justify-content: center;
    cursor: pointer;
    transition: background-color 0.3s ease;
}
.btn-icon-primary svg { width: 20px; height: 20px; }
/* 重点：图标按钮悬浮变黑 */
.btn-icon-primary:hover { background-color: var(--hover-black); }

/* --- 流程步骤区域 --- */
.process-steps {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 70px;
}
.step-item { text-align: center; flex: 1; padding: 0 20px; }

.step-icon-box {
    position: relative;
    width: 100px; height: 100px;
    background: white;
    border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    margin: 0 auto 30px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.05);
    border: 3px solid var(--bg-light);
    transition: all 0.3s ease;
}
/* 激活状态或悬浮状态的样式 */
.step-icon-box.active, .step-item:hover .step-icon-box {
    border-color: var(--primary-color);
    background: rgba(79, 70, 229, 0.05);
}

.step-number {
    position: absolute;
    top: -5px; right: -5px;
    background: var(--primary-color);
    color: white;
    width: 32px; height: 32px;
    border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    font-weight: 700;
    font-size: 0.9rem;
    border: 2px solid white;
}

.step-emoji { font-size: 2.5rem; }
.step-item h3 { margin-bottom: 15px; font-size: 1.2rem; font-weight: 700; }
.step-item p { color: var(--text-medium); line-height: 1.6; }

.step-connector { font-size: 2rem; color: var(--text-light); opacity: 0.5; font-weight: 300; }

/* 响应式适配 */
@media (max-width: 1024px) {
    .hero-title { font-size: 3rem; }
    .process-steps { flex-direction: column; gap: 40px; }
    .step-connector { transform: rotate(90deg); margin: 20px 0; }
}
@media (max-width: 768px) {
    .hero-wrapper { padding: 100px 0; }
    .hero-title { font-size: 2.5rem; }
    .hero-actions { flex-direction: column; }
    .large-btn { width: 100%; text-align: center; }
}
</style>