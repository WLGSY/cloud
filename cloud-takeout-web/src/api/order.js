import request from './request'

export const orderApi = {
  // 创建订单
  create(data) {
    return request.post('/api/order/create', data)
  },

  // 获取订单列表
  getList(params) {
    return request.get('/api/order/list', { params })
  },

  // 获取订单详情
  getDetail(id) {
    return request.get(`/api/order/${id}`)
  },

  // 支付订单
  pay(id) {
    return request.post(`/api/order/${id}/pay`)
  },

  // 确认收货
  complete(id) {
    return request.post(`/api/order/${id}/complete`)
  },

  // 取消订单
  cancel(id, reason) {
    return request.post(`/api/order/${id}/cancel`, { reason })
  }
}
