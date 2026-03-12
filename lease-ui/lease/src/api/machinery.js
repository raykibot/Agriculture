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

