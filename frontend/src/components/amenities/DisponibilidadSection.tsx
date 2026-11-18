import { useMemo } from 'react'
import { Cargando, ErrorCarga } from '../Estados'
import type { DisponibilidadAmenity } from '../../types'

interface DisponibilidadSectionProps {
  desde: string
  hasta: string
  disponibilidad: DisponibilidadAmenity[]
  cargando: boolean
  error: boolean
  errorRango: string
  reservando: boolean
  amenitySeleccionado: boolean
  onCambiarDesde: (fecha: string) => void
  onCambiarHasta: (fecha: string) => void
  onReservar: (disponibilidad: DisponibilidadAmenity) => void
  onReintentar: () => void
}

export default function DisponibilidadSection({
  desde,
  hasta,
  disponibilidad,
  cargando,
  error,
  errorRango,
  reservando,
  amenitySeleccionado,
  onCambiarDesde,
  onCambiarHasta,
  onReservar,
  onReintentar,
}: DisponibilidadSectionProps) {
  const fechasAgrupadas = useMemo(() => {
    const grupos = new Map<
      string,
      DisponibilidadAmenity[]
    >()

    for (const item of disponibilidad) {
      const elementos = grupos.get(item.fecha) ?? []
      elementos.push(item)
      grupos.set(item.fecha, elementos)
    }

    return [...grupos.entries()].sort(([fechaA], [fechaB]) =>
      fechaA.localeCompare(fechaB),
    )
  }, [disponibilidad])

  return (
    <section className="space-y-4">
      <div className="card p-5">
        <div className="flex flex-col justify-between gap-4 lg:flex-row lg:items-end">
          <div>
            <h2 className="text-lg font-semibold text-slate-900">
              Disponibilidad
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Consultá turnos disponibles en un rango máximo de
              31 días.
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div>
              <label
                className="label"
                htmlFor="disponibilidad-desde"
              >
                Desde
              </label>

              <input
                id="disponibilidad-desde"
                className="input"
                type="date"
                value={desde}
                disabled={!amenitySeleccionado}
                onChange={(event) =>
                  onCambiarDesde(event.target.value)
                }
              />
            </div>

            <div>
              <label
                className="label"
                htmlFor="disponibilidad-hasta"
              >
                Hasta
              </label>

              <input
                id="disponibilidad-hasta"
                className="input"
                type="date"
                value={hasta}
                disabled={!amenitySeleccionado}
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

      {!amenitySeleccionado && (
        <div className="card p-6 text-center text-slate-500">
          Seleccioná un amenity para consultar su disponibilidad.
        </div>
      )}

      {amenitySeleccionado &&
        !errorRango &&
        cargando && <Cargando />}

      {amenitySeleccionado &&
        !errorRango &&
        error && (
          <ErrorCarga onReintentar={onReintentar} />
        )}

      {amenitySeleccionado &&
        !errorRango &&
        !cargando &&
        !error && (
          <>
            <LeyendaDisponibilidad />

            <div className="space-y-4">
              {fechasAgrupadas.map(([fecha, turnos]) => (
                <article
                  key={fecha}
                  className="card overflow-hidden"
                >
                  <header className="border-b border-slate-100 bg-slate-50 px-5 py-3">
                    <h3 className="font-semibold text-slate-900">
                      {formatearFechaLarga(fecha)}
                    </h3>
                  </header>

                  <div className="grid gap-3 p-4 md:grid-cols-2">
                    {turnos.map((turno) => (
                      <TurnoDisponible
                        key={`${turno.turnoId}-${turno.fecha}`}
                        disponibilidad={turno}
                        reservando={reservando}
                        onReservar={onReservar}
                      />
                    ))}
                  </div>
                </article>
              ))}

              {fechasAgrupadas.length === 0 && (
                <div className="card p-8 text-center text-slate-500">
                  No se encontraron turnos activos para el rango
                  seleccionado.
                </div>
              )}
            </div>
          </>
        )}
    </section>
  )
}

interface TurnoDisponibleProps {
  disponibilidad: DisponibilidadAmenity
  reservando: boolean
  onReservar: (disponibilidad: DisponibilidadAmenity) => void
}

function TurnoDisponible({
  disponibilidad,
  reservando,
  onReservar,
}: TurnoDisponibleProps) {
  const estilos = {
    DISPONIBLE:
      'border-emerald-200 bg-emerald-50',
    RESERVADO:
      'border-slate-200 bg-slate-50',
    RESERVA_PROPIA:
      'border-blue-200 bg-blue-50',
    BLOQUEADO:
      'border-amber-200 bg-amber-50',
  }

  return (
    <div
      className={`rounded-xl border p-4 ${
        estilos[disponibilidad.estado]
      }`}
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="font-semibold text-slate-900">
            {disponibilidad.turno}
          </p>

          <p className="mt-1 text-sm text-slate-600">
            {disponibilidad.horaInicio} a{' '}
            {disponibilidad.horaFin}
          </p>
        </div>

        <EstadoDisponibilidad
          estado={disponibilidad.estado}
        />
      </div>

      {disponibilidad.disponible && (
        <button
          className="btn-primary mt-4"
          type="button"
          disabled={reservando}
          onClick={() => onReservar(disponibilidad)}
        >
          {reservando ? 'Reservando...' : 'Reservar'}
        </button>
      )}

      {disponibilidad.estado === 'RESERVA_PROPIA' && (
        <p className="mt-3 text-sm text-blue-700">
          Este turno ya está reservado por tu cuenta.
        </p>
      )}

      {disponibilidad.estado === 'BLOQUEADO' && (
        <p className="mt-3 text-sm text-amber-700">
          El turno no está disponible por un bloqueo
          administrativo.
        </p>
      )}
    </div>
  )
}

function EstadoDisponibilidad({
  estado,
}: {
  estado: DisponibilidadAmenity['estado']
}) {
  const configuracion = {
    DISPONIBLE: {
      texto: 'Disponible',
      clase: 'bg-emerald-100 text-emerald-700',
    },
    RESERVADO: {
      texto: 'Reservado',
      clase: 'bg-slate-200 text-slate-600',
    },
    RESERVA_PROPIA: {
      texto: 'Reserva propia',
      clase: 'bg-blue-100 text-blue-700',
    },
    BLOQUEADO: {
      texto: 'Bloqueado',
      clase: 'bg-amber-100 text-amber-700',
    },
  }

  const opcion = configuracion[estado]

  return (
    <span
      className={`inline-flex shrink-0 rounded-full px-2 py-1 text-xs font-semibold ${opcion.clase}`}
    >
      {opcion.texto}
    </span>
  )
}

function LeyendaDisponibilidad() {
  return (
    <div className="card flex flex-wrap gap-3 p-4">
      <ElementoLeyenda
        texto="Disponible"
        clase="bg-emerald-500"
      />
      <ElementoLeyenda
        texto="Reservado"
        clase="bg-slate-400"
      />
      <ElementoLeyenda
        texto="Reserva propia"
        clase="bg-blue-500"
      />
      <ElementoLeyenda
        texto="Bloqueado"
        clase="bg-amber-500"
      />
    </div>
  )
}

function ElementoLeyenda({
  texto,
  clase,
}: {
  texto: string
  clase: string
}) {
  return (
    <span className="flex items-center gap-2 text-sm text-slate-600">
      <span
        className={`h-3 w-3 rounded-full ${clase}`}
      />
      {texto}
    </span>
  )
}

function formatearFechaLarga(fecha: string) {
  const texto = new Intl.DateTimeFormat('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(new Date(`${fecha}T00:00:00`))

  return texto.charAt(0).toUpperCase() + texto.slice(1)
}