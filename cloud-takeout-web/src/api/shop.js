import request from './request'

export const shopApi = {
  // 商家注册（创建用户+店铺）
  register(data) {
    return request.post('/api/shop/register', data)
  },

  // 获取当前商家的店铺信息
  getMyShop() {
    return request.get('/api/shop/my')
  },

  // 保存/更新店铺信息
  save(data) {
    return request.post('/api/shop/save', data)
  },

  // 管理员：获取店铺列表
  getList(params) {
    return request.get('/api/shop/list', { params })
  }
}
