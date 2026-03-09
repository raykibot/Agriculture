import { request } from '../utils/request'

// 登录接口
export const loginAPI = (data) => {
  return request({
    url: '/api/user/login',
    method: 'post',
    data: data
  })
}

export const logoutAPI = () => {
  return request({
    url: '/api/user/logout',
    method: 'post'
    // 注意：这里的 header 不需要手动加 token，因为之前在 request.js 的请求拦截器里已经全局配置了自动携带 token
  })
}

// 注册接口
export const registerAPI = (data) => {
  return request({
    url: '/api/user/register',
    method: 'post',
    data: data
  })
}