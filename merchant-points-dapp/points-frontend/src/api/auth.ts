import request from '@/utils/request'

export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data)
}

export function register(data: any) {
  return request.post('/auth/register', data)
}