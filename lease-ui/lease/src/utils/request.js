import axios from 'axios'

// 1. 创建 axios 实例
const request = axios.create({
  baseURL: '', // 如果你配了 vite proxy，这里可以只写 '/api' 甚至 '/'
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 2. 请求拦截器 (Request Interceptor)
request.interceptors.request.use(
  config => {
    // 每次发送请求之前，尝试从本地获取 Token
    const token = localStorage.getItem('user_token')
    if (token) {
      // 按规范放入请求头
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 3. 响应拦截器 (Response Interceptor)
request.interceptors.response.use(
  response => {
    // axios 会自动把后端返回的 JSON 放在 response.data 里
    const resData = response.data

    // 统一处理后端的 Result 包装类逻辑
    if (resData.code === 200) {
      return resData // 请求成功，直接把数据返回给业务层
    } else {
      // 后端业务报错（如密码错误、找不到数据等），抛出错误让业务层 catch
      return Promise.reject(new Error(resData.message || '操作失败'))
    }
  },
  error => {
    console.error('网络请求异常:', error)
    // 可以在这里统一处理 401 登录过期跳回登录页等 Http 状态码异常
    return Promise.reject(error)
  }
)

export { request }