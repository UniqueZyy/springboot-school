import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserVO } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserVO | null>(null)
  const isLoggedIn = ref(false)

  const setUserInfo = (user: UserVO) => {
    userInfo.value = user
    isLoggedIn.value = true
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  const clearUserInfo = () => {
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('userInfo')
  }

  const loadUserInfo = () => {
    const saved = localStorage.getItem('userInfo')
    if (saved) {
      userInfo.value = JSON.parse(saved)
      isLoggedIn.value = true
    }
  }

  return {
    userInfo,
    isLoggedIn,
    setUserInfo,
    clearUserInfo,
    loadUserInfo
  }
})
