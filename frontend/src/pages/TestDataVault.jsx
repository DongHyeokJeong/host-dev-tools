import React, { useEffect, useState } from 'react'
import {
  listProcesses,
  registerProcess,
  listTestData,
  saveTestData,
  deleteTestData,
} from '../api/testDataVaultApi'

const TRANSACTION_TYPES = ['승인거래', '취소거래']

// 테스트 데이터함 화면. 왼쪽에 등록된 프로세스(ongwhtion44 같은 코드) 목록을 두고,
// 프로세스를 선택하면 해당 프로세스로 저장된 전문만 오른쪽에 보여준다. 승인거래/취소거래는
// 프로세스가 아니라 각 전문의 "거래 구분" 값이다. 저장은 백엔드가 data/test-data-vault.json
// 파일에 그대로 쓰고 읽는다 (DB 미사용, 확인 필요: 다중 서버 동시 접근은 고려하지 않음).
export default function TestDataVault() {
  const [processes, setProcesses] = useState([])
  const [selectedProcess, setSelectedProcess] = useState(null) // null = 전체
  const [newProcessName, setNewProcessName] = useState('')
  const [entries, setEntries] = useState([])
  const [name, setName] = useState('')
  const [processName, setProcessName] = useState('')
  const [transactionType, setTransactionType] = useState(TRANSACTION_TYPES[0])
  const [content, setContent] = useState('')
  const [copiedId, setCopiedId] = useState(null)
  const [error, setError] = useState(null)

  async function loadProcesses() {
    try {
      const data = await listProcesses()
      setProcesses(data)
      setError(null)
    } catch (e) {
      setError('백엔드(http://localhost:8080)에 연결할 수 없습니다. 서버 실행 여부를 확인하세요.')
    }
  }

  async function loadEntries(process) {
    try {
      const data = await listTestData(process || undefined)
      setEntries(data)
      setError(null)
    } catch (e) {
      setError('백엔드(http://localhost:8080)에 연결할 수 없습니다. 서버 실행 여부를 확인하세요.')
    }
  }

  useEffect(() => {
    loadProcesses()
  }, [])

  useEffect(() => {
    loadEntries(selectedProcess)
    setProcessName(selectedProcess || '')
  }, [selectedProcess])

  async function handleAddProcess() {
    if (!newProcessName.trim()) return
    try {
      const updated = await registerProcess(newProcessName.trim())
      setProcesses(updated)
      setNewProcessName('')
    } catch (e) {
      setError('프로세스 등록 중 오류가 발생했습니다.')
    }
  }

  async function handleSave() {
    if (!content.trim()) return
    try {
      await saveTestData({ name, processName, transactionType, content })
      setName('')
      setContent('')
      await loadProcesses()
      await loadEntries(selectedProcess)
    } catch (e) {
      setError('저장 중 오류가 발생했습니다.')
    }
  }

  async function handleDelete(id) {
    try {
      await deleteTestData(id)
      setEntries((prev) => prev.filter((e) => e.id !== id))
    } catch (e) {
      setError('삭제 중 오류가 발생했습니다.')
    }
  }

  async function handleCopy(entry) {
    try {
      await navigator.clipboard.writeText(entry.content)
      setCopiedId(entry.id)
      setTimeout(() => setCopiedId(null), 1500)
    } catch (e) {
      setError('클립보드 복사에 실패했습니다.')
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>테스트 데이터함</h1>
        <p className="page-desc">프로세스별로 전문을 저장해두고 필요할 때 복사해서 재사용합니다</p>
      </div>

      <div className="tdv-layout">
        <aside className="tdv-sidebar card">
          <div className="tdv-sidebar-title">등록된 프로세스</div>
          <nav className="tdv-process-nav">
            <button
              className={'tdv-process-link' + (selectedProcess === null ? ' active' : '')}
              onClick={() => setSelectedProcess(null)}
            >
              전체
            </button>
            {processes.map((p) => (
              <button
                key={p}
                className={'tdv-process-link' + (selectedProcess === p ? ' active' : '')}
                onClick={() => setSelectedProcess(p)}
              >
                {p}
              </button>
            ))}
          </nav>
          <div className="tdv-add-process">
            <input
              className="stringhex-input tdv-name-input"
              value={newProcessName}
              onChange={(e) => setNewProcessName(e.target.value)}
              placeholder="예: ongwccio061"
            />
            <button className="btn btn-sm" onClick={handleAddProcess}>
              추가
            </button>
          </div>
        </aside>

        <div className="tdv-main">
          <div className="card stringhex-card">
            <label className="field-label">
              프로세스{' '}
              <span className="field-label-hint">(왼쪽에서 선택하거나 새 코드를 입력하면 자동으로 등록됩니다)</span>
            </label>
            <input
              className="stringhex-input tdv-name-input"
              value={processName}
              onChange={(e) => setProcessName(e.target.value)}
              placeholder="예: ongwhtion44"
            />

            <label className="field-label">거래 구분</label>
            <select
              className="stringhex-input tdv-name-input"
              value={transactionType}
              onChange={(e) => setTransactionType(e.target.value)}
            >
              {TRANSACTION_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>

            <label className="field-label">전문 내용</label>
            <textarea
              className="stringhex-input"
              rows={3}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="저장할 원문 전문을 입력하세요"
              spellCheck={false}
            />

            <label className="field-label">설명</label>
            <input
              className="stringhex-input tdv-name-input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="예: 정상승인 케이스"
            />

            <div className="tdv-save-row">
              <button className="btn" disabled={!content.trim()} onClick={handleSave}>
                저장
              </button>
            </div>
          </div>

          <div className="card" style={{ marginTop: 16 }}>
            <table className="iso-field-table">
              <thead>
                <tr>
                  <th>설명</th>
                  <th>프로세스</th>
                  <th>거래 구분</th>
                  <th>저장 시각</th>
                  <th>내용</th>
                  <th>액션</th>
                </tr>
              </thead>
              <tbody>
                {entries.length === 0 && (
                  <tr>
                    <td colSpan={6}>
                      <span className="preview-empty">저장된 데이터가 없습니다</span>
                    </td>
                  </tr>
                )}
                {entries.map((e) => (
                  <tr key={e.id}>
                    <td>{e.name}</td>
                    <td>{e.processName}</td>
                    <td>{e.transactionType}</td>
                    <td>{new Date(e.createdAt).toLocaleString()}</td>
                    <td className="tdv-content-cell">{e.content}</td>
                    <td className="sim-actions">
                      <button className="btn btn-sm" onClick={() => handleCopy(e)}>
                        {copiedId === e.id ? '복사됨' : '복사'}
                      </button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleDelete(e.id)}>
                        삭제
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {error && <div className="error">{error}</div>}
        </div>
      </div>
    </div>
  )
}
