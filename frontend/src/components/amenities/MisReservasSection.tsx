import { useMemo } from 'react'
import { Cargando, ErrorCarga } from '../Estados'
import type { ReservaAmenity } from '../../types'

interface MisReservasSectionProps {
  reservas: ReservaAmenity[]
  cargando: boolean
  error: boolean
  cancelando: boolean
  onCancelar: (reserva: ReservaAmenity) => void
  onReintentar: () => void
}

export default function MisReservasSection({
  reservas,
  cargando,
  error,
  cancelando,
  onCancelar,
  onReintentar,
}: MisReservasSectionProps) {
  const reservasOrdenadas = useMemo(
    () =>
      [...reservas].sort((a, b) =>
        b.fecha.localeCompare(a.fecha),
      ),
    [reservas],
  )

  const activas = reservas.filter(
    (reserva) => reserva.estado === 'ACTIVA',
  ).length

  const canceladas = reservas.length - activas

  if (cargando) {
    return <Cargando />
  }

  if (error) {
    return <ErrorCarga onReintentar={onReintentar} />
  }

  return (
    <section className="space-y-4">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">
          Mis reservas
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          Consultá tus reservas activas y el historial de
          cancelaciones.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <Resumen titulo="Total" valor={reservas.length} />
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

      <div className="card overflow-x-auto">
        <table className="w-full min-w-[900px]">
          <thead className="bg-brand">
            <tr>
              <th className="th rounded-tl-2xl">
                Amenity
              </th>
              <th className="th">Fecha</th>
              <th className="th">Turno</th>
              <th className="th">Horario</th>
              <th className="th">Unidad</th>
              <th className="th">Estado</th>
              <th className="th rounded-tr-2xl text-right">
                Acciones
              </th>
            </tr>
          </thead>

          <tbody className="divide-y divide-slate-100">
            {reservasOrdenadas.map((reserva) => {
              const puedeCancelar =
                reserva.estado === 'ACTIVA' &&
                !esFechaPasada(reserva.fecha)

              return (
                <tr key={reserva.id}>
                  <td className="td font-medium">
                    {reserva.amenity}
                  </td>

                  <td className="td">
                    {formatearFecha(reserva.fecha)}
                  </td>

                  <td className="td">
                    {reserva.turno}
                  </td>

                  <td className="td">
                    {reserva.horaInicio} a{' '}
                    {reserva.horaFin}
                  </td>

                  <td className="td">
                    {reserva.unidad}
                  </td>

                  <td className="td">
                    <EstadoReserva reserva={reserva} />
                  </td>

                  <td className="td">
                    <div className="flex justify-end">
                      {puedeCancelar ? (
                        <button
                          className="btn-secondary"
                          type="button"
                          disabled={cancelando}
                          onClick={() =>
                            onCancelar(reserva)
                          }
                        >
                          {cancelando
                            ? 'Cancelando...'
                            : 'Cancelar'}
                        </button>
                      ) : (
                        <span className="text-sm text-slate-400">
                          Sin acciones
                        </span>
                      )}
                    </div>
                  </td>
                </tr>
              )
            })}

            {reservasOrdenadas.length === 0 && (
              <tr>
                <td
                  className="td py-10 text-center text-slate-500"
                  colSpan={7}
                >
                  Todavía no realizaste reservas de amenities.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
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
          {formatearFechaHora(reserva.fechaCancelacion)}
        </p>
      )}
    </div>
  )
}

function esFechaPasada(fecha: string) {
  const hoy = new Date()
  hoy.setHours(0, 0, 0, 0)

  const fechaReserva = new Date(`${fecha}T00:00:00`)

  return fechaReserva < hoy
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