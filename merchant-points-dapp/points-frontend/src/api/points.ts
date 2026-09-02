import request from '@/utils/request'

export function issuePoints(data: any) {
  return request.post('/points/issue', data)
}

export function transferPoints(data: any) {
  return request.post('/points/transfer', data)
}

export function consumePoints(data: any) {
  return request.post('/points/consume', data)
}

export function getBalance(merchantId: number) {
  return request.get(`/points/balance/${merchantId}`)
}

export function getTransactions(params: any) {
  return request.get('/points/transactions', { params })
}