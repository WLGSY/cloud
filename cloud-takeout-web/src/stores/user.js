import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 方法
  const setUser = (data) => {
    token.value = data.token
    // 合并 userType 到 userInfo
    userInfo.value = {
      ...data.userInfo,
      userType: data.userInfo?.userType || data.userType || 'customer'
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.clear()
  }

  const isLoggedIn = () => {
    return !!token.value
  }

  return { token, userInfo, setUser, logout, isLoggedIn }
})
