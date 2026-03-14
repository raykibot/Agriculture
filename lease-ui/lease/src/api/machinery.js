import { request } from '../utils/request'

// 获取农机分页列表 (支持分类和关键字)
export const getMachineryListAPI = (current = 1, size = 12, categoryId = null, keyword = null) => {
  return request({
    url: '/api/machinery/list',
    method: 'get',
    params: { current, size, categoryId, keyword }
  })
}


// 获取首页各个分类的随机热门机型
export const getPopularMachineryAPI = () => {
  return request({
    url: '/api/machinery/popular',
    method: 'get'
  })
}

// 获取农机设备详情
export const getMachineryDetailAPI = (id) => {
  return request({
    url: `/api/machinery/detail/${id}`,
    method: 'get'
  })
}

// 动态获取分类
export const getCategoriesAPI = () => {
  return request.get('/api/machinery/categories')
}

// 提交发布表单
export const publishMachineryAPI = (data) => {
  return request.post('/api/machinery/publish', data)
}

// 上传图片 (必须使用 FormData)
export const uploadImageAPI = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取我发布的农机
export const getMyPublishedMachineryAPI = (userId) => {
  return request.get('/api/machinery/my-published', { params: { userId } })
}

