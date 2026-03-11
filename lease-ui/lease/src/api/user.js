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

// 获取历史聊天记录
export const getChatHistoryAPI = (userId1, userId2) => {
  return request({
    url: '/api/chat/history',
    method: 'get',
    params: { userId1, userId2 }
  })
}

// 2. 批量获取在线状态
export const getBatchOnlineStatusAPI = (userIds) => {
  return request({
    url: '/api/chat/online-status/batch',
    method: 'post',
    data: userIds // 注意：后端使用 @RequestBody，所以这里用 data (放在请求体里)
  })
}

// 3. 将消息标记为已读
export const markAsReadAPI = (fromUserId, toUserId) => {
  return request({
    url: '/api/chat/read',
    method: 'post',
    params: { fromUserId, toUserId } // 注意：后端使用 @RequestParam，所以用 params (放在URL拼接里)
  })
}

// 获取用户的全局未读消息总数
export const getUnreadCountAPI = (userId) => {
  return request({
    url: '/api/chat/unread-count',
    method: 'get',
    params: { userId }
  })
}

// 获取用户个人资料
export const getUserProfileAPI = (userId) => {
  return request({
    url: '/api/user/profile',
    method: 'get',
    params: { userId }
  })
}

// 提交修改/绑定个人资料
export const updateUserProfileAPI = (data) => {
  return request({
    url: '/api/user/profile/update',
    method: 'post',
    data // 使用 POST 传递 JSON 体
  })
}