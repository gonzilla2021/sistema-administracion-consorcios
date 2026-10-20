import { Link } from 'react-router-dom'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import { usuarioActual } from '../services/auth'
import { ROL_LEGIBLE } from '../types'

function Dato({ etiqueta, valor }: { etiqueta: string; valor: number | string }) {
  return (
    <div className="min-w-24 rounded-xl bg-white/10 px-4 py-3 text-center">
      <p className="text-xs text-white/70">{etiqueta}</p>
      <p className="text-2xl font-bold text-white">{valor}</p>
    </div>
  )
}

function Acceso({ to, titulo, detalle }: { to: string; titulo: string; detalle: string }) {
  return (
    <Link to={to} className="card block p-5 hover:border-brand">
      <p className="font-semibold">{titulo}</p>
      <p className="mt-1 text-sm text-slate-500">{detalle}</p>
    </Link>
  )
}

export default function DashboardPage() {
  const usuario = usuarioActual()
  const { consorcios, activo } = useConsorcioActivo()
  const esSuper = usuario?.rol === 'SUPERADMINISTRADOR'
  const gestiona = esSuper || usuario?.rol === 'ADMINISTRADOR_CONSORCIO'

  return (
    <div className="space-y-8">
      <section className="flex flex-wrap items-center justify-between gap-6 rounded-3xl bg-brand p-8 shadow-lg">
        <div>
          <h1 className="text-2xl font-bold text-white">¡Hola!</h1>
          <p className="text-white/80">{usuario ? `${usuario.email} · ${ROL_LEGIBLE[usuario.rol]}` : ''}</p>
        </div>
        {gestiona && (
          <div className="flex gap-3">
            <Dato etiqueta="Consorcios" valor={consorcios.length} />
            <Dato etiqueta={activo ? `Unidades de ${activo.nombre}` : 'Unidades'} valor={activo?.cantidadUnidades ?? 0} />
          </div>
        )}
      </section>

      {gestiona ? (
        <section className="grid gap-4 sm:grid-cols-3">
          <Acceso to="/consorcios" titulo="Consorcios" detalle="Datos de los consorcios" />
          <Acceso to="/unidades" titulo="Unidades" detalle="Unidades y coeficientes del consorcio activo" />
          {esSuper && <Acceso to="/usuarios" titulo="Usuarios" detalle="Cuentas y roles" />}
        </section>
      ) : (
        <p className="text-sm text-slate-500">La consulta de tus unidades y liquidaciones estará disponible próximamente.</p>
      )}
    </div>
  )
}
