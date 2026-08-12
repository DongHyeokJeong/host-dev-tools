import React, { useEffect, useRef, useState } from 'react'
import { convertStringToHex } from '../api/stringHexApi'

const EXAMPLE = 'BASDKJNCVLS1213(03)(00)(F2)(3C)(24)'

// 07_String_Hex변환 화면 (B안: 인라인 하이라이트 스타일)
export default function StringHexConvert() {
  const [rawInput, setRawInput] = useState(EXAMPLE)
  const [tokens, setTokens] = useState([])
  const [warnings, setWarnings] = useState([])
  const [hoveredIndex, setHoveredIndex] = useState(null)
  const [error, setError] = useState(null)
  const debounceRef = useRef(null)

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current)

    if (!rawInput) {
      setTokens([])
      setWarnings([])
      setError(null)
      return
    }

    debounceRef.current = setTimeout(async () => {
      try {
        const data = await convertStringToHex(rawInput, 'ASCII')
        setTokens(data.tokens || [])
        setWarnings(data.warnings || [])
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
        <h1>String → 16진수 변환</h1>
        <p className="page-desc">ASCII 문자와 (XX) 형태의 Hex 리터럴이 혼합된 문자열을 Hex로 변환합니다</p>
      </div>

      <div className="card stringhex-card">
        <label className="field-label">원문 입력</label>
        <textarea
          className="stringhex-input"
          rows={2}
          value={rawInput}
          onChange={(e) => setRawInput(e.target.value)}
          placeholder={EXAMPLE}
          spellCheck={false}
        />

        <label className="field-label">
          입력 데이터{' '}
          <span className="field-label-hint">(글자에 마우스를 올리면 결과의 대응 Hex가 함께 강조됩니다)</span>
        </label>
        <div className="stringhex-preview">
          {tokens.length === 0 && <span className="preview-empty">입력한 내용이 여기에 표시됩니다</span>}
          {tokens.map((t, idx) => (
            <span
              key={idx}
              className={
                'tok ' +
                (t.type === 'LITERAL' ? 'tok-literal' : 'tok-ascii') +
                (idx === hoveredIndex ? ' tok-hover' : '')
              }
              onMouseEnter={() => setHoveredIndex(idx)}
              onMouseLeave={() => setHoveredIndex(null)}
              title={t.type === 'LITERAL' ? `Hex 리터럴 ${t.sourceText} → ${t.hex}` : `'${t.sourceText}' → ${t.hex}`}
            >
              {t.sourceText}
            </span>
          ))}
        </div>

        <label className="field-label">결과</label>
        <div className="stringhex-result">
          {tokens.length === 0 && <span className="preview-empty">-</span>}
          {tokens.map((t, idx) => (
            <span
              key={idx}
              className={'hex-byte' + (idx === hoveredIndex ? ' tok-hover' : '')}
              onMouseEnter={() => setHoveredIndex(idx)}
              onMouseLeave={() => setHoveredIndex(null)}
            >
              {t.hex}
            </span>
          ))}
        </div>

        <label className="field-label">
          ASCII 디코딩{' '}
          <span className="field-label-hint">
            (각 Hex 바이트를 ASCII 기준으로 되돌린 문자입니다. 출력 불가능한 값은 '.'으로 표시됩니다)
          </span>
        </label>
        <div className="stringhex-result">
          {tokens.length === 0 && <span className="preview-empty">-</span>}
          {tokens.map((t, idx) => (
            <span
              key={idx}
              className={'hex-byte decoded-char' + (idx === hoveredIndex ? ' tok-hover' : '')}
              onMouseEnter={() => setHoveredIndex(idx)}
              onMouseLeave={() => setHoveredIndex(null)}
              title={`${t.hex} → ${t.decodedChar}`}
            >
              {t.decodedChar}
            </span>
          ))}
        </div>

        <div className="legend">
          <span className="legend-swatch" />( ) 안의 값은 Hex 리터럴로 그대로 사용되며 갈색으로 구분됩니다
        </div>

        {warnings.length > 0 && (
          <div className="warning-box">
            {warnings.map((w, i) => (
              <div key={i}>⚠ {w}</div>
            ))}
          </div>
        )}
        {error && <div className="error">{error}</div>}
      </div>
    </div>
  )
}
