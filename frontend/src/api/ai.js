import request from './request'

export function generateInterviewQuestions(data) {
  return request.post('/api/user/ai/interview/questions', data)
}

export function analyzeInterviewAnswer(data) {
  return request.post('/api/user/ai/interview/feedback', data)
}
