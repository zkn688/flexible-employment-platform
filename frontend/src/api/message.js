import request from './request'

export function getMessages() {
  return request.get('/api/user/messages')
}

export function sendMessage(data) {
  return request.post('/api/user/messages', data)
}

export function readMessage(id) {
  return request.put(`/api/user/messages/${id}/read`)
}

export function deleteMessage(id) {
  return request.delete(`/api/user/messages/${id}`)
}
