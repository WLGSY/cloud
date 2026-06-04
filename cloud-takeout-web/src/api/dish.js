import request from './request'

export const dishApi = {
  // 获取菜品列表（分页、搜索、分类）
  getList(params) {
    return request.get('/dish/list', { params })
  },

  // 获取菜品详情
  getDetail(id) {
    return request.get(`/dish/${id}`)
  }
}
