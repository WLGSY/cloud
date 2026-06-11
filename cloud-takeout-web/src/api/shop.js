import request from './request'

export const shopApi = {
  // 商家注册（创建用户+店铺）
  register(data) {
    return request.post('/api/shop/register', data)
  },

  // 获取当前商家的店铺信息（单个，兼容旧接口）
  getMyShop() {
    return request.get('/api/shop/my')
  },

  // 获取当前商家的所有店铺列表（新接口）
  getMyShops() {
    return request.get('/api/shop/my')
  },

  // 保存/更新店铺信息
  save(data) {
    return request.post('/api/shop/save', data)
  },

  // 创建新店铺
  create(data) {
    return request.post('/api/shop/create', data)
  },

  // 用户端：获取所有营业中店铺（无需登录）
  getAll() {
    return request.get('/api/shop/all')
  },

  // 管理员：获取店铺列表
  getList(params) {
    return request.get('/api/shop/list', { params })
  }
}
