export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export function resolveFileUrl(url) {
  if (!url) {
    return ''
  }
  if (url.startsWith('/upload/')) {
    return `${API_BASE_URL}${url}`
  }
  return url
}
