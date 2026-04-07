import {request} from '@/utils/request'

export const getAdminUsersAPI = () => request.get('/api/admin/users')
export const changeUserStatusAPI = (id, status) => request.put('/api/admin/users/status', { id, status })

export const getAdminMachineryAPI = () => request.get('/api/admin/machinery')
export const updateMachineryPriceAPI = (id, price) => request.put('/api/admin/machinery/price', { id, price })