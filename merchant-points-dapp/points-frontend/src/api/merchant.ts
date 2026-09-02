import request from '@/utils/request'

export function getMerchantList(params: any) {
  return request.get('/merchants', { params })
}

export function getMerchantDetail(id: number) {
  return request.get(`/merchants/${id}`)
}

export function auditMerchant(id: number, data: any) {
  return request.put(`/merchants/${id}/audit`, data)
}

export function switchMerchantStatus(id: number, status: number) {
  return request.put(`/merchants/${id}/status`, { status })
}