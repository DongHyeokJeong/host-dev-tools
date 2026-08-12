import axios from 'axios'

// 확인 필요: 배포 환경의 실제 API 베이스 URL로 교체 필요 (현재는 로컬 개발용 고정값)
const API_BASE = 'http://localhost:8080/api'

export async function convertStringToHex(input, encoding) {
  const res = await axios.post(`${API_BASE}/string-hex/convert`, { input, encoding })
  return res.data
}
