import { createContext, useContext, useState, type ReactNode } from 'react'
import { useQuery } from '@tanstack/react-query'
import { listarConsorcios } from '../services/consorcios'
import type { Consorcio } from '../types'

const KEY = 'consorcioActivo'

interface Valor {
  consorcios: Consorcio[]
  cargando: boolean
  activo: Consorcio | null
  elegir: (id: string) => void
}

const Contexto = createContext<Valor | null>(null)

/** Consorcios visibles para el usuario y el que está "activo" (el que usan las pantallas de unidades). */
export function ConsorcioActivoProvider({ children }: { children: ReactNode }) {
  const { data, isLoading } = useQuery({ queryKey: ['consorcios'], queryFn: listarConsorcios, retry: false })
  const [id, setId] = useState(() => localStorage.getItem(KEY))
  const consorcios = data ?? []
  const activo = consorcios.find((c) => c.id === id) ?? consorcios[0] ?? null

  const elegir = (nuevo: string) => {
    localStorage.setItem(KEY, nuevo)
    setId(nuevo)
  }

  return <Contexto.Provider value={{ consorcios, cargando: isLoading, activo, elegir }}>{children}</Contexto.Provider>
}

export function useConsorcioActivo() {
  const v = useContext(Contexto)
  if (!v) throw new Error('useConsorcioActivo fuera de ConsorcioActivoProvider')
  return v
}
