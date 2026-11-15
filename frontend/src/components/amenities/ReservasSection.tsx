import { Cargando, ErrorCarga } from '../Estados'
import type { ReservaAmenity } from '../../types'

interface ReservasSectionProps {
  desde: string
  hasta: string
  errorRango: string
  reservas: ReservaAmenity[]
  cargando: boolean
  error: boolean
  onCambiarDesde: (fecha: string) => void
  onCambiarHasta: (fecha: string) => void
  onReintentar: () => void
}

export default function ReservasSection({
  desde,
  hasta,
  errorRango,
  reservas,
  cargando,
  error,
  onCambiarDesde,
  onCambiarHasta,
  onReintentar,
}: ReservasSectionProps) {
  const activas = reservas.filter(
    (reserva) => reserva.estado === 'ACTIVA',
  ).length

  const canceladas = reservas.filter(
    (reserva) =>
      reserva.estado === 'CANCELADA_POR_USUARIO',
  ).length

  return (
    <section className="space-y-4">
      <div className="card p-5">
        <div className="flex flex-col justify-between gap-4 lg:flex-row lg:items-end">
          <div>
            <h2 className="text-lg font-semibold text-slate-900">
              Reservas
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Consultá las reservas activas y canceladas del
              amenity seleccionado.
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div>
              <label
                className="label"
                htmlFor="reservas-desde"
              >
                Desde
              </label>

              <input
                id="reservas-desde"
                className="input"
                type="date"
                value={desde}
                onChange={(event) =>
                  onCambiarDesde(event.target.value)
                }
              />
            </div>

            <div>
              <label
                className="label"
                htmlFor="reservas-hasta"
              >
                Hasta
              </label>

              <input
                id="reservas-hasta"
                className="input"
                type="date"
                value={hasta}
                onChange={(event) =>
                  onCambiarHasta(event.target.value)
                }
              />
            </div>
          </div>
        </div>

        {errorRango && (
          <p className="mt-3 text-sm text-red-600">
            {errorRango}
          </p>
        )}
      </div>

      {!errorRango && cargando && <Cargando />}

      {!errorRango && error && (
        <ErrorCarga onReintentar={onReintentar} />
      )}

      {!errorRango && !cargando && !error && (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <Resumen
              titulo="Total"
              valor={reservas.length}
            />

            <Resumen
              titulo="Activas"
              valor={activas}
              color="verde"
            />

            <Resumen
              titulo="Canceladas"
              valor={canceladas}
              color="gris"
            />
          </div>

          <TablaReservas reservas={reservas} />
        </>
      )}
    </section>
  )
}

interface ResumenProps {
  titulo: string
  valor: number
  color?: 'normal' | 'verde' | 'gris'
}

function Resumen({
  titulo,
  valor,
  color = 'normal',
}: ResumenProps) {
  const estilos = {
    normal: {
      tarjeta: '',
      titulo: 'text-slate-500',
      valor: 'text-slate-900',
    },
    verde: {
      tarjeta: 'border-emerald-200',
      titulo: 'text-emerald-700',
      valor: 'text-emerald-700',
    },
    gris: {
      tarjeta: 'border-slate-200',
      titulo: 'text-slate-500',
      valor: 'text-slate-600',
    },
  }

  const estilo = estilos[color]

  return (
    <article className={`card p-5 ${estilo.tarjeta}`}>
      <p
        className={`text-xs font-semibold uppercase tracking-wide ${estilo.titulo}`}
      >
        {titulo}
      </p>

      <p className={`mt-2 text-2xl font-bold ${estilo.valor}`}>
        {valor}
      </p>
    </article>
  )
}

function TablaReservas({
  reservas,
}: {
  reservas: ReservaAmenity[]
}) {
  return (
    <div className="card overflow-x-auto">
      <table className="w-full min-w-[850px]">
        <thead className="bg-brand">
          <tr>
            <th className="th rounded-tl-2xl">Fecha</th>
            <th className="th">Turno</th>
            <th className="th">Horario</th>
            <th className="th">Unidad</th>
            <th className="th">Estado</th>
            <th className="th rounded-tr-2xl">
              Registrada
            </th>
          </tr>
        </thead>

        <tbody className="divide-y divide-slate-100">
          {reservas.map((reserva) => (
            <tr key={reserva.id}>
              <td className="td">
                {formatearFecha(reserva.fecha)}
              </td>

              <td className="td font-medium">
                {reserva.turno}
              </td>

              <td className="td">
                {reserva.horaInicio} a {reserva.horaFin}
              </td>

              <td className="td">{reserva.unidad}</td>

              <td className="td">
                <EstadoReserva reserva={reserva} />
              </td>

              <td className="td">
                {formatearFechaHora(
                  reserva.fechaCreacion,
                )}
              </td>
            </tr>
          ))}

          {reservas.length === 0 && (
            <tr>
              <td
                className="td py-10 text-center text-slate-500"
                colSpan={6}
              >
                No hay reservas para el rango seleccionado.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}

function EstadoReserva({
  reserva,
}: {
  reserva: ReservaAmenity
}) {
  const activa = reserva.estado === 'ACTIVA'

  return (
    <div>
      <span
        className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
          activa
            ? 'bg-emerald-100 text-emerald-700'
            : 'bg-slate-200 text-slate-600'
        }`}
      >
        {activa ? 'Activa' : 'Cancelada'}
      </span>

      {!activa && reserva.fechaCancelacion && (
        <p className="mt-1 text-xs text-slate-500">
          Cancelada el{' '}
          {formatearFechaHora(reserva.fechaCancelacion)}
        </p>
      )}
    </div>
  )
}

function formatearFecha(fecha: string) {
  return new Intl.DateTimeFormat('es-AR').format(
    new Date(`${fecha}T00:00:00`),
  )
}

function formatearFechaHora(fecha: string | null) {
  if (!fecha) {
    return 'Sin registrar'
  }

  return new Intl.DateTimeFormat('es-AR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(fecha))
}