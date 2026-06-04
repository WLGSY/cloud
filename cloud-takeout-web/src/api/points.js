import request from './request'

export const pointsApi = {
  // 获取我的积分
  getMyPoints() {
    return request.get('/points/my')
  },

  // 获取积分明细
  getLog(params) {
    return request.get('/points/log', { params })
  },

  // 积分排行榜
  getRanking() {
    return request.get('/points/ranking')
  }
}
