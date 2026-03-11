<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { getUnreadCountAPI } from '@/api/user' // 引入未读数 API

const router = useRouter()
const route = useRoute()

// 全局未读消息数
const unreadCount = ref(0)
let pollingTimer = null

// 获取全局未读消息数的逻辑
const fetchUnreadCount = async () => {
  const userInfoStr = localStorage.getItem('user_info')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      const res = await getUnreadCountAPI(userInfo.userId)
      unreadCount.value = res.data
    } catch (error) {
      console.error('获取全局未读消息数失败', error)
    }
  } else {
    unreadCount.value = 0 // 没登录自然没有未读
  }
}

onMounted(() => {
  fetchUnreadCount()
  // 开启全局静默轮询，每隔 5 秒向服务器查一次红点 (模拟全局实时推送)
  pollingTimer = setInterval(() => {
    fetchUnreadCount()
  }, 5000)
})

onUnmounted(() => {
  // 销毁组件时清除定时器，防止内存泄漏
  if (pollingTimer) clearInterval(pollingTimer)
})

// 当页面路由发生变化时（比如刚从聊天页面退出来），立刻查一次刷新状态
watch(
  () => route.path,
  () => {
    fetchUnreadCount()
  }
)

// 处理点击右上角用户图标的跳转逻辑
const handleUserIconClick = () => {
  const userInfo = localStorage.getItem('user_info')
  if (userInfo) {
    router.push('/profile')
  } else {
    router.push('/login')
  }
}
</script>

<template>
  <header class="navbar-wrapper">
    <div class="navbar-container">
      <div class="brand-zone">
        <div class="logo-area">
          <img src="@/assets/logo.png" alt="Logo" class="logo-img" />
          <span class="logo-text">农机共享</span>
        </div>
      </div>

      <div class="nav-menu-zone">
        <nav class="nav-links">
          <RouterLink to="/">首页</RouterLink>
          
          <div class="dropdown-wrapper">
            <a href="javascript:void(0)" class="dropdown-trigger">
              农机展厅
              <svg class="arrow-down" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd" />
              </svg>
            </a>
            
            <div class="dropdown-menu">
              <RouterLink to="/machinery-list?categoryId=1" class="dropdown-item">耕作机械</RouterLink>
              <RouterLink to="/machinery-list?categoryId=2" class="dropdown-item">种植施肥</RouterLink>
              <RouterLink to="/machinery-list?categoryId=3" class="dropdown-item">植物保护</RouterLink>
              <RouterLink to="/machinery-list?categoryId=4" class="dropdown-item">收获机械</RouterLink>
              <RouterLink to="/machinery-list?categoryId=5" class="dropdown-item">加工机械</RouterLink>
              <RouterLink to="/machinery-list?categoryId=6" class="dropdown-item">农业运输</RouterLink>
              <RouterLink to="/machinery-list" class="dropdown-item">全部设备</RouterLink>
            </div>
          </div>

          <RouterLink to="/messages" class="nav-message-item">
            消息
            <span v-if="unreadCount > 0" class="global-badge">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </span>
          </RouterLink>
          
          <RouterLink to="/publish">我要出租</RouterLink>
          <a href="#service">服务流程</a>
        </nav>

        <div class="nav-actions">
          <div class="contact-info">
            <span class="phone-label">需要租赁? 随时致电</span>
            <span class="phone-number">+86 400 123 4567</span>
          </div>
          <button class="btn-primary nav-btn" @click="router.push('/machinery-list')">浏览机型</button>
          
          <a @click.prevent="handleUserIconClick" class="btn-icon-round" title="登录/个人中心">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
            </svg>
          </a>
        </div>
      </div>
    </div>
  </header>

  <main class="main-content">
    <RouterView :key="$route.fullPath" />
  </main>

  <footer class="footer-wrapper">
    <div class="wide-container footer-container">
      <div class="footer-col brand-col">
        <div class="logo-area light">
          <span class="logo-icon">🚜</span>
          <span class="logo-text">农机共享</span>
        </div>
        <p class="footer-desc">
          打破传统农业重资产壁垒，海量现代化农机一键共享。<br>
          提升作业效率，降低耕作成本，让科技真正惠及每一寸土地。
        </p>
      </div>
      <div class="footer-col">
        <h3>快速链接</h3>
        <ul class="footer-links">
          <li><a href="#">关于我们</a></li>
          <li><a href="#">最新资讯</a></li>
        </ul>
      </div>
      <div class="footer-col">
        <h3>服务中心</h3>
        <ul class="footer-links">
          <li><a href="#">帮助中心</a></li>
          <li><a href="#">常见问题</a></li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <div class="wide-container"><p>&copy; 2026 农业器械租赁共享平台. All Rights Reserved.</p></div>
    </div>
  </footer>
</template>

<style>
/* 基础重置代码 */
html, body { margin: 0; padding: 0; width: 100%; min-height: 100vh; overflow-x: hidden; }
#app { width: 100vw !important; max-width: 100% !important; margin: 0 !important; padding: 0 !important; display: block !important; }

:root {
  --primary-color: #5465ff;
  --hover-black: #111827;
  --text-dark: #1f2937;
  --text-medium: #4b5563;
  --text-light: #9ca3af;
  --bg-light: #f3f4f6;
  --bg-dark: #111827;
  --font-family-sans: 'Inter', system-ui, sans-serif;
}

