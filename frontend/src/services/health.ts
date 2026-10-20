import { api, ApiError } from './api'

export type EstadoConexion = 'verificando' | 'conectado' | 'sin-base' | 'sin-backend'

interface Salud {
  backend: string
  baseDeDatos: string
}

/** Consulta GET /api/health: distingue backend caído de base de datos caída. */
export async function verificarConexion(): Promise<EstadoConexion> {
  try {
    const salud = await api<Salud>('/api/health')
    return salud.baseDeDatos === 'UP' ? 'conectado' : 'sin-base'
  } catch (e) {
    // El backend responde 503 cuando no llega a la base; sin respuesta es que no está levantado.
    return e instanceof ApiError && e.status === 503 ? 'sin-base' : 'sin-backend'
  }
}
