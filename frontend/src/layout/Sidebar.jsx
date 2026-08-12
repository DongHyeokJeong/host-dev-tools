import React from 'react'
import { NavLink } from 'react-router-dom'

const MENU_ITEMS = [
  { path: '/', label: '대시보드' },
  { path: '/card-simulator', label: '카드사 시뮬레이터' },
  { path: '/iso-parsing', label: 'ISO 전문 파싱' },
  { path: '/bitmap-parser', label: '비트맵 파서' },
  { path: '/host-numbering', label: '호스트 채번' },
  { path: '/test-data-vault', label: '테스트 데이터함' },
  { path: '/string-hex', label: 'String/Hex 변환' },
]

export default function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar-title">HOST 개발 도구</div>
      <nav className="sidebar-nav">
        {MENU_ITEMS.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === '/'}
            className={({ isActive }) => 'sidebar-link' + (isActive ? ' active' : '')}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
