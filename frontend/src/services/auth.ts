import { api, clearToken, getToken, setToken } from './api'
import type { LoginRequest, LoginResponse, Rol } from '../types'

const USER_KEY = 'usuario'

export interface SesionUsuario {
  email: string
  rol: Rol
}

export async function login(datos: LoginRequest): Promise<SesionUsuario> {
  const res = await api<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(datos),
  })
  setToken(res.token)
  const sesion: SesionUsuario = { email: res.email, rol: res.rol }
  localStorage.setItem(USER_KEY, JSON.stringify(sesion))
  return sesion
}

export function logout() {
  clearToken()
  localStorage.removeItem(USER_KEY)
}

export function estaAutenticado() {
  return getToken() !== null
}

export function usuarioActual(): SesionUsuario | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as SesionUsuario
  } catch {
    return null
  }
}
