import React, { useEffect, useRef, useState } from 'react'
import {
  getHostNumberingStatus,
  getProductionTemplateUrl,
  recommendHostCode,
  uploadProductionData,
} from '../api/hostNumberingApi'

const NON_GIRO_PREFIXES = ['HD', 'HJ', 'HL', 'HN']
const GIRO_PREFIXES = ['HG']
const ALL_PREFIXES = [...NON_GIRO_PREFIXES, ...GIRO_PREFIXES]

const QUERY_TEXT = "SELECT MERCHANT_NAME, HOST_CODE, PORT_NO FROM HOST_MST WHERE USE_YN = 'Y'"

function formatRelative(isoString) {
  if (!isoString) return '-'
  const diffMs = Date.now() - new Date(isoString).getTime()
  const minutes = Math.floor(diffMs / 60000)
  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}시간 전`
  return `${Math.floor(hours / 24)}일 전`
}

function formatDate(isoString) {
  if (!isoString) return null
  return new Date(isoString).toISOString().slice(0, 10)
}

// 05_호스트채번 화면. 채번의 핵심은 "다음 번호 매기기"가 아니라 운영/테스트 환경 양쪽에
// 이미 사용 중인 코드·포트를 모아 어느 쪽에도 없는 조합을 찾는 것이다. 테스트 DB는
// 자동 연동(백엔드 Mock)으로 보여주고, 운영 데이터는 직접 조회가 불가능하다는 전제라
// 쿼리 결과를 엑셀로 업로드받는다.
export default function HostNumbering() {
  const [status, setStatus] = useState(null)
  const [internetGiro, setInternetGiro] = useState(false)
  const [prefix, setPrefix] = useState('HD')
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [finding, setFinding] = useState(false)
  const [copied, setCopied] = useState(false)
  const fileInputRef = useRef(null)

  async function loadStatus() {
    try {
      const data = await getHostNumberingStatus()
      setStatus(data)
      setError(null)
    } catch (e) {
      setError('백엔드(http://localhost:8080)에 연결할 수 없습니다. 서버 실행 여부를 확인하세요.')
    }
  }

  useEffect(() => {
    loadStatus()
  }, [])

  function handleToggleGiro(next) {
    setInternetGiro(next)
    setPrefix(next ? GIRO_PREFIXES[0] : NON_GIRO_PREFIXES[0])
    setResult(null)
  }

  function handleUploadClick() {
    fileInputRef.current?.click()
  }

  async function handleFileChange(e) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    try {
      const data = await uploadProductionData(file)
      setStatus(data)
      setError(null)
    } catch (e2) {
      setError(e2?.response?.data?.message || '엑셀 업로드 중 오류가 발생했습니다.')
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  async function handleFind() {
    setFinding(true)
    try {
      const data = await recommendHostCode(internetGiro, prefix)
      setResult(data)
      setError(null)
    } catch (e) {
      setError(e?.response?.data?.message || '미사용 조합을 찾는 중 오류가 발생했습니다.')
      setResult(null)
    } finally {
      setFinding(false)
    }
  }

  async function handleCopyQuery() {
    try {
      await navigator.clipboard.writeText(QUERY_TEXT)
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    } catch (e) {
      setError('클립보드 복사에 실패했습니다.')
    }
  }

  const activeGroup = internetGiro ? GIRO_PREFIXES : NON_GIRO_PREFIXES

  return (
    <div className="page">
      <div className="page-header">
        <h1>호스트 채번</h1>
        <p className="page-desc">운영·테스트 환경 대조 후 미사용 코드/포트 추천</p>
      </div>

      <div className="hn-status-grid">
        <div className={'card hn-status-card' + (status && status.testDbMode !== 'REAL' ? ' hn-status-card-warn' : '')}>
          <div className="hn-status-top">
            <div className="hn-status-label">테스트 DB</div>
            <span className={'hn-dot ' + (status && status.testDbMode === 'REAL' ? 'hn-dot-ok' : 'hn-dot-warn')} />
          </div>
          <div className={'hn-status-value' + (status && status.testDbMode !== 'REAL' ? ' hn-status-value-warn' : '')}>
            {status && status.testDbMode === 'REAL' ? '자동 연동됨' : 'Mock 데이터 (미연결)'}
          </div>
          <div className="hn-status-caption">
            · {status && status.testDbMode === 'REAL' ? '마지막 동기화' : '기준 시각'}{' '}
            {status ? formatRelative(status.testDbLastSyncAt) : '-'}
          </div>
        </div>

        <div className={'card hn-status-card' + (status && !status.productionUploaded ? ' hn-status-card-warn' : '')}>
          <div className="hn-status-top">
            <div className="hn-status-label">운영 데이터</div>
            <div className="hn-status-actions">
              <a className="btn btn-sm" href={getProductionTemplateUrl()} download>
                양식 다운로드
              </a>
              <button className="btn btn-sm" disabled={uploading} onClick={handleUploadClick}>
                {uploading ? '업로드 중...' : '엑셀 업로드'}
              </button>
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept=".xlsx,.xls"
              style={{ display: 'none' }}
              onChange={handleFileChange}
            />
          </div>
          <div className={'hn-status-value' + (status && !status.productionUploaded ? ' hn-status-value-warn' : '')}>
            {status && status.productionUploaded ? '엑셀 업로드 완료' : '엑셀 업로드 필요'}
          </div>
          <div className="hn-status-caption">
            (가맹점명, 코드, 포트) ·{' '}
            {status && status.productionUploaded
              ? `${formatDate(status.productionLastUploadedAt)} 업로드`
              : '아직 업로드되지 않음'}
          </div>
        </div>
      </div>

      <label className="field-label">운영 데이터 조회 쿼리 (엑셀 양식)</label>
      <div className="card code-block">
        <button className="btn btn-sm code-block-copy" onClick={handleCopyQuery}>
          {copied ? '복사됨' : '복사'}
        </button>
        <div className="code-block-line">{QUERY_TEXT}</div>
        <div className="code-block-comment">-- 결과를 엑셀로 저장 후 위 '엑셀 업로드'에 첨부</div>
      </div>

      <label className="field-label">인터넷 지로 채번 여부</label>
      <div className="toggle-group">
        <button
          className={'toggle-btn' + (!internetGiro ? ' active' : '')}
          onClick={() => handleToggleGiro(false)}
        >
          아니오
        </button>
        <button className={'toggle-btn' + (internetGiro ? ' active' : '')} onClick={() => handleToggleGiro(true)}>
          예 (인터넷지로)
        </button>
      </div>

      <label className="field-label">코드 접두사</label>
      <div className="prefix-row">
        <div className="prefix-group">
          {ALL_PREFIXES.map((p) => {
            const enabled = activeGroup.includes(p)
            return (
              <button
                key={p}
                className={'prefix-btn' + (prefix === p ? ' active' : '') + (!enabled ? ' disabled' : '')}
                disabled={!enabled}
                onClick={() => {
                  setPrefix(p)
                  setResult(null)
                }}
              >
                {p}
              </button>
            )
          })}
        </div>
        <span className="prefix-hint">
          인터넷지로 '{internetGiro ? '예' : '아니오'}' 상태라 {internetGiro ? 'HD/HJ/HL/HN은' : 'HG는'} 비활성
        </span>
      </div>

      <div className="hn-find-row">
        <button className="btn btn-primary" disabled={finding} onClick={handleFind}>
          {finding ? '찾는 중...' : '미사용 조합 찾기'}
        </button>
      </div>

      {status && !status.productionUploaded && (
        <div className="warning-box">⚠ 운영 데이터가 아직 업로드되지 않아 테스트 DB 기준으로만 추천합니다.</div>
      )}
      {error && <div className="error">{error}</div>}

      {result && (
        <>
          <label className="field-label" style={{ marginTop: 20 }}>
            추천 결과
          </label>
          <div className="hn-result-grid">
            <div className="card hn-result-card">
              <div className="hn-status-label">코드</div>
              <div className="hn-result-value">{result.code}</div>
            </div>
            <div className="card hn-result-card">
              <div className="hn-status-label">운영 포트 (…1)</div>
              <div className="hn-result-value">{result.productionPort}</div>
            </div>
            <div className="card hn-result-card">
              <div className="hn-status-label">테스트 포트 (…2)</div>
              <div className="hn-result-value">{result.testPort}</div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
