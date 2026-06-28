import request from './request'

export function getNotices() {
  return request.get('/api/user/notices')
}

export function getNoticeDetail(id) {
  return request.get(`/api/user/notices/${id}`)
}

export function getPolicies() {
  return request.get('/api/user/policies')
}

export function getPolicyDetail(id) {
  return request.get(`/api/user/policies/${id}`)
}

export function applyPolicy(data) {
  return request.post('/api/user/policy-applications', data)
}

export function getPolicyApplications() {
  return request.get('/api/user/policy-applications')
}
