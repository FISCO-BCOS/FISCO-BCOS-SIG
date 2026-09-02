import request from '@/utils/request'

export function getDashboardData() {
  return request.get('/dashboard')
}