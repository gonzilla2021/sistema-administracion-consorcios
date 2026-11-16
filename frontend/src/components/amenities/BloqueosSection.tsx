import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'

import { Cargando, ErrorCarga } from '../Estados'
import type {
  BloqueoAmenity,
  BloqueoAmenityInput,
  TurnoAmenity,
} from '../../types'

interface BloqueosSectionProps {
  turnos: TurnoAmenity[]
  bloqueos: BloqueoAmenity[]
  cargando: boolean
  error: boolean
  guardando: boolean
  desbloqueando: boolean
  amenityActivo: boolean
  onCrear: (datos: BloqueoAmenityInput) => void
  onDesbloquear: (bloqueo: BloqueoAmenity) => void
  onReintentar: () => void
}

const fechaLocal = (fecha: Date) => {
  const anio = fecha.getFullYear()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')

  return `${anio}-${mes}-${dia}`
}

const FORM_INICIAL: BloqueoAmenityInput = {
  turnoId: '',
  fecha: fechaLocal(new Date()),
  motivo: '',
}

export default function BloqueosSection({
  turnos,
  bloqueos,
  cargando,
  error,
  guardando,
  desbloqueando,
  amenityActivo,
  onCrear,
  onDesbloquear,
  onReintentar,
}: BloqueosSectionProps) {
  const [form, setForm] =
    useState<BloqueoAmenityInput>(FORM_INICIAL)

  const turnosActivos = useMemo(
    () => turnos.filter((turno) => turno.activo),
    [turnos],
  )

  const bloqueosOrdenados = useMemo(
    () =>
      [...bloqueos].sort((a, b) =>
        b.fecha.localeCompare(a.fecha),
      ),
    [bloqueos],
  )

  const activos = bloqueos.filter(
    (bloqueo) => bloqueo.activo,
  ).length

  const desbloqueados = bloqueos.length - activos

  const enviar = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    onCrear({
      turnoId: form.turnoId,
      fecha: form.fecha,
      motivo: form.motivo.trim(),
    })
  }

  const limpiarFormulario = () => {
    setForm({
      turnoId: '',
      fecha: fechaLocal(new Date()),
      motivo: '',
    })
  }

  return (
    <section className="space-y-4">
      <div className="card p-5">
        <div>
          <h2 className="text-lg font-semibold text-slate-900">
            Bloqueos administrativos
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            Bloqueá un turno libre por mantenimiento, reparación
            u otra causa administrativa.
          </p>
        </div>

        {!amenityActivo && (
          <p className="mt-3 text-sm text-amber-700">
            Activá el amenity para crear nuevos bloqueos.
          </p>
        )}

        {turnosActivos.length === 0 && amenityActivo && (
          <p className="mt-3 text-sm text-amber-700">
            El amenity no tiene turnos activos para bloquear.
          </p>
        )}

        <form
          className="mt-5 space-y-4"
          onSubmit={enviar}
        >
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label
                className="label"
                htmlFor="bloqueo-turno"
              >
                Turno
              </label>

              <select
                id="bloqueo-turno"
                className="input"
                required
                disabled={
                  !amenityActivo ||
                  turnosActivos.length === 0 ||
                  guardando
                }
                value={form.turnoId}
                onChange={(event) =>
                  setForm({
                    ...form,
                    turnoId: event.target.value,
                  })
                }
              >
                <option value="">Seleccionar turno</option>

                {turnosActivos.map((turno) => (
                  <option key={turno.id} value={turno.id}>
                    {turno.nombre} · {turno.horaInicio} a{' '}
                    {turno.horaFin}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label
                className="label"
                htmlFor="bloqueo-fecha"
              >
                Fecha
              </label>

              <input
                id="bloqueo-fecha"
                className="input"
                required
                type="date"
                min={fechaLocal(new Date())}
                disabled={!amenityActivo || guardando}
                value={form.fecha}
                onChange={(event) =>
                  setForm({
                    ...form,
                    fecha: event.target.value,
                  })
                }
              />
            </div>
          </div>

          <div>
            <label
              className="label"
              htmlFor="bloqueo-motivo"
            >
              Motivo
            </label>

            <textarea
              id="bloqueo-motivo"
              className="input min-h-24 resize-y"
              required
              maxLength={500}
              disabled={!amenityActivo || guardando}
              placeholder="Ejemplo: reparación de la parrilla"
              value={form.motivo}
              onChange={(event) =>
                setForm({
                  ...form,
                  motivo: event.target.value,
                })
              }
            />

            <p className="mt-1 text-right text-xs text-slate-400">
              {form.motivo.length}/500
            </p>
          </div>

          <div className="flex flex-wrap gap-2">
            <button
              className="btn-primary"
              type="submit"
              disabled={
                !amenityActivo ||
                turnosActivos.length === 0 ||
                guardando
              }
            >
              {guardando
                ? 'Bloqueando...'
                : 'Bloquear turno'}
            </button>

            <button
              className="btn-secondary"
              type="button"
              disabled={guardando}
              onClick={limpiarFormulario}
            >
              Limpiar
            </button>
          </div>
        </form>
      </div>

      {cargando && <Cargando />}

      {error && (
        <ErrorCarga onReintentar={onReintentar} />
      )}

      {!cargando && !error && (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <Resumen
              titulo="Total"
              valor={bloqueos.length}
            />

            <Resumen
              titulo="Activos"
              valor={activos}
              color="amarillo"
            />

            <Resumen
              titulo="Desbloqueados"
              valor={desbloqueados}
              color="gris"
            />
          </div>

          <div className="card overflow-x-auto">
            <table className="w-full min-w-[850px]">
              <thead className="bg-brand">
                <tr>
                  <th className="th rounded-tl-2xl">
                    Fecha
                  </th>
                  <th className="th">Turno</th>
                  <th className="th">Horario</th>
                  <th className="th">Motivo</th>
                  <th className="th">Estado</th>
                  <th className="th rounded-tr-2xl text-right">
                    Acciones
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-slate-100">
                {bloqueosOrdenados.map((bloqueo) => (
                  <tr key={bloqueo.id}>
                    <td className="td">
                      {formatearFecha(bloqueo.fecha)}
                    </td>

                    <td className="td font-medium">
                      {bloqueo.turno}
                    </td>

                    <td className="td">
                      {bloqueo.horaInicio} a{' '}
                      {bloqueo.horaFin}
                    </td>

                    <td className="td max-w-xs">
                      <p className="whitespace-normal">
                        {bloqueo.motivo}
                      </p>
                    </td>

                    <td className="td">
                      <EstadoBloqueo
                        activo={bloqueo.activo}
                      />
                    </td>

                    <td className="td">
                      <div className="flex justify-end">
                        {bloqueo.activo ? (
                          <button
                            className="btn-secondary"
                            type="button"
                            disabled={desbloqueando}
                            onClick={() =>
                              onDesbloquear(bloqueo)
                            }
                          >
                            {desbloqueando
                              ? 'Procesando...'
                              : 'Desbloquear'}
                          </button>
                        ) : (
                          <span className="text-sm text-slate-400">
                            Sin acciones
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}

                {bloqueosOrdenados.length === 0 && (
                  <tr>
                    <td
                      className="td py-10 text-center text-slate-500"
                      colSpan={6}
                    >
                      No hay bloqueos registrados para este
                      amenity.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </section>
  )
}

interface ResumenProps {
  titulo: string
  valor: number
  color?: 'normal' | 'amarillo' | 'gris'
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
    amarillo: {
      tarjeta: 'border-amber-200',
      titulo: 'text-amber-700',
      valor: 'text-amber-700',
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

function EstadoBloqueo({ activo }: { activo: boolean }) {
  return (
    <span
      className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
        activo
          ? 'bg-amber-100 text-amber-700'
          : 'bg-slate-200 text-slate-600'
      }`}
    >
      {activo ? 'Bloqueado' : 'Desbloqueado'}
    </span>
  )
}

function formatearFecha(fecha: string) {
  return new Intl.DateTimeFormat('es-AR').format(
    new Date(`${fecha}T00:00:00`),
  )
}