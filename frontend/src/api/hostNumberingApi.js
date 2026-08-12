import axios from 'axios'

// 확인 필요: 배포 환경의 실제 API 베이스 URL로 교체 필요 (현재는 로컬 개발용 고정값)
const API_BASE = 'http://localhost:8080/api'

export function getProductionTemplateUrl() {
  return `${API_BASE}/host-numbering/production-template`
}

export async function getHostNumberingStatus() {
  const res = await axios.get(`${API_BASE}/host-numbering/status`)
  return res.data
}

export async function uploadProductionData(file) {
  const formData = new FormData()
  formData.append('file', file)
  const res = await axios.post(`${API_BASE}/host-numbering/production-upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data
}

export async function recommendHostCode(internetGiro, prefix) {
  const res = await axios.post(`${API_BASE}/host-numbering/recommend`, { internetGiro, prefix })
  return res.data
}
