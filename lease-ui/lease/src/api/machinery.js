import { request } from '../utils/request'

/**
 * 获取农机设备列表
 */
export const getMachineryListAPI = () => {
  return request({
    url: '/api/machinery/list',
    method: 'GET'
    // 如果后续有分页和条件查询参数，可以在这里加上 params: { page, size, keyword }
  })
}