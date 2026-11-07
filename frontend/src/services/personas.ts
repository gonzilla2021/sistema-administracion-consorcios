import { api } from './api'
import type { Persona, PersonaInput, Vinculacion, VinculacionInput } from '../types'

export const listarPersonas = () => api<Persona[]>('/api/personas')

export const crearPersona = (datos: PersonaInput) =>
  api<Persona>('/api/personas', { method: 'POST', body: JSON.stringify(datos) })

export const actualizarPersona = (id: string, datos: PersonaInput) =>
  api<Persona>(`/api/personas/${id}`, { method: 'PUT', body: JSON.stringify(datos) })

const baseVinc = (consorcioId: string, unidadId: string) =>
  `/api/consorcios/${consorcioId}/unidades/${unidadId}/vinculaciones`

export const listarVinculaciones = (consorcioId: string, unidadId: string) =>
  api<Vinculacion[]>(baseVinc(consorcioId, unidadId))

export const crearVinculacion = (consorcioId: string, unidadId: string, datos: VinculacionInput) =>
  api<Vinculacion>(baseVinc(consorcioId, unidadId), { method: 'POST', body: JSON.stringify(datos) })

export const finalizarVinculacion = (consorcioId: string, unidadId: string, id: string, fechaFin: string) =>
  api<Vinculacion>(`${baseVinc(consorcioId, unidadId)}/${id}/finalizar`, {
    method: 'PATCH',
    body: JSON.stringify({ fechaFin }),
  })
