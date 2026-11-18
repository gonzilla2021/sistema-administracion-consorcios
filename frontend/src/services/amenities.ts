import { api } from './api'
import type {
  Amenity,
  AmenityInput,
  BloqueoAmenity,
  BloqueoAmenityInput,
  DisponibilidadAmenity,
  ReservaAmenity,
  ReservaAmenityInput,
  TurnoAmenity,
  TurnoAmenityInput,
} from '../types'

/* 
  Administración de amenities
  */

export const listarAmenities = (consorcioId: string) =>
  api<Amenity[]>(
    `/api/consorcios/${consorcioId}/amenities`,
  )

export const obtenerAmenity = (
  consorcioId: string,
  amenityId: string,
) =>
  api<Amenity>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}`,
  )

export const crearAmenity = (
  consorcioId: string,
  datos: AmenityInput,
) =>
  api<Amenity>(
    `/api/consorcios/${consorcioId}/amenities`,
    {
      method: 'POST',
      body: JSON.stringify(datos),
    },
  )

export const actualizarAmenity = (
  consorcioId: string,
  amenityId: string,
  datos: AmenityInput,
) =>
  api<Amenity>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}`,
    {
      method: 'PUT',
      body: JSON.stringify(datos),
    },
  )

export const cambiarEstadoAmenity = (
  consorcioId: string,
  amenityId: string,
  activo: boolean,
) =>
  api<Amenity>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}/estado`,
    {
      method: 'PATCH',
      body: JSON.stringify({ activo }),
    },
  )

/* =========================================================
 * Administración de turnos
 * ======================================================= */

export const listarTurnosAmenity = (
  consorcioId: string,
  amenityId: string,
) =>
  api<TurnoAmenity[]>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}/turnos`,
  )

export const crearTurnoAmenity = (
  consorcioId: string,
  amenityId: string,
  datos: TurnoAmenityInput,
) =>
  api<TurnoAmenity>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}/turnos`,
    {
      method: 'POST',
      body: JSON.stringify(datos),
    },
  )

export const actualizarTurnoAmenity = (
  consorcioId: string,
  amenityId: string,
  turnoId: string,
  datos: TurnoAmenityInput,
) =>
  api<TurnoAmenity>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}/turnos/${turnoId}`,
    {
      method: 'PUT',
      body: JSON.stringify(datos),
    },
  )

export const cambiarEstadoTurnoAmenity = (
  consorcioId: string,
  amenityId: string,
  turnoId: string,
  activo: boolean,
) =>
  api<TurnoAmenity>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}/turnos/${turnoId}/estado`,
    {
      method: 'PATCH',
      body: JSON.stringify({ activo }),
    },
  )

/* =========================================================
 * Reservas y disponibilidad para residentes
 * ======================================================= */

export const listarMisAmenities = () =>
  api<Amenity[]>('/api/mi-cuenta/amenities')

export const consultarDisponibilidadAmenity = (
  amenityId: string,
  desde: string,
  hasta: string,
) => {
  const parametros = new URLSearchParams({
    desde,
    hasta,
  })

  return api<DisponibilidadAmenity[]>(
    `/api/mi-cuenta/amenities/${amenityId}/disponibilidad?${parametros}`,
  )
}

export const crearReservaAmenity = (
  amenityId: string,
  datos: ReservaAmenityInput,
) =>
  api<ReservaAmenity>(
    `/api/mi-cuenta/amenities/${amenityId}/reservas`,
    {
      method: 'POST',
      body: JSON.stringify(datos),
    },
  )

export const listarMisReservasAmenity = () =>
  api<ReservaAmenity[]>('/api/mi-cuenta/reservas')

export const cancelarReservaAmenity = (
  reservaId: string,
) =>
  api<ReservaAmenity>(
    `/api/mi-cuenta/reservas/${reservaId}/cancelar`,
    {
      method: 'PATCH',
    },
  )

/* =========================================================
 * Consulta administrativa de reservas
 * ======================================================= */

export const listarReservasAmenity = (
  consorcioId: string,
  amenityId: string,
  desde: string,
  hasta: string,
) => {
  const parametros = new URLSearchParams({
    desde,
    hasta,
  })

  return api<ReservaAmenity[]>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}/reservas?${parametros}`,
  )
}

/* =========================================================
 * Bloqueos administrativos
 * ======================================================= */

export const listarBloqueosAmenity = (
  consorcioId: string,
  amenityId: string,
) =>
  api<BloqueoAmenity[]>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}/bloqueos`,
  )

export const crearBloqueoAmenity = (
  consorcioId: string,
  amenityId: string,
  datos: BloqueoAmenityInput,
) =>
  api<BloqueoAmenity>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}/bloqueos`,
    {
      method: 'POST',
      body: JSON.stringify(datos),
    },
  )

export const desbloquearAmenity = (
  consorcioId: string,
  amenityId: string,
  bloqueoId: string,
) =>
  api<BloqueoAmenity>(
    `/api/consorcios/${consorcioId}/amenities/${amenityId}/bloqueos/${bloqueoId}/desbloquear`,
    {
      method: 'PATCH',
    },
  )