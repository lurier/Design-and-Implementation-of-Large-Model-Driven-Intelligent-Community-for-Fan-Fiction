import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getUserInfo() {
  return request.get('/users/me')
}

export function getUserPublishedArticles() {
  return request.get('/articles/user/published')
}

export function updateUserProfile(data) {
  return request.put('/users/profile', data)
}
