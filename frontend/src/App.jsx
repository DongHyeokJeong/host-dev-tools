import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './layout/Layout'
import Dashboard from './pages/Dashboard'
import Placeholder from './pages/Placeholder'
import StringHexConvert from './pages/StringHexConvert'
import IsoParsing from './pages/IsoParsing'
import BitmapParser from './pages/BitmapParser'
import CardSimulator from './pages/CardSimulator'
import TestDataVault from './pages/TestDataVault'

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/card-simulator" element={<CardSimulator />} />
          <Route path="/iso-parsing" element={<IsoParsing />} />
          <Route path="/bitmap-parser" element={<BitmapParser />} />
          <Route
            path="/host-numbering"
            element={<Placeholder title="호스트 채번" description="운영·테스트 환경 대조 후 미사용 코드/포트 추천" />}
          />
          <Route path="/test-data-vault" element={<TestDataVault />} />
          <Route path="/string-hex" element={<StringHexConvert />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  )
}
