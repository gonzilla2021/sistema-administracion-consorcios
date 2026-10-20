import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import RutaProtegida from './components/RutaProtegida'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import ConsorciosPage from './pages/ConsorciosPage'
import UnidadesPage from './pages/UnidadesPage'
import UsuariosPage from './pages/UsuariosPage'
import CuentaPage from './pages/CuentaPage'
import type { Rol } from './types'

const GESTION: Rol[] = ['SUPERADMINISTRADOR', 'ADMINISTRADOR_CONSORCIO']

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <RutaProtegida>
            <Layout />
          </RutaProtegida>
        }
      >
        <Route path="/" element={<DashboardPage />} />
        <Route path="/cuenta" element={<CuentaPage />} />
        <Route
          path="/consorcios"
          element={
            <RutaProtegida roles={GESTION}>
              <ConsorciosPage />
            </RutaProtegida>
          }
        />
        <Route
          path="/unidades"
          element={
            <RutaProtegida roles={GESTION}>
              <UnidadesPage />
            </RutaProtegida>
          }
        />
        <Route
          path="/usuarios"
          element={
            <RutaProtegida roles={['SUPERADMINISTRADOR']}>
              <UsuariosPage />
            </RutaProtegida>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
