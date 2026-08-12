import React, { useEffect, useRef, useState } from 'react'
import { parseIsoMessage } from '../api/isoParsingApi'

const EXAMPLE = 'ISO0234000520200323804213060920400030000000000450000~~'

// ISO 전문 파싱 화면.
// 백엔드 IsoParsingService.FIELD_SPECS에 정의된 순서/길이대로 원문을 앞에서부터 잘라 보여준다.
// 확인 필요: 전송일시/추적번호/개시시간/개시일자/입력 유형/거래고유번호는 아직 길이가 정의되지
// 않아 값 없이 자리만 표시된다 (금융결제원 스펙 확정 후 백엔드 FIELD_SPECS에 길이만 채우면 됨).
export default function IsoParsing() {
  const [rawInput, setRawInput] = useState(EXAMPLE)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const debounceRef = useRef(null)

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current)

    if (!rawInput) {
      setResult(null)
      setError(null)
      return
    }

    debounceRef.current = setTimeout(async () => {
      try {
        const data = await parseIsoMessage(rawInput)
        setResult(data)
        setError(null)
      } catch (e) {
        setError('백엔드(http://localhost:8080)에 연결할 수 없습니다. 서버 실행 여부를 확인하세요.')
      }
    }, 150)

    return () => clearTimeout(debounceRef.current)
  }, [rawInput])

  return (
    <div className="page">
      <div className="page-header">
        <h1>ISO 전문 파싱</h1>
        <p className="page-desc">
          정해진 필드 순서/길이대로 원문을 잘라서 보여줍니다. 길이가 아직 정의되지 않은 필드는 이름만 표시됩니다.
        </p>
      </div>

      <div className="card stringhex-card">
        <label className="field-label">원문 전문 입력</label>
        <textarea
          className="stringhex-input"
          rows={3}
          value={rawInput}
          onChange={(e) => setRawInput(e.target.value)}
          placeholder={EXAMPLE}
          spellCheck={false}
        />

        {result && (
          <>
            <label className="field-label">필드별 파싱 결과</label>
            <table className="iso-field-table">
              <thead>
                <tr>
                  <th>필드명</th>
                  <th>오프셋</th>
                  <th>길이</th>
                  <th>값</th>
                </tr>
              </thead>
              <tbody>
                {result.fields.map((f, idx) => (
                  <tr key={idx} className={f.length === null ? 'iso-field-undefined' : ''}>
                    <td>{f.name}</td>
                    <td>{f.offset}</td>
                    <td>{f.length === null ? '확인 필요' : f.length}</td>
                    <td className="iso-field-value">
                      {f.value !== null ? f.value : <span className="preview-empty">-</span>}
                      {f.length !== null && !f.complete && f.value !== null && (
                        <span className="iso-field-incomplete"> (데이터 부족)</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <label className="field-label">
              이후 데이터{' '}
              <span className="field-label-hint">(정의된 필드를 모두 읽고 남은 나머지 원문)</span>
            </label>
            <div className="stringhex-result iso-remaining">
              {result.remainingData ? result.remainingData : <span className="preview-empty">-</span>}
            </div>
          </>
        )}

        {result && result.warnings.length > 0 && (
          <div className="warning-box">
            {result.warnings.map((w, i) => (
              <div key={i}>⚠ {w}</div>
            ))}
          </div>
        )}
        {error && <div className="error">{error}</div>}
      </div>
    </div>
  )
}
