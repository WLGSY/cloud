import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 方法
  const setUser = (data) => {
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
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
