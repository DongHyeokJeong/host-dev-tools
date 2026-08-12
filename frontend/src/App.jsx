import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './layout/Layout'
import Dashboard from './pages/Dashboard'
import StringHexConvert from './pages/StringHexConvert'
import IsoParsing from './pages/IsoParsing'
import BitmapParser from './pages/BitmapParser'
import CardSimulator from './pages/CardSimulator'
import TestDataVault from './pages/TestDataVault'
import HostNumbering from './pages/HostNumbering'

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/card-simulator" element={<CardSimulator />} />
          <Route path="/iso-parsing" element={<IsoParsing />} />
          <Route path="/bitmap-parser" element={<BitmapParser />} />
          <Route path="/host-numbering" element={<HostNumbering />} />
          <Route path="/test-data-vault" element={<TestDataVault />} />
          <Route path="/string-hex" element={<StringHexConvert />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  )
}
