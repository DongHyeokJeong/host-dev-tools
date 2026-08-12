import axios from 'axios'

// 확인 필요: 배포 환경의 실제 API 베이스 URL로 교체 필요 (현재는 로컬 개발용 고정값)
const API_BASE = 'http://localhost:8080/api'

export async function listCardSimulators() {
  const res = await axios.get(`${API_BASE}/card-simulators`)
  return res.data
}

export async function startCardSimulator(id) {
  const res = await axios.post(`${API_BASE}/card-simulators/${id}/start`)
  return res.data
}

export async function stopCardSimulator(id) {
  const res = await axios.post(`${API_BASE}/card-simulators/${id}/stop`)
  return res.data
}

export async function restartCardSimulator(id) {
  const res = await axios.post(`${API_BASE}/card-simulators/${id}/restart`)
  return res.data
}
