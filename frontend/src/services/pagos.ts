import { api } from './api'
import type { Pago } from '../types'

const base = (consorcioId: string) => `/api/consorcios/${consorcioId}/pagos`

export const listarPagos = (consorcioId: string) => api<Pago[]>(base(consorcioId))

export const registrarPago = (consorcioId: string, detalleId: string, fechaPago: string) =>
  api<Pago>(`${base(consorcioId)}/${detalleId}/pagar`, { method: 'PATCH', body: JSON.stringify({ fechaPago }) })

export const revertirPago = (consorcioId: string, detalleId: string) =>
  api<Pago>(`${base(consorcioId)}/${detalleId}/revertir`, { method: 'PATCH' })
