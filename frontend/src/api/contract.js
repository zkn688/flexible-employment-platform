import request from './request'

export function getContracts() {
  return request.get('/api/user/contracts')
}

export function getContractDetail(id) {
  return request.get(`/api/user/contracts/${id}`)
}
