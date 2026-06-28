import request from './request'

export function createApplication(data) {
  return request.post('/api/user/applications', data)
}

export function getApplications() {
  return request.get('/api/user/applications')
}

export function withdrawApplication(id) {
  return request.put(`/api/user/applications/${id}/withdraw`)
}
