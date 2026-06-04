import request from './request'

export const orderApi = {
  // 创建订单
  create(data) {
    return request.post('/order/create', data)
  },

  // 获取订单列表
  getList(params) {
    return request.get('/order/list', { params })
  },

  // 获取订单详情
  getDetail(id) {
    return request.get(`/order/${id}`)
  },

  // 支付订单
  pay(id) {
    return request.post(`/order/${id}/pay`)
  },

  // 取消订单
  cancel(id, reason) {
    return request.post(`/order/${id}/cancel`, { reason })
  }
}
