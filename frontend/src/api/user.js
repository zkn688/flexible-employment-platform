import request from './request'

export function login(data) {
  return request.post('/api/user/login', data)
}

export function register(data) {
  return request.post('/api/user/register', data)
}

export function getCaptcha(scene = 'login') {
  return request.get('/api/user/captcha', { params: { scene } })
}

export function getProfile() {
  return request.get('/api/user/profile')
}

export function updateProfile(data) {
  return request.put('/api/user/profile', data)
}

export function updatePassword(data) {
  return request.put('/api/user/profile/password', data)
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/user/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function uploadMaterial(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/user/upload/materials', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function getPreference() {
  return request.get('/api/user/preference')
}

export function savePreference(data) {
  return request.post('/api/user/preference', data)
}