* { box-sizing: border-box; }
body { font-family: var(--font-family-sans); color: var(--text-dark); background-color: #ffffff; -webkit-font-smoothing: antialiased; }
a { text-decoration: none; color: inherit; transition: all 0.3s; }
ul { list-style: none; padding: 0; margin: 0; }
.wide-container { max-width: 1400px; width: 100%; margin: 0 auto; padding: 0 40px; }

/* 导航栏布局 */
.navbar-wrapper { position: sticky; top: 0; z-index: 1000; width: 100%; background-color: #ffffff; box-shadow: 0 2px 15px rgba(0,0,0,0.06); }
.navbar-container { display: flex; align-items: stretch; height: 80px; width: 100%; }

.brand-zone { background-color: var(--primary-color); padding: 0 50px 0 5vw; display: flex; align-items: center; clip-path: polygon(0 0, 100% 0, 85% 100%, 0% 100%); }
.logo-area { display: flex; align-items: center; gap: 10px; }
.logo-img { height: 32px; width: auto; }
.logo-text { font-size: 1.5rem; font-weight: 800; color: #ffffff; }

.nav-menu-zone { flex: 1; display: flex; justify-content: space-between; align-items: center; padding: 0 5vw 0 40px; }
.nav-links { display: flex; gap: 35px; align-items: center; height: 100%; }
.nav-links > a, .dropdown-trigger { font-weight: 600; font-size: 1rem; color: var(--text-dark); display: flex; align-items: center; height: 100%; position: relative; }
.nav-links > a:hover, .dropdown-trigger:hover { color: var(--primary-color); }

/* =========================================
   全局红点 Badge CSS (新增)
   ========================================= */
.nav-message-item {
  position: relative;
  display: flex;
  align-items: center;
}
.global-badge {
  position: absolute;
  top: 18px; /* 根据导航高度调整 */
  right: -18px;
  background-color: #ef4444; /* 醒目的红色 */
  color: white;
  font-size: 0.65rem;
  font-weight: bold;
  padding: 2px 5px;
  border-radius: 12px;
  line-height: 1;
  border: 2px solid white;
  box-shadow: 0 2px 5px rgba(239, 68, 68, 0.4);
  z-index: 10;
  transform: scale(0.9);
}

/* 下拉菜单 */
.dropdown-wrapper { position: relative; height: 100%; }
.arrow-down { width: 16px; height: 16px; margin-left: 6px; transition: transform 0.3s ease; color: var(--text-medium); }
.dropdown-wrapper:hover .arrow-down { transform: rotate(180deg); color: var(--primary-color); }

.dropdown-menu {
  position: absolute;
  top: 80px; 
  left: -20px; 
  background-color: #ffffff;
  min-width: 220px; 
  border-radius: 6px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  padding: 0; 
  opacity: 0;
  visibility: hidden;
  transform: translateY(15px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1001;
}

.dropdown-menu::before {
  content: '';
  position: absolute;
  top: -8px; 
  left: 45px;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-bottom: 8px solid var(--primary-color); 
}

.dropdown-wrapper:hover .dropdown-menu { opacity: 1; visibility: visible; transform: translateY(0); }

.dropdown-item {
  display: block;
  padding: 14px 22px; 
  color: var(--text-dark);
  font-size: 0.95rem; 
  font-weight: 600; 
  border-bottom: 1px solid #f0f0f0; 
  transition: all 0.3s ease;
}
.dropdown-item:first-child { border-radius: 6px 6px 0 0; }
.dropdown-item:last-child { border-bottom: none; border-radius: 0 0 6px 6px; }
.dropdown-item:hover { color: var(--primary-color); padding-left: 28px; background-color: #fafafa; }

/* 右侧操作区 */
.nav-actions { display: flex; align-items: center; gap: 18px; }
.contact-info { display: flex; flex-direction: column; text-align: right; }
.phone-label { font-size: 0.75rem; color: var(--text-medium); } 
.phone-number { font-weight: 800; color: var(--primary-color); font-size: 1rem; } 

.btn-primary { background-color: var(--primary-color); color: white; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; }
.btn-primary:hover { background-color: var(--hover-black); transform: translateY(-2px); }

.nav-btn { padding: 10px 22px; font-size: 0.95rem; }

.btn-icon-round {
  width: 42px; 
  height: 42px; 
  border-radius: 50%; 
  background-color: var(--primary-color); 
  color: white; 
  border: none; 
  display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 10px rgba(84, 101, 255, 0.2);
}
.btn-icon-round svg { width: 20px; height: 20px; } 
.btn-icon-round:hover { background-color: var(--hover-black); transform: translateY(-2px); box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15); }

/* 页脚代码 */
.footer-wrapper { background-color: var(--bg-dark); color: var(--text-light); padding-top: 80px; width: 100%; }
.footer-container { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 50px; padding-bottom: 60px; }
.footer-col h3 { color: #ffffff; margin-bottom: 25px; font-size: 1.2rem; font-weight: 700; }
.footer-desc { margin: 20px 0; line-height: 1.8; opacity: 0.8; font-size: 0.95rem; }
.footer-links li { margin-bottom: 12px; font-size: 0.95rem; }
.footer-links li a:hover { color: var(--primary-color); }
.footer-bottom { border-top: 1px solid rgba(255,255,255,0.1); padding: 25px 0; text-align: center; font-size: 0.9rem;}
</style>