import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { api } from '../services/api'
import { logout, usuarioActual } from '../services/auth'
import type { Consorcio } from '../types'

const ROL_LEGIBLE = {
  SUPERADMINISTRADOR: 'Superadministrador',
  ADMINISTRADOR_CONSORCIO: 'Administrador de consorcio',
  PROPIETARIO: 'Propietario',
  INQUILINO: 'Inquilino',
} as const

export default function DashboardPage() {
  const navigate = useNavigate()
  const usuario = usuarioActual()

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['consorcios'],
    queryFn: () => api<Consorcio[]>('/api/consorcios'),
    retry: false,
  })

  const salir = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200">
        <div className="max-w-5xl mx-auto px-4 py-3 flex items-center justify-between">
          <h1 className="font-semibold text-slate-900">Administración de Consorcios</h1>
          <div className="flex items-center gap-4 text-sm">
            {usuario && (
              <span className="text-slate-600">
                {usuario.email} · {ROL_LEGIBLE[usuario.rol]}
              </span>
            )}
            <button onClick={salir} className="text-indigo-600 hover:underline cursor-pointer">
              Salir
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-8">
        <h2 className="text-lg font-semibold text-slate-900 mb-4">Consorcios</h2>

        {isLoading && <p className="text-sm text-slate-500">Cargando…</p>}

        {isError && (
          <div role="alert" className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
            <p>No se pudieron cargar los consorcios. Puede que el backend no esté disponible todavía.</p>
            <button onClick={() => refetch()} className="mt-2 underline cursor-pointer">
              Reintentar
            </button>
          </div>
        )}

        {data && data.length === 0 && <p className="text-sm text-slate-500">Todavía no hay consorcios cargados.</p>}

        {data && data.length > 0 && (
          <ul className="grid gap-3 sm:grid-cols-2">
            {data.map((c) => (
              <li key={c.id} className="bg-white border border-slate-200 rounded-xl p-4">
                <p className="font-medium text-slate-900">{c.nombre}</p>
                <p className="text-sm text-slate-500">{c.direccion}</p>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  )
}
