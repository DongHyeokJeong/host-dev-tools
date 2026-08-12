import axios from 'axios'

// 확인 필요: 배포 환경의 실제 API 베이스 URL로 교체 필요 (현재는 로컬 개발용 고정값)
const API_BASE = 'http://localhost:8080/api'

export async function listProcesses() {
  const res = await axios.get(`${API_BASE}/test-data-vault/processes`)
  return res.data
}

export async function registerProcess(name) {
  const res = await axios.post(`${API_BASE}/test-data-vault/processes`, { name })
  return res.data
}

export async function listTestData(process) {
  const res = await axios.get(`${API_BASE}/test-data-vault`, {
    params: process ? { process } : {},
  })
  return res.data
}

export async function saveTestData({ name, processName, content }) {
  const res = await axios.post(`${API_BASE}/test-data-vault`, { name, processName, content })
  return res.data
}

export async function deleteTestData(id) {
  await axios.delete(`${API_BASE}/test-data-vault/${id}`)
}
