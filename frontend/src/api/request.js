import axios from 'axios'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '../utils/url'

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code !== 200) {
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(result)
    }
    return result
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || '网络请求失败')
    return Promise.reject(error)
  },
)

export default request
