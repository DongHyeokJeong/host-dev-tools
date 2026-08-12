import React from 'react'

// 확인 필요: 대시보드/카드사 시뮬레이터/ISO 전문 파싱/비트맵 파서/호스트 채번/테스트 데이터함 화면은
// 다음 단계에서 순차적으로 구현 예정. 오늘은 String/Hex 변환 화면만 실제 구현하고
// 나머지는 라우팅/사이드바 뼈대 확인용 placeholder로 둔다.
export default function Placeholder({ title, description }) {
  return (
    <div className="page">
      <div className="page-header">
        <h1>{title}</h1>
        {description && <p className="page-desc">{description}</p>}
      </div>
      <div className="card placeholder-card">
        <p>이 화면은 아직 구현되지 않았습니다. (준비 중)</p>
      </div>
    </div>
  )
}
