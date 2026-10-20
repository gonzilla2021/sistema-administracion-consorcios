import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { ConsorcioActivoProvider, useConsorcioActivo } from '../components/ConsorcioActivo'
import { logout, usuarioActual } from '../services/auth'
import { ROL_LEGIBLE } from '../types'

function Barra() {
  const { consorcios, activo, elegir } = useConsorcioActivo()
  const usuario = usuarioActual()
  return (
    <div className="flex items-center justify-between gap-4 border-b border-slate-200 bg-white px-6 py-3">
      <div>
        <p className="text-[10px] font-semibold uppercase tracking-widest text-slate-500">Consorcio activo</p>
        {consorcios.length > 1 ? (
          <select
            aria-label="Consorcio activo"
            className="-ml-1 cursor-pointer bg-transparent text-sm font-semibold focus:outline-none"
            value={activo?.id ?? ''}
            onChange={(e) => elegir(e.target.value)}
          >
            {consorcios.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nombre}
              </option>
            ))}
          </select>
        ) : (
          <p className="text-sm font-semibold">{activo?.nombre ?? '—'}</p>
        )}
      </div>
      {usuario && <p className="hidden text-xs text-slate-500 sm:block">{ROL_LEGIBLE[usuario.rol]}</p>}
    </div>
  )
}

export default function Layout() {
  const navigate = useNavigate()
  const usuario = usuarioActual()
  const [abierto, setAbierto] = useState(false)
  const esSuper = usuario?.rol === 'SUPERADMINISTRADOR'
  const gestiona = esSuper || usuario?.rol === 'ADMINISTRADOR_CONSORCIO'

  const salir = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const enlace = ({ isActive }: { isActive: boolean }) =>
    `block rounded-lg px-3 py-2 text-sm font-medium ${
      isActive ? 'bg-gold text-white' : 'text-white/80 hover:bg-white/10'
    }`

  return (
    <ConsorcioActivoProvider>
      <div className="min-h-screen md:flex">
        <aside
          className={`${abierto ? 'flex' : 'hidden'} w-full shrink-0 flex-col bg-brand md:sticky md:top-0 md:flex md:h-screen md:w-60`}
        >
          <div className="border-b border-white/10 px-5 py-5">
            <p className="text-lg font-bold text-white">Consorcios</p>
            <p className="truncate text-xs text-white/70">{usuario?.email}</p>
          </div>
          <nav className="flex-1 space-y-1 px-3 py-4" onClick={() => setAbierto(false)}>
            <NavLink to="/" end className={enlace}>
              Tablero
            </NavLink>
            {gestiona && (
              <>
                <NavLink to="/consorcios" className={enlace}>
                  Consorcios
                </NavLink>
                <NavLink to="/unidades" className={enlace}>
                  Unidades
                </NavLink>
              </>
            )}
            {esSuper && (
              <NavLink to="/usuarios" className={enlace}>
                Usuarios
              </NavLink>
            )}
          </nav>
          <NavLink to="/cuenta" className={enlace} onClick={() => setAbierto(false)}>
            Cambiar contraseña
          </NavLink>
          <button
            onClick={salir}
            className="cursor-pointer border-t border-white/10 px-5 py-4 text-left text-sm text-white/80 hover:text-white"
          >
            Cerrar sesión
          </button>
        </aside>

        <div className="min-w-0 flex-1">
          <button
            className="w-full cursor-pointer bg-brand px-4 py-2 text-left text-sm font-medium text-white md:hidden"
            onClick={() => setAbierto((a) => !a)}
          >
            ☰ Menú
          </button>
          <Barra />
          <main className="mx-auto max-w-6xl px-6 py-8">
            <Outlet />
          </main>
        </div>
      </div>
    </ConsorcioActivoProvider>
  )
}
