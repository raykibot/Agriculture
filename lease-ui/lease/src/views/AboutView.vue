<template>
  <div class="about-wrapper">
    <section class="page-header">
      <div class="wide-container text-center">
        <h1 class="page-title">关于我们</h1>
        <p class="page-subtitle">致力于用科技赋能农业，连接每一台农机与每一片土地。</p>
      </div>
    </section>

    <section class="section-wrapper">
      <div class="wide-container">
        <div class="content-box">
          <div class="text-block">
            <h2>我们的使命</h2>
            <p>
              农业机械化是现代农业的基础。然而，昂贵的购置成本和季节性的闲置问题一直困扰着许多农户。"农机共享平台"应运而生，我们旨在建立一个安全、透明、高效的农机租赁市场，让先进的生产力触手可及。
            </p>
          </div>
          <div class="text-block">
            <h2>为什么选择我们？</h2>
            <ul class="feature-list">
              <li><strong>海量机型：</strong> 整合全国优质农机资源，覆盖耕种管收全流程。</li>
              <li><strong>透明定价：</strong> 拒绝隐形消费，租金、押金清晰明了。</li>
              <li><strong>安全保障：</strong> 平台提供交易担保及保险服务，租赁无忧。</li>
              <li><strong>智能匹配：</strong> 基于地理位置和作业需求，快速找到最近的设备。</li>
            </ul>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.about-wrapper { min-height: 80vh; }

.page-header {
  background-color: var(--bg-dark);
  color: white;
  padding: 100px 0;
  background-image: linear-gradient(rgba(17, 24, 39, 0.8), rgba(17, 24, 39, 0.8)), url('https://images.unsplash.com/photo-1592982537447-6f296312a022?auto=format&fit=crop&q=60&w=1920'); /* 可替换为相关背景图 */
  background-size: cover;
  background-position: center;
}
.page-title { font-size: 3rem; font-weight: 900; margin-bottom: 20px; }
.page-subtitle { font-size: 1.2rem; color: var(--text-light); max-width: 600px; margin: 0 auto; }

.content-box {
  background: white;
  padding: 60px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  max-width: 1000px;
  margin: -60px auto 0; /* 向上重叠一点 */
  position: relative;
  z-index: 10;
}

.text-block { margin-bottom: 50px; }
.text-block h2 {
  font-size: 2rem;
  color: var(--text-dark);
  margin-bottom: 25px;
  position: relative;
  padding-bottom: 15px;
}
.text-block h2::after {
    content: '';
    position: absolute;
    bottom: 0; left: 0;
    width: 60px; height: 4px;
    background-color: var(--primary-color);
    border-radius: 2px;
}

.text-block p { font-size: 1.1rem; color: var(--text-medium); line-height: 1.8; }

.feature-list { margin-top: 20px; }
.feature-list li {
    margin-bottom: 15px;
    font-size: 1.1rem;
    color: var(--text-medium);
    display: flex;
}
.feature-list li strong { color: var(--text-dark); margin-right: 10px; min-width: 100px; }

@media (max-width: 768px) {
    .page-title { font-size: 2.5rem; }
    .content-box { padding: 30px; margin-top: 0; border-radius: 0; box-shadow: none; }
    .feature-list li { flex-direction: column; }
}
</style>