import request from './request'

export const dishApi = {
  // 获取菜品列表（分页、搜索、分类）
  getList(params) {
    const validParams = {}
    Object.keys(params).forEach(key => {
      if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
        validParams[key] = params[key]
      }
    })
    return request.get('/api/dish/list', { params: validParams })
  },

  // 获取菜品详情
  getDetail(id) {
    return request.get(`/api/dish/${id}`)
  },

  // 添加菜品
  add(data) {
    return request.post('/api/dish', data)
  },

  // 更新菜品
  update(data) {
    return request.put('/api/dish', data)
  },

  // 删除菜品
  delete(id) {
    return request.delete(`/api/dish/${id}`)
  },

  // 上架
  publish(id) {
    return request.put(`/api/dish/publish/${id}`)
  },

  // 下架
  recall(id) {
    return request.put(`/api/dish/recall/${id}`)
  }
}
