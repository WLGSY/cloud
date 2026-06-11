import request from './request'

// ========== 配置项 ==========
// 改为 false 使用真实后端接口
const USE_MOCK = false
// ==========================

// 模拟数据（仅当 USE_MOCK = true 时使用）
const mockUsers = [
  { username: 'test', password: '123456', id: 1, nickname: '测试用户', phone: '13800138000' },
  { username: 'admin', password: 'admin123', id: 2, nickname: '管理员', phone: '13900139000' }
]

const mockLogin = (username, password) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const user = mockUsers.find(u => u.username === username && u.password === password)
      if (user) {
        resolve({
          code: 200,
          data: {
            token: `mock-token-${Date.now()}-${user.id}`,
            userInfo: user
          },
          message: '登录成功'
        })
      } else {
        reject({
          response: { status: 401, data: { message: '用户名或密码错误' } }
        })
      }
    }, 500)
  })
}

const mockRegister = (data) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const existing = mockUsers.find(u => u.username === data.username)
      if (existing) {
        reject({
          response: { status: 400, data: { message: '用户名已存在' } }
        })
      } else {
        resolve({
          code: 200,
          data: { id: mockUsers.length + 1, username: data.username, phone: data.phone },
          message: '注册成功'
        })
      }
    }, 500)
  })
}

const mockUpdatePassword = (data) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '密码修改成功'
      })
    }, 500)
  })
}

export const userApi = {
  // 用户登录
  async login(data) {
    if (USE_MOCK) {
      return await mockLogin(data.username, data.password)
    }
    return request.post('/api/user/login', data)
  },

  // 用户注册
  async register(data) {
    if (USE_MOCK) {
      return await mockRegister(data)
    }
    return request.post('/api/user/register', data)
  },

  // 获取用户信息
  getUserInfo() {
    if (USE_MOCK) {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return Promise.resolve({ code: 200, data: userInfo, message: '成功' })
    }
    return request.get('/api/user/info')
  },

  // 更新用户信息
  updateUserInfo(data) {
    if (USE_MOCK) {
      return Promise.resolve({ code: 200, data: data, message: '更新成功' })
    }
    return request.put('/api/user/info', data)
  },

  // 修改密码
  updatePassword(data) {
    if (USE_MOCK) {
      return mockUpdatePassword(data)
    }
    return request.put('/api/user/password', data)
  },

  // ===== 管理端 =====

  // 获取用户列表（分页）
  getUserList(params) {
    return request.get('/api/user/list', { params })
  },

  // 更新用户角色
  updateUserRole(id, role) {
    return request.put(`/api/user/${id}/role`, { role })
  },

  // 上传头像
  uploadAvatar(avatarUrl, userId) {
    return request.post('/api/user/avatar', { avatarUrl, userId: String(userId) })
  },

  // 获取统计数据
  getStats() {
    return request.get('/api/user/stats')
  }
}
