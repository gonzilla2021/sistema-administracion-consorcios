export type Rol = 'SUPERADMINISTRADOR' | 'ADMINISTRADOR_CONSORCIO' | 'PROPIETARIO' | 'INQUILINO'
export type TipoVinculo = 'PROPIETARIO' | 'INQUILINO'
export type TipoGasto = 'ORDINARIO' | 'EXTRAORDINARIO'
export type EstadoLiquidacion = 'ABIERTA' | 'CERRADA'
export type EstadoPago = 'PENDIENTE' | 'PAGADO'

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
  cantidadUnidades: number
}

/** POST /api/consorcios, PUT /api/consorcios/{id} */
export interface ConsorcioInput {
  nombre: string
  direccion: string
}

/** GET /api/consorcios/{id}/unidades */
export interface Unidad {
  id: string
  identificador: string
  coeficientePropiedad: number
}

/** PUT /api/consorcios/{id}/unidades (conjunto completo; los coeficientes deben sumar 100) */
export interface UnidadInput {
  identificador: string
  coeficientePropiedad: number
}

/** GET /api/usuarios (solo superadministrador) */
export interface Usuario {
  id: string
  email: string
  rol: Rol
  activo: boolean
  fechaAlta: string
  personaId: string | null
  consorcioId: string | null
}

/** POST /api/usuarios */
export interface UsuarioInput {
  email: string
  password: string
  rol: Rol
  consorcioId?: string | null
}

export const ROL_LEGIBLE: Record<Rol, string> = {
  SUPERADMINISTRADOR: 'Superadministrador',
  ADMINISTRADOR_CONSORCIO: 'Administrador de consorcio',
  PROPIETARIO: 'Propietario',
  INQUILINO: 'Inquilino',
}
