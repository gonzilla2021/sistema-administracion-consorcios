export type Rol = 'SUPERADMINISTRADOR' | 'ADMINISTRADOR_CONSORCIO' | 'PROPIETARIO' | 'INQUILINO'
export type TipoVinculo = 'PROPIETARIO' | 'INQUILINO'
export type TipoGasto = 'ORDINARIO' | 'EXTRAORDINARIO'
export type EstadoLiquidacion = 'ABIERTA' | 'CERRADA'
export type EstadoPago = 'PENDIENTE' | 'PAGADO'

// Contrato con el backend (a confirmar con quien implemente la autenticación)

/** POST /api/auth/login */
export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  email: string
  rol: Rol
}

/** GET /api/consorcios */
export interface Consorcio {
  id: string
  nombre: string
  direccion: string
  fechaAlta: string
}
