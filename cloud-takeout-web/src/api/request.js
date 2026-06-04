import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token
    }
    // 【修复】添加调试日志
    console.log('【请求】', config.method.toUpperCase(), config.baseURL + config.url, config.data)
    return config
  },
  error => {
    console.error('请求错误', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 【修复】添加调试日志
    console.log('【响应】', res)

    // 【修复】兼容不同的响应格式
    if (res.code === 200 || res.code === 0) {
      return res
    }

    // 如果返回的是数组（直接返回列表数据）
    if (Array.isArray(res)) {
      return {
        code: 200,
        data: {
          list: res,
          total: res.length
        },
        message: '成功'
      }
    }

    if (res.token) {
      return {
        code: 200,
        data: res,
        message: '成功'
      }
    }

    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    console.error('【响应错误】', error)

    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.clear()
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error(`接口不存在: ${error.config?.url}`)
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败: ${status}`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else if (error.message === 'Network Error') {
      ElMessage.error('网络错误，请确认后端服务是否已启动')
    } else {
      ElMessage.error(error.message || '未知错误')
    }

    return Promise.reject(error)
  }
)

export default request
