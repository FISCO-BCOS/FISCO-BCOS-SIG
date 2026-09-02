import request from '@/utils/request'

export function getUserProfile() {
  return request.get('/user/profile')
}

export function updatePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/user/password', data)
}