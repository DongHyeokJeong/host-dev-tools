import React, { useEffect, useState } from 'react'
import {
  listCardSimulators,
  startCardSimulator,
  stopCardSimulator,
  restartCardSimulator,
} from '../api/cardSimulatorApi'

// 카드사 시뮬레이터 화면.
// 확인 필요: 실제 대상 카드사 목록과 실행/중지/재시작 시 호출할 실제 명령(재기동 쿼리 등)이
// 아직 정의되지 않아, 현재는 백엔드 메모리에 둔 Mock 상태를 토글하는 골격 단계입니다.
export default function CardSimulator() {
  const [simulators, setSimulators] = useState([])
  const [pendingId, setPendingId] = useState(null)
  const [error, setError] = useState(null)

  async function load() {
    try {
      const data = await listCardSimulators()
      setSimulators(data)
      setError(null)
    } catch (e) {
      setError('백엔드(http://localhost:8080)에 연결할 수 없습니다. 서버 실행 여부를 확인하세요.')
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function handleAction(id, action) {
    setPendingId(id)
    try {
      const updated = await action(id)
      setSimulators((prev) => prev.map((s) => (s.id === id ? updated : s)))
      setError(null)
    } catch (e) {
      setError('요청 처리 중 오류가 발생했습니다.')
    } finally {
      setPendingId(null)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>카드사 시뮬레이터</h1>
        <p className="page-desc">
          카드사별 시뮬레이터 실행 상태를 확인하고 실행/중지/재시작할 수 있습니다 (현재는 Mock 상태 골격)
        </p>
      </div>

      <div className="card">
        <table className="iso-field-table">
          <thead>
            <tr>
              <th>카드사</th>
              <th>상태</th>
              <th>마지막 변경 시각</th>
              <th>제어</th>
            </tr>
          </thead>
          <tbody>
            {simulators.map((s) => (
              <tr key={s.id}>
                <td>{s.name}</td>
                <td>
                  <span className={'status-badge ' + (s.status === 'RUNNING' ? 'status-running' : 'status-stopped')}>
                    {s.status === 'RUNNING' ? '실행중' : '중지됨'}
                  </span>
                </td>
                <td>{new Date(s.lastActionAt).toLocaleString()}</td>
                <td className="sim-actions">
                  <button
                    className="btn btn-sm"
                    disabled={pendingId === s.id || s.status === 'RUNNING'}
                    onClick={() => handleAction(s.id, startCardSimulator)}
                  >
                    실행
                  </button>
                  <button
                    className="btn btn-sm"
                    disabled={pendingId === s.id || s.status === 'STOPPED'}
                    onClick={() => handleAction(s.id, stopCardSimulator)}
                  >
                    중지
                  </button>
                  <button
                    className="btn btn-sm"
                    disabled={pendingId === s.id}
                    onClick={() => handleAction(s.id, restartCardSimulator)}
                  >
                    재시작
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {error && <div className="error">{error}</div>}
      </div>
    </div>
  )
}
