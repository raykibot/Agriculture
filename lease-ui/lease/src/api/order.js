
import { request } from '@/utils/request'
// 创建订单接口


export const createOrderAPI = (data) => {
  return request({
    url: '/api/order/create',
    method: 'post',
    data
  })
}


// 获取支付宝收银台HTML
export const getPayHtmlAPI = (orderNo) => {
  return request({
    url: '/api/order/pay',
    method: 'get',
    params: { orderNo }
  })
}

export const getOrderLogisticsAPI = (orderNo) => {
  return request({
    url: '/api/order/logistics',
    method: 'get',
    params: { orderNo }
  })
}

// 获取用户订单列表(带状态过滤)
export const getUserOrdersAPI = (userId, role, statusGroup) => {
  return request({
    url: '/api/order/list',
    method: 'get',
    params: { userId, role, statusGroup }
  })
}

// 订单发货
export const shipOrderAPI = (orderNo) => {
  return request({
    url: '/api/order/ship',
    method: 'post',
    params: { orderNo }
  })
}