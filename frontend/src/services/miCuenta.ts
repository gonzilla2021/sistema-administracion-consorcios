import { api } from './api'
import type { MiLiquidacion } from '../types'

export const listarMisLiquidaciones = () =>
  api<MiLiquidacion[]>('/api/mi-cuenta/liquidaciones')