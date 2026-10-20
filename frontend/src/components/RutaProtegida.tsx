import { Navigate } from 'react-router-dom'
import { estaAutenticado, usuarioActual } from '../services/auth'
import type { Rol } from '../types'

/** Exige sesión; si se pasa `roles`, además que el usuario tenga uno de ellos. */
export default function RutaProtegida({ children, roles }: { children: React.ReactNode; roles?: Rol[] }) {
  if (!estaAutenticado()) return <Navigate to="/login" replace />
  const usuario = usuarioActual()
  if (roles && (!usuario || !roles.includes(usuario.rol))) return <Navigate to="/" replace />
  return <>{children}</>
}
