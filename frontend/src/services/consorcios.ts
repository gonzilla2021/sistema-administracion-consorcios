import { api } from './api'
import type { Consorcio, ConsorcioInput, Unidad, UnidadInput, Usuario, UsuarioInput } from '../types'

export const listarConsorcios = () => api<Consorcio[]>('/api/consorcios')

export const crearConsorcio = (datos: ConsorcioInput) =>
  api<Consorcio>('/api/consorcios', { method: 'POST', body: JSON.stringify(datos) })

export const actualizarConsorcio = (id: string, datos: ConsorcioInput) =>
  api<Consorcio>(`/api/consorcios/${id}`, { method: 'PUT', body: JSON.stringify(datos) })

export const listarUnidades = (consorcioId: string) => api<Unidad[]>(`/api/consorcios/${consorcioId}/unidades`)

export const reemplazarUnidades = (consorcioId: string, unidades: UnidadInput[]) =>
  api<Unidad[]>(`/api/consorcios/${consorcioId}/unidades`, { method: 'PUT', body: JSON.stringify({ unidades }) })

export const listarUsuarios = () => api<Usuario[]>('/api/usuarios')

export const crearUsuario = (datos: UsuarioInput) =>
  api<Usuario>('/api/usuarios', { method: 'POST', body: JSON.stringify(datos) })

export const cambiarEstadoUsuario = (id: string, activo: boolean) =>
  api<Usuario>(`/api/usuarios/${id}`, { method: 'PATCH', body: JSON.stringify({ activo }) })
