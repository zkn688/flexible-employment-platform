import request from './request'

export function getResumes() {
  return request.get('/api/user/resumes')
}

export function createResume(data) {
  return request.post('/api/user/resumes', data)
}

export function updateResume(id, data) {
  return request.put(`/api/user/resumes/${id}`, data)
}

export function deleteResume(id) {
  return request.delete(`/api/user/resumes/${id}`)
}

export function analyzeResumeAdvice(id) {
  return request.post(`/api/user/ai/resume-advice/${id}`)
}
