import request from './request'

export function getJobs(params) {
  return request.get('/api/user/jobs', { params })
}

export function getRecommendJobs(params) {
  return request.get('/api/user/jobs/recommend', { params })
}

export function getJobDetail(id) {
  return request.get(`/api/user/jobs/${id}`)
}

export function analyzeJobMatch(id) {
  return request.post(`/api/user/ai/job-match/${id}`)
}

export function favoriteJob(id) {
  return request.post(`/api/user/jobs/${id}/favorite`)
}

export function cancelFavoriteJob(id) {
  return request.delete(`/api/user/jobs/${id}/favorite`)
}

export function getFavoriteJobs(params) {
  return request.get('/api/user/jobs/favorites', { params })
}
