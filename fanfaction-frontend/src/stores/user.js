import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // 初始化时，如果有 token，自动获取用户信息
  if (token.value) {
    fetchUserInfo()
  }

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const tokenValue = res.data.token
    token.value = tokenValue
    localStorage.setItem('token', tokenValue)
    await fetchUserInfo()
    return res
  }

  async function register(registerForm) {
    return await registerApi(registerForm)
  }

  async function fetchUserInfo() {
    if (token.value) {
      try {
        const res = await getUserInfo()
        userInfo.value = res.data
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果获取失败，清除 token
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
      }
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  // 刷新用户信息（审核通过后重新拉取最新角色信息，实现无感升级）
  async function refreshUserInfo() {
    if (token.value) {
      try {
        const res = await getUserInfo()
        userInfo.value = res.data
        return res.data
      } catch (error) {
        console.error('刷新用户信息失败:', error)
        throw error
      }
    }
  }

  // 更新用户信息（用于头像等字段更新后同步到全局状态）
  function updateUserInfo(newInfo) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...newInfo }
    }
  }

  // 判断是否为创作者
  function isCreator() {
    // 检查 roles 字段是否包含 CREATOR 或 ADMIN
    const roles = userInfo.value?.roles || ''
    return roles.includes('ROLE_CREATOR') || roles.includes('ROLE_ADMIN')
  }

  // 判断是否有创作者权限（包括已申请但未通过的情况）
  function hasCreatorPermission() {
    // 如果是管理员或创作者，直接返回 true
    if (isCreator()) {
      return true
    }
    // 否则检查是否有 creatorStatus 字段且为 APPROVED
    return userInfo.value?.creatorStatus === 'APPROVED'
  }

  // 判断是否正在审核中
  function isCreatorPending() {
    return userInfo.value?.creatorStatus === 'PENDING'
  }

  // 判断是否为管理员
  function isAdmin() {
    const roles = userInfo.value?.roles || ''
    return roles.includes('ROLE_ADMIN')
  }

  return {
    token,
    userInfo,
    login,
    register,
    fetchUserInfo,
    refreshUserInfo,
    logout,
    updateUserInfo,
    isCreator,
    hasCreatorPermission,
    isCreatorPending,
    isAdmin
  }
})
