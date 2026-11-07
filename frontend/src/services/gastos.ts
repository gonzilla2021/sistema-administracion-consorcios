import { api } from './api'
import type { Gasto, GastoInput, Liquidacion, LiquidacionUnidad } from '../types'

const base = (consorcioId: string) => `/api/consorcios/${consorcioId}`

export const listarGastos = (consorcioId: string) => api<Gasto[]>(`${base(consorcioId)}/gastos`)

export const crearGasto = (consorcioId: string, datos: GastoInput) =>
  api<Gasto>(`${base(consorcioId)}/gastos`, { method: 'POST', body: JSON.stringify(datos) })

export const actualizarGasto = (consorcioId: string, id: string, datos: GastoInput) =>
  api<Gasto>(`${base(consorcioId)}/gastos/${id}`, { method: 'PUT', body: JSON.stringify(datos) })

export const eliminarGasto = (consorcioId: string, id: string) =>
  api<void>(`${base(consorcioId)}/gastos/${id}`, { method: 'DELETE' })

export const listarLiquidaciones = (consorcioId: string) =>
  api<Liquidacion[]>(`${base(consorcioId)}/liquidaciones`)

export const generarLiquidacion = (consorcioId: string, periodo: string) =>
  api<Liquidacion>(`${base(consorcioId)}/liquidaciones`, { method: 'POST', body: JSON.stringify({ periodo }) })

export const detalleLiquidacion = (consorcioId: string, id: string) =>
  api<LiquidacionUnidad[]>(`${base(consorcioId)}/liquidaciones/${id}/unidades`)

export const cerrarLiquidacion = (consorcioId: string, id: string) =>
  api<Liquidacion>(`${base(consorcioId)}/liquidaciones/${id}/cerrar`, { method: 'PATCH' })
