import { FormEvent, useEffect, useState } from 'react'
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { Cargando, ErrorCarga } from '../components/Estados'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import {
  actualizarAmenity,
  actualizarTurnoAmenity,
  cambiarEstadoAmenity,
  cambiarEstadoTurnoAmenity,
  crearAmenity,
  crearTurnoAmenity,
  listarAmenities,
  listarTurnosAmenity,
} from '../services/amenities'
import { ApiError } from '../services/api'
import type {
  Amenity,
  AmenityInput,
  TurnoAmenity,
  TurnoAmenityInput,
} from '../types'

const AMENITY_INICIAL: AmenityInput = {
  nombre: 'SUM + Parrilla',
  descripcion: '',
  activo: true,
}

const TURNO_INICIAL: TurnoAmenityInput = {
  nombre: '',
  horaInicio: '',
  horaFin: '',
  activo: true,
}

export default function AmenitiesPage() {
  const { activo } = useConsorcioActivo()
  const queryClient = useQueryClient()

  const consorcioId = activo?.id ?? ''

  const [amenitySeleccionadoId, setAmenitySeleccionadoId] =
    useState<string | null>(null)

  const [formAmenity, setFormAmenity] =
    useState<AmenityInput>(AMENITY_INICIAL)

  const [amenityEditandoId, setAmenityEditandoId] =
    useState<string | null>(null)

  const [formTurno, setFormTurno] =
    useState<TurnoAmenityInput>(TURNO_INICIAL)

  const [turnoEditandoId, setTurnoEditandoId] =
    useState<string | null>(null)

  const [mensaje, setMensaje] = useState('')
  const [error, setError] = useState('')

  const amenities = useQuery({
    queryKey: ['amenities', consorcioId],
    queryFn: () => listarAmenities(consorcioId),
    enabled: Boolean(consorcioId),
  })

  const amenitySeleccionado =
    amenities.data?.find(
      (amenity) => amenity.id === amenitySeleccionadoId,
    ) ?? null

  useEffect(() => {
    if (
      amenities.data?.length &&
      !amenitySeleccionadoId
    ) {
      setAmenitySeleccionadoId(amenities.data[0].id)
    }
  }, [amenities.data, amenitySeleccionadoId])

  useEffect(() => {
    setAmenitySeleccionadoId(null)
    setAmenityEditandoId(null)
    setTurnoEditandoId(null)
    setFormAmenity(AMENITY_INICIAL)
    setFormTurno(TURNO_INICIAL)
    setMensaje('')
    setError('')
  }, [consorcioId])

  const turnos = useQuery({
    queryKey: [
      'turnos-amenity',
      consorcioId,
      amenitySeleccionadoId,
    ],
    queryFn: () =>
      listarTurnosAmenity(
        consorcioId,
        amenitySeleccionadoId!,
      ),
    enabled: Boolean(
      consorcioId && amenitySeleccionadoId,
    ),
  })

  const refrescarAmenities = async () => {
    await queryClient.invalidateQueries({
      queryKey: ['amenities', consorcioId],
    })
  }

  const refrescarTurnos = async () => {
    await queryClient.invalidateQueries({
      queryKey: [
        'turnos-amenity',
        consorcioId,
        amenitySeleccionadoId,
      ],
    })
  }

  const mutacionAmenity = useMutation({
    mutationFn: async () => {
      if (amenityEditandoId) {
        return actualizarAmenity(
          consorcioId,
          amenityEditandoId,
          formAmenity,
        )
      }

      return crearAmenity(consorcioId, formAmenity)
    },
    onSuccess: async (amenity) => {
      await refrescarAmenities()

      setAmenitySeleccionadoId(amenity.id)
      setAmenityEditandoId(null)
      setFormAmenity(AMENITY_INICIAL)
      setError('')
      setMensaje(
        amenityEditandoId
          ? 'Amenity actualizado correctamente.'
          : 'Amenity creado correctamente.',
      )
    },
    onError: (errorDesconocido) => {
      setMensaje('')
      setError(obtenerMensaje(errorDesconocido))
    },
  })

  const mutacionEstadoAmenity = useMutation({
    mutationFn: ({
      amenityId,
      activo,
    }: {
      amenityId: string
      activo: boolean
    }) =>
      cambiarEstadoAmenity(
        consorcioId,
        amenityId,
        activo,
      ),
    onSuccess: async () => {
      await refrescarAmenities()
      setError('')
      setMensaje('Estado del amenity actualizado.')
    },
    onError: (errorDesconocido) => {
      setMensaje('')
      setError(obtenerMensaje(errorDesconocido))
    },
  })

  const mutacionTurno = useMutation({
    mutationFn: async () => {
      if (!amenitySeleccionadoId) {
        throw new Error('Seleccioná un amenity.')
      }

      if (turnoEditandoId) {
        return actualizarTurnoAmenity(
          consorcioId,
          amenitySeleccionadoId,
          turnoEditandoId,
          formTurno,
        )
      }

      return crearTurnoAmenity(
        consorcioId,
        amenitySeleccionadoId,
        formTurno,
      )
    },
    onSuccess: async () => {
      await refrescarTurnos()

      setTurnoEditandoId(null)
      setFormTurno(TURNO_INICIAL)
      setError('')
      setMensaje(
        turnoEditandoId
          ? 'Turno actualizado correctamente.'
          : 'Turno creado correctamente.',
      )
    },
    onError: (errorDesconocido) => {
      setMensaje('')
      setError(obtenerMensaje(errorDesconocido))
    },
  })

  const mutacionEstadoTurno = useMutation({
    mutationFn: ({
      turnoId,
      activo,
    }: {
      turnoId: string
      activo: boolean
    }) => {
      if (!amenitySeleccionadoId) {
        throw new Error('Seleccioná un amenity.')
      }

      return cambiarEstadoTurnoAmenity(
        consorcioId,
        amenitySeleccionadoId,
        turnoId,
        activo,
      )
    },
    onSuccess: async () => {
      await refrescarTurnos()
      setError('')
      setMensaje('Estado del turno actualizado.')
    },
    onError: (errorDesconocido) => {
      setMensaje('')
      setError(obtenerMensaje(errorDesconocido))
    },
  })

  const guardarAmenity = (event: FormEvent) => {
    event.preventDefault()
    setMensaje('')
    setError('')
    mutacionAmenity.mutate()
  }

  const guardarTurno = (event: FormEvent) => {
    event.preventDefault()
    setMensaje('')
    setError('')
    mutacionTurno.mutate()
  }

  const editarAmenity = (amenity: Amenity) => {
    setAmenityEditandoId(amenity.id)
    setFormAmenity({
      nombre: amenity.nombre,
      descripcion: amenity.descripcion ?? '',
      activo: amenity.activo,
    })
    setMensaje('')
    setError('')
  }

  const editarTurno = (turno: TurnoAmenity) => {
    setTurnoEditandoId(turno.id)
    setFormTurno({
      nombre: turno.nombre,
      horaInicio: turno.horaInicio,
      horaFin: turno.horaFin,
      activo: turno.activo,
    })
    setMensaje('')
    setError('')
  }

  const cancelarEdicionAmenity = () => {
    setAmenityEditandoId(null)
    setFormAmenity(AMENITY_INICIAL)
  }

  const cancelarEdicionTurno = () => {
    setTurnoEditandoId(null)
    setFormTurno(TURNO_INICIAL)
  }

  if (!activo) {
    return (
      <div className="card p-6 text-slate-600">
        Seleccioná un consorcio para administrar sus amenities.
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold text-slate-900">
          Amenities
        </h1>

        <p className="mt-1 text-sm text-slate-500">
          Configurá el SUM, la parrilla y sus turnos para{' '}
          <strong>{activo.nombre}</strong>.
        </p>
      </header>

      {mensaje && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-700">
          {mensaje}
        </div>
      )}

      {error && (
        <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <div className="grid gap-6 xl:grid-cols-[360px_1fr]">
        <section className="space-y-4">
          <FormularioAmenity
            datos={formAmenity}
            editando={Boolean(amenityEditandoId)}
            guardando={mutacionAmenity.isPending}
            onChange={setFormAmenity}
            onSubmit={guardarAmenity}
            onCancelar={cancelarEdicionAmenity}
          />

          <ListaAmenities
            cargando={amenities.isLoading}
            error={amenities.isError}
            amenities={amenities.data ?? []}
            seleccionadoId={amenitySeleccionadoId}
            cambiandoEstado={
              mutacionEstadoAmenity.isPending
            }
            onSeleccionar={setAmenitySeleccionadoId}
            onEditar={editarAmenity}
            onCambiarEstado={(amenity) =>
              mutacionEstadoAmenity.mutate({
                amenityId: amenity.id,
                activo: !amenity.activo,
              })
            }
            onReintentar={() => amenities.refetch()}
          />
        </section>

        <section className="space-y-4">
          {amenitySeleccionado ? (
            <>
              <EncabezadoAmenity
                amenity={amenitySeleccionado}
              />

              <FormularioTurno
                datos={formTurno}
                editando={Boolean(turnoEditandoId)}
                guardando={mutacionTurno.isPending}
                amenityActivo={
                  amenitySeleccionado.activo
                }
                onChange={setFormTurno}
                onSubmit={guardarTurno}
                onCancelar={cancelarEdicionTurno}
              />

              <ListaTurnos
                cargando={turnos.isLoading}
                error={turnos.isError}
                turnos={turnos.data ?? []}
                cambiandoEstado={
                  mutacionEstadoTurno.isPending
                }
                onEditar={editarTurno}
                onCambiarEstado={(turno) =>
                  mutacionEstadoTurno.mutate({
                    turnoId: turno.id,
                    activo: !turno.activo,
                  })
                }
                onReintentar={() => turnos.refetch()}
              />
            </>
          ) : (
            <div className="card p-8 text-center text-slate-500">
              Creá o seleccioná un amenity para configurar sus
              turnos.
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

interface FormularioAmenityProps {
  datos: AmenityInput
  editando: boolean
  guardando: boolean
  onChange: (datos: AmenityInput) => void
  onSubmit: (event: FormEvent) => void
  onCancelar: () => void
}

function FormularioAmenity({
  datos,
  editando,
  guardando,
  onChange,
  onSubmit,
  onCancelar,
}: FormularioAmenityProps) {
  return (
    <form className="card space-y-4 p-5" onSubmit={onSubmit}>
      <div>
        <h2 className="font-semibold text-slate-900">
          {editando ? 'Editar amenity' : 'Nuevo amenity'}
        </h2>

        <p className="mt-1 text-xs text-slate-500">
          Ejemplo: SUM + Parrilla.
        </p>
      </div>

      <div>
        <label className="label" htmlFor="amenity-nombre">
          Nombre
        </label>

        <input
          id="amenity-nombre"
          className="input"
          required
          maxLength={100}
          value={datos.nombre}
          onChange={(event) =>
            onChange({
              ...datos,
              nombre: event.target.value,
            })
          }
        />
      </div>

      <div>
        <label
          className="label"
          htmlFor="amenity-descripcion"
        >
          Descripción
        </label>

        <textarea
          id="amenity-descripcion"
          className="input min-h-24"
          maxLength={500}
          value={datos.descripcion ?? ''}
          onChange={(event) =>
            onChange({
              ...datos,
              descripcion: event.target.value,
            })
          }
        />
      </div>

      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          checked={datos.activo}
          onChange={(event) =>
            onChange({
              ...datos,
              activo: event.target.checked,
            })
          }
        />
        Amenity Activo
      </label>

      <div className="flex gap-2">
        <button
          className="btn-primary"
          disabled={guardando}
          type="submit"
        >
          {guardando
            ? 'Guardando...'
            : editando
              ? 'Guardar cambios'
              : 'Crear amenity'}
        </button>

        {editando && (
          <button
            className="btn-secondary"
            type="button"
            onClick={onCancelar}
          >
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}

interface ListaAmenitiesProps {
  cargando: boolean
  error: boolean
  amenities: Amenity[]
  seleccionadoId: string | null
  cambiandoEstado: boolean
  onSeleccionar: (id: string) => void
  onEditar: (amenity: Amenity) => void
  onCambiarEstado: (amenity: Amenity) => void
  onReintentar: () => void
}

function ListaAmenities({
  cargando,
  error,
  amenities,
  seleccionadoId,
  cambiandoEstado,
  onSeleccionar,
  onEditar,
  onCambiarEstado,
  onReintentar,
}: ListaAmenitiesProps) {
  if (cargando) {
    return <Cargando />
  }

  if (error) {
    return <ErrorCarga onReintentar={onReintentar} />
  }

  return (
    <div className="card overflow-hidden">
      <div className="border-b border-slate-100 p-4">
        <h2 className="font-semibold">Amenities configurados</h2>
      </div>

      <div className="divide-y divide-slate-100">
        {amenities.map((amenity) => (
          <article
            key={amenity.id}
            className={`space-y-3 p-4 ${
              seleccionadoId === amenity.id
                ? 'bg-blue-50'
                : ''
            }`}
          >
            <button
              className="w-full text-left"
              type="button"
              onClick={() => onSeleccionar(amenity.id)}
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="font-semibold text-slate-900">
                    {amenity.nombre}
                  </p>

                  <p className="mt-1 text-sm text-slate-500">
                    {amenity.descripcion || 'Sin descripción'}
                  </p>
                </div>

                <EstadoActivo activo={amenity.activo} />
              </div>
            </button>

            <div className="flex gap-2">
              <button
                className="btn-secondary"
                type="button"
                onClick={() => onEditar(amenity)}
              >
                Editar
              </button>

              <button
                className="btn-secondary"
                disabled={cambiandoEstado}
                type="button"
                onClick={() => onCambiarEstado(amenity)}
              >
                {amenity.activo ? 'Desactivar' : 'Activar'}
              </button>
            </div>
          </article>
        ))}

        {amenities.length === 0 && (
          <p className="p-5 text-sm text-slate-500">
            Este consorcio todavía no tiene amenities.
          </p>
        )}
      </div>
    </div>
  )
}

function EncabezadoAmenity({
  amenity,
}: {
  amenity: Amenity
}) {
  return (
    <div className="card flex items-start justify-between gap-4 p-5">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
          Amenity seleccionado
        </p>

        <h2 className="mt-1 text-xl font-bold">
          {amenity.nombre}
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          {amenity.descripcion || 'Sin descripción'}
        </p>
      </div>

      <EstadoActivo activo={amenity.activo} />
    </div>
  )
}

interface FormularioTurnoProps {
  datos: TurnoAmenityInput
  editando: boolean
  guardando: boolean
  amenityActivo: boolean
  onChange: (datos: TurnoAmenityInput) => void
  onSubmit: (event: FormEvent) => void
  onCancelar: () => void
}

function FormularioTurno({
  datos,
  editando,
  guardando,
  amenityActivo,
  onChange,
  onSubmit,
  onCancelar,
}: FormularioTurnoProps) {
  return (
    <form className="card space-y-4 p-5" onSubmit={onSubmit}>
      <div>
        <h2 className="font-semibold">
          {editando ? 'Editar turno' : 'Nuevo turno'}
        </h2>

        {!amenityActivo && (
          <p className="mt-1 text-sm text-amber-700">
            Activá el amenity para administrar sus turnos.
          </p>
        )}
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <div>
          <label className="label" htmlFor="turno-nombre">
            Nombre
          </label>

          <input
            id="turno-nombre"
            className="input"
            required
            maxLength={60}
            value={datos.nombre}
            onChange={(event) =>
              onChange({
                ...datos,
                nombre: event.target.value,
              })
            }
          />
        </div>

        <div>
          <label className="label" htmlFor="turno-inicio">
            Hora de inicio
          </label>

          <input
            id="turno-inicio"
            className="input"
            required
            type="time"
            value={datos.horaInicio}
            onChange={(event) =>
              onChange({
                ...datos,
                horaInicio: event.target.value,
              })
            }
          />
        </div>

        <div>
          <label className="label" htmlFor="turno-fin">
            Hora de finalización
          </label>

          <input
            id="turno-fin"
            className="input"
            required
            type="time"
            value={datos.horaFin}
            onChange={(event) =>
              onChange({
                ...datos,
                horaFin: event.target.value,
              })
            }
          />
        </div>
      </div>

      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          checked={datos.activo}
          onChange={(event) =>
            onChange({
              ...datos,
              activo: event.target.checked,
            })
          }
        />
        Turno activo
      </label>

      <div className="flex gap-2">
        <button
          className="btn-primary"
          disabled={!amenityActivo || guardando}
          type="submit"
        >
          {guardando
            ? 'Guardando...'
            : editando
              ? 'Guardar turno'
              : 'Crear turno'}
        </button>

        {editando && (
          <button
            className="btn-secondary"
            type="button"
            onClick={onCancelar}
          >
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}

interface ListaTurnosProps {
  cargando: boolean
  error: boolean
  turnos: TurnoAmenity[]
  cambiandoEstado: boolean
  onEditar: (turno: TurnoAmenity) => void
  onCambiarEstado: (turno: TurnoAmenity) => void
  onReintentar: () => void
}

function ListaTurnos({
  cargando,
  error,
  turnos,
  cambiandoEstado,
  onEditar,
  onCambiarEstado,
  onReintentar,
}: ListaTurnosProps) {
  if (cargando) {
    return <Cargando />
  }

  if (error) {
    return <ErrorCarga onReintentar={onReintentar} />
  }

  return (
    <div className="card overflow-x-auto">
      <table className="w-full min-w-[650px]">
        <thead className="bg-brand">
          <tr>
            <th className="th rounded-tl-2xl">Turno</th>
            <th className="th">Horario</th>
            <th className="th">Estado</th>
            <th className="th rounded-tr-2xl text-right">
              Acciones
            </th>
          </tr>
        </thead>

        <tbody className="divide-y divide-slate-100">
          {turnos.map((turno) => (
            <tr key={turno.id}>
              <td className="td font-medium">
                {turno.nombre}
              </td>

              <td className="td">
                {turno.horaInicio} a {turno.horaFin}
              </td>

              <td className="td">
                <EstadoActivo activo={turno.activo} />
              </td>

              <td className="td">
                <div className="flex justify-end gap-2">
                  <button
                    className="btn-secondary"
                    type="button"
                    onClick={() => onEditar(turno)}
                  >
                    Editar
                  </button>

                  <button
                    className="btn-secondary"
                    disabled={cambiandoEstado}
                    type="button"
                    onClick={() =>
                      onCambiarEstado(turno)
                    }
                  >
                    {turno.activo
                      ? 'Desactivar'
                      : 'Activar'}
                  </button>
                </div>
              </td>
            </tr>
          ))}

          {turnos.length === 0 && (
            <tr>
              <td
                className="td py-8 text-center text-slate-500"
                colSpan={4}
              >
                Este amenity todavía no tiene turnos.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}

function EstadoActivo({ activo }: { activo: boolean }) {
  return (
    <span
      className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
        activo
          ? 'bg-emerald-100 text-emerald-700'
          : 'bg-slate-200 text-slate-600'
      }`}
    >
      {activo ? 'Activo' : 'Inactivo'}
    </span>
  )
}

function obtenerMensaje(error: unknown) {
  if (error instanceof ApiError) {
    return error.message
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'No se pudo completar la operación.'
}