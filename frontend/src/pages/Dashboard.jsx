import React from 'react'
import { Link } from 'react-router-dom'

// 확인 필요: 각 카드의 상태 요약(예: "3개 실행중 / 5개")은 실제 백엔드 데이터 연동 전이라
// 정적 설명 문구로 대체함. 추후 각 화면 API 구현 시 실데이터로 교체 필요.
const CARDS = [
  { path: '/card-simulator', title: '카드사 시뮬레이터', desc: '카드사별 시뮬레이터 실행 상태 확인 및 제어' },
  { path: '/iso-parsing', title: 'ISO 전문 파싱', desc: '선택한 규격에 맞춰 필드 자동 매핑' },
  { path: '/bitmap-parser', title: '비트맵 파서', desc: 'Primary / Secondary 비트맵 On/Off 확인' },
  { path: '/host-numbering', title: '호스트 채번', desc: '운영·테스트 환경 대조 후 미사용 코드/포트 추천' },
  { path: '/test-data-vault', title: '테스트 데이터함', desc: '프로세스별 전문 저장 및 재사용' },
  { path: '/string-hex', title: 'String/Hex 변환', desc: 'ASCII / EBCDIC 혼합 문자열 ↔ Hex 변환' },
]

export default function Dashboard() {
  return (
    <div className="page">
      <div className="page-header">
        <h1>대시보드</h1>
        <p className="page-desc">자주 쓰는 기능 바로가기</p>
      </div>
      <div className="dashboard-grid">
        {CARDS.map((c) => (
          <Link key={c.path} to={c.path} className="card dashboard-card">
            <div className="dashboard-card-title">{c.title}</div>
            <div className="dashboard-card-desc">{c.desc}</div>
          </Link>
        ))}
      </div>
    </div>
  )
}
