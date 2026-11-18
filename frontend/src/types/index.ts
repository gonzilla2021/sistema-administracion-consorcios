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
  consorcioIds: string[]
}

/** POST /api/usuarios */
export interface UsuarioInput {
  email: string
  password: string
  rol: Rol
  consorcioIds?: string[]
}

/** GET /api/consorcios/{id}/gastos */
export interface Gasto {
  id: string
  consorcioId: string
  descripcion: string
  monto: number
  tipo: TipoGasto
  fecha: string
  comprobante: string | null
}

/** POST/PUT /api/consorcios/{id}/gastos */
export interface GastoInput {
  descripcion: string
  monto: number
  tipo: TipoGasto
  fecha: string
  comprobante?: string | null
}

/** POST /api/consorcios/{id}/liquidaciones y PATCH .../cerrar */
export interface Liquidacion {
  id: string
  consorcioId: string
  periodo: string
  estado: EstadoLiquidacion
  totalGastos: number
  cantidadUnidades: number
}

/** GET /api/consorcios/{id}/liquidaciones/{liqId}/unidades */
export interface LiquidacionUnidad {
  id: string
  unidadFuncionalId: string
  identificador: string
  importe: number
  estadoPago: EstadoPago
  fechaPago: string | null
  visibleParaInquilino: boolean
}

/** GET /api/mi-cuenta/liquidaciones */
export interface MiLiquidacion {
  liquidacionId: string
  unidadFuncionalId: string
  unidad: string
  periodo: string
  estadoLiquidacion: EstadoLiquidacion
  importe: number
  estadoPago: EstadoPago
  fechaPago: string | null
}

/** GET /api/consorcios/{id}/pagos y PATCH .../pagar | .../revertir (el id es el del detalle de liquidación) */
export interface Pago {
  id: string
  liquidacionId: string
  unidadFuncionalId: string
  identificadorUnidad: string
  importe: number
  estadoPago: EstadoPago
  fechaPago: string | null
}

/** GET /api/consorcios/{id}/personas */
export interface Persona {
  id: string
  nombre: string
  apellido: string
  dni: string
  email: string | null
  telefono: string | null
}

/** POST/PUT /api/consorcios/{id}/personas */
export interface PersonaInput {
  nombre: string
  apellido: string
  dni: string
  email?: string | null
  telefono?: string | null
}

/** GET /api/consorcios/{id}/unidades/{unidadId}/vinculaciones */
export interface Vinculacion {
  id: string
  unidadFuncionalId: string
  personaId: string
  nombre: string
  apellido: string
  tipo: TipoVinculo
  fechaInicio: string
  fechaFin: string | null
  vigente: boolean
}

/** POST .../vinculaciones */
export interface VinculacionInput {
  personaId: string
  tipo: TipoVinculo
  fechaInicio: string
  fechaFin?: string | null
}


export const moneda = (n: number) => n.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' })

export const ROL_LEGIBLE: Record<Rol, string> = {
  SUPERADMINISTRADOR: 'Superadministrador',
  ADMINISTRADOR_CONSORCIO: 'Administrador de consorcio',
  PROPIETARIO: 'Propietario',
  INQUILINO: 'Inquilino',
}

export type EstadoReservaAmenity =
  | 'ACTIVA'
  | 'CANCELADA_POR_USUARIO'

export type EstadoDisponibilidad =
  | 'DISPONIBLE'
  | 'RESERVADO'
  | 'RESERVA_PROPIA'
  | 'BLOQUEADO'

/** Amenity asociado a un consorcio */
export interface Amenity {
  id: string
  consorcioId: string
  nombre: string
  descripcion: string | null
  activo: boolean
}

/** Datos para crear o modificar un amenity */
export interface AmenityInput {
  nombre: string
  descripcion?: string | null
  activo: boolean
}

/** Turno configurado para un amenity */
export interface TurnoAmenity {
  id: string
  amenityId: string
  nombre: string
  horaInicio: string
  horaFin: string
  activo: boolean
}

/** Datos para crear o modificar un turno */
export interface TurnoAmenityInput {
  nombre: string
  horaInicio: string
  horaFin: string
  activo: boolean
}

/** Reserva de un amenity */
export interface ReservaAmenity {
  id: string
  amenityId: string
  amenity: string
  turnoId: string
  turno: string
  horaInicio: string
  horaFin: string
  unidadFuncionalId: string
  unidad: string
  fecha: string
  estado: EstadoReservaAmenity
  fechaCreacion: string
  fechaCancelacion: string | null
}

/** Datos necesarios para crear una reserva */
export interface ReservaAmenityInput {
  turnoId: string
  fecha: string
}

/** Disponibilidad de un turno para una fecha */
export interface DisponibilidadAmenity {
  turnoId: string
  turno: string
  horaInicio: string
  horaFin: string
  fecha: string
  disponible: boolean
  estado: EstadoDisponibilidad
}

/** Bloqueo administrativo */
export interface BloqueoAmenity {
  id: string
  amenityId: string
  amenity: string
  turnoId: string
  turno: string
  horaInicio: string
  horaFin: string
  fecha: string
  motivo: string
  fechaCreacion: string
  activo: boolean
}

/** Datos necesarios para bloquear un turno */
export interface BloqueoAmenityInput {
  turnoId: string
  fecha: string
  motivo: string
}
