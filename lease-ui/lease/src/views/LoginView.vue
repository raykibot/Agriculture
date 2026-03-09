<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { loginAPI, registerAPI } from '@/api/user' // 引入后端接口
import { showMessage } from '@/utils/message'

const isLogin = ref(true)
const router = useRouter()

// 表单数据双向绑定
const formData = reactive({
  phone: '',
  username: '', // 注册需要的用户名
  password: '',
  confirmPassword: ''
})

// 切换登录/注册模式
const toggleMode = () => { 
  isLogin.value = !isLogin.value 
  // 切换模式时清空表单，避免数据残留
  formData.phone = ''
  formData.username = ''
  formData.password = ''
  formData.confirmPassword = ''
}

// 手机号正则校验 (中国大陆11位手机号)
const isValidPhone = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

// 提交表单
const handleSubmit = async (e) => {
  e.preventDefault()

  // 1. 手机号基础校验
  if (!isValidPhone(formData.phone)) {
    showMessage('请输入有效的11位手机号码！', 'warning')
    return
  }
  
  if (isLogin.value) {
    // ================= 登录逻辑 =================
    try {
      const res = await loginAPI({
        phone: formData.phone,
        password: formData.password
      })
      
      // 登录成功，将后端返回的 token 存入 localStorage
      // 你的后端返回结构是 data: { token: "xxx", userId: 1, username: "xxx" }
      localStorage.setItem('user_token', res.data.token)
      localStorage.setItem('user_info', JSON.stringify({
        userId: res.data.userId,
        username: res.data.username
      }))

      showMessage('登录成功！即将跳转...', 'success')
      setTimeout(() => {
        router.push('/profile')
      }, 1000)
      
    } catch (error) {
      // 👇 4. 替换 alert
      showMessage(error.message || '登录失败，请检查账号和密码', 'error')
    }

  } else {
    // ================= 注册逻辑 =================
    // 注册特有校验
    if (!formData.username.trim()) {
      alert('请输入用户名！')
      return
    }
    if (formData.password !== formData.confirmPassword) {
      alert('两次输入的密码不一致！')
      return
    }

    try {
      await registerAPI({
        phone: formData.phone,
        username: formData.username,
        password: formData.password
      })
      
      showMessage('注册成功，请登录！', 'success')
      isLogin.value = true // 注册成功后，自动切换回登录面板
      
    } catch (error) {
      showMessage(error.message || '注册失败', 'error')
    }
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-box">
      
      <div class="auth-banner">
        <div class="banner-content">
          <h2>{{ isLogin ? '欢迎回来' : '加入我们' }}</h2>
          <p>高效调度，轻松流转，开启您的现代农业机械化新体验。</p>
        </div>
      </div>
      
      <div class="auth-form-wrapper">
        <div class="form-inner">
          <h2 class="form-title">{{ isLogin ? '登录账号' : '注册新账号' }}</h2>
          <p class="form-subtitle">
            {{ isLogin ? '请输入您的凭据以访问您的账户' : '填写下方信息成为平台认证用户' }}
          </p>

          <form class="auth-form" @submit="handleSubmit">
            
            <div class="input-group">
              <label>手机号</label>
              <input type="text" v-model="formData.phone" placeholder="请输入11位手机号" required maxlength="11" />
            </div>

            <div class="input-group" v-if="!isLogin">
              <label>用户名</label>
              <input type="text" v-model="formData.username" placeholder="请设置您的昵称" required />
            </div>

            <div class="input-group">
              <label>密码</label>
              <input type="password" v-model="formData.password" placeholder="请输入密码" required />
            </div>

            <div class="input-group" v-if="!isLogin">
              <label>确认密码</label>
              <input type="password" v-model="formData.confirmPassword" placeholder="请再次输入密码" required />
            </div>

            <div class="form-actions" v-if="isLogin">
              <label class="remember-me">
                <input type="checkbox" /> 记住我
              </label>
              <a href="#" class="forgot-pwd">忘记密码？</a>
            </div>

            <button type="submit" class="btn-primary w-full">
              {{ isLogin ? '立即登录' : '注册并返回登录' }}
            </button>
          </form>

          <div class="switch-mode">
            {{ isLogin ? '还没有账号？' : '已有账号？' }}
            <span @click="toggleMode" class="link-primary">{{ isLogin ? '立即注册' : '返回登录' }}</span>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* 样式与上一版保持完全一致，完美铺满全屏，适配滚动 */
.auth-container {
  min-height: calc(100vh - 80px); 
  width: 100%;
  background-color: #ffffff;
  display: flex;
}

.auth-box {
  display: flex;
  width: 100%;
  height: auto; 
  min-height: 100%; 
  max-width: 100%;
  background: white;
  border-radius: 0; 
  box-shadow: none; 
}

.auth-banner {
  flex: 1; 
  background: linear-gradient(to right, rgba(17,24,39,0.85), rgba(84, 101, 255, 0.7)), url('https://images.unsplash.com/photo-1625246333195-78d9c38ad449?auto=format&fit=crop&q=80&w=1200') center/cover;
  padding: 60px; 
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: flex-end; 
}

.banner-content h2 { font-size: 2.5rem; margin-bottom: 15px; font-weight: 800; }
.banner-content p { font-size: 1.1rem; line-height: 1.6; opacity: 0.9; max-width: 500px; }

.auth-form-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center; 
  background-color: white;
  padding: 40px 0; 
}

.form-inner {
  width: 100%;
  max-width: 420px; 
  padding: 0 20px;
}

.form-title { font-size: 2rem; color: var(--text-dark); margin-bottom: 10px; font-weight: 800; }
.form-subtitle { color: var(--text-medium); margin-bottom: 30px; font-size: 0.95rem; }

.input-group { margin-bottom: 20px; }
.input-group label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 0.9rem; color: var(--text-dark); }
.input-group input {
  width: 100%;
  padding: 14px 16px; 
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: 0.3s;
  background-color: #f9fafb;
}
.input-group input:focus { outline: none; border-color: var(--primary-color); background-color: white; box-shadow: 0 0 0 3px rgba(84, 101, 255, 0.1); }

.form-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.remember-me { display: flex; align-items: center; gap: 8px; color: var(--text-medium); font-size: 0.95rem; }
.forgot-pwd { color: var(--primary-color); font-weight: 600; font-size: 0.95rem; }

.w-full { width: 100%; padding: 15px; font-size: 1.1rem; margin-bottom: 25px; border-radius: 8px; cursor: pointer; border: none; }

.switch-mode { text-align: center; color: var(--text-medium); font-size: 1rem; }
.link-primary { color: var(--primary-color); font-weight: 700; cursor: pointer; transition: 0.3s; }
.link-primary:hover { color: var(--hover-black); }

@media (max-width: 768px) {
  .auth-banner { display: none; }
  .auth-container { min-height: calc(100vh - 80px); }
  .form-inner { max-width: 100%; padding: 40px 25px; }
}
</style>