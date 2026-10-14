import { Navigate } from 'react-router-dom'
import { estaAutenticado } from '../services/auth'

export default function RutaProtegida({ children }: { children: React.ReactNode }) {
  return estaAutenticado() ? <>{children}</> : <Navigate to="/login" replace />
}
