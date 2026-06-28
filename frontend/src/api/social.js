import request from './request'

export function applySocialSecurity(data) {
  return request.post('/api/user/social-security/apply', data)
}

export function getSocialApplications() {
  return request.get('/api/user/social-security/applications')
}

export function getSocialPayments() {
  return request.get('/api/user/social-security/payments')
}

export function paySocialPayment(id) {
  return request.put(`/api/user/social-security/payments/${id}/pay`)
}
