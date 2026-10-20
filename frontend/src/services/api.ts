const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

const TOKEN_KEY = 'token'
const USER_KEY = 'usuario'

export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token)
export const clearToken = () => localStorage.removeItem(TOKEN_KEY)

/** Error de la API: `message` trae el motivo que informa el backend, si lo hay. */
export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getToken()
  const res = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  })
  if (!res.ok) {
    // Token vencido o inválido en una pantalla protegida: volver al login.
    if (res.status === 401 && !path.startsWith('/api/auth/')) {
      clearToken()
      localStorage.removeItem(USER_KEY)
      window.location.assign('/login')
    }
    let motivo = ''
    try {
      motivo = ((await res.json()) as { message?: string }).message ?? ''
    } catch {
      // respuesta sin cuerpo JSON
    }
    throw new ApiError(res.status, motivo || `Error ${res.status} en ${path}`)
  }
  return res.status === 204 ? (undefined as T) : ((await res.json()) as T)
}
