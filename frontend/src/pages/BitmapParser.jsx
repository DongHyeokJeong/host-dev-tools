import React, { useEffect, useRef, useState } from 'react'
import { parseBitmap } from '../api/bitmapApi'

const EXAMPLE = 'F23A800108E098000020000000000001'

function BitmapGrid({ fields }) {
  return (
    <div className="bitmap-grid">
      {fields.map((f) => (
        <div
          key={f.number}
          className={'bitmap-cell' + (f.on ? ' bitmap-cell-on' : '')}
          title={`필드 ${f.number}: ${f.on ? 'On' : 'Off'}`}
        >
          {f.number}
        </div>
      ))}
    </div>
  )
}

// 비트맵 파서 화면. 16자리(1차) 또는 32자리(1차+2차) Hex 입력 시 필드 1~128의 On/Off를 실시간으로 보여준다.
export default function BitmapParser() {
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
        const data = await parseBitmap(rawInput)
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
        <h1>비트맵 파서</h1>
        <p className="page-desc">16자리(1차) 또는 32자리(1차+2차) Hex 비트맵을 입력하면 필드 On/Off를 실시간으로 보여줍니다</p>
      </div>

      <div className="card stringhex-card">
        <label className="field-label">비트맵 Hex 입력</label>
        <textarea
          className="stringhex-input"
          rows={2}
          value={rawInput}
          onChange={(e) => setRawInput(e.target.value)}
          placeholder={EXAMPLE}
          spellCheck={false}
        />

        {result && (
          <>
            <label className="field-label">
              1차 비트맵{' '}
              <span className="field-label-hint">
                ({result.primaryBitmapHex ?? '-'} — 1번 비트: 2차 비트맵 {result.hasSecondary ? '있음' : '없음'})
              </span>
            </label>
            {result.primaryFields.length > 0 ? (
              <BitmapGrid fields={result.primaryFields} />
            ) : (
              <div className="stringhex-result">
                <span className="preview-empty">-</span>
              </div>
            )}

            {result.hasSecondary && (
              <>
                <label className="field-label">
                  2차 비트맵 <span className="field-label-hint">({result.secondaryBitmapHex ?? '-'})</span>
                </label>
                {result.secondaryFields.length > 0 ? (
                  <BitmapGrid fields={result.secondaryFields} />
                ) : (
                  <div className="stringhex-result">
                    <span className="preview-empty">-</span>
                  </div>
                )}
              </>
            )}

            <label className="field-label">켜진 필드 번호</label>
            <div className="stringhex-result">
              {[...result.primaryFields, ...result.secondaryFields]
                .filter((f) => f.on)
                .map((f) => f.number)
                .join(', ') || <span className="preview-empty">-</span>}
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
