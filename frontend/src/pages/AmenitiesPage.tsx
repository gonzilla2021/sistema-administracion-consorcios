import { useEffect, useMemo, useState } from 'react'
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'

import AmenityForm from '../components/amenities/AmenityForm'
import AmenityList from '../components/amenities/AmenityList'
import ReservasSection from '../components/amenities/ReservasSection'
import TurnoForm from '../components/amenities/TurnoForm'
import TurnoList from '../components/amenities/TurnoList'
import BloqueosSection from '../components/amenities/BloqueosSection'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import { ApiError } from '../services/api'
import {
  actualizarAmenity,
  actualizarTurnoAmenity,
  cambiarEstadoAmenity,
  cambiarEstadoTurnoAmenity,
  crearAmenity,
  crearTurnoAmenity,
  listarAmenities,
  listarReservasAmenity,
  listarTurnosAmenity,
  crearBloqueoAmenity,
  desbloquearAmenity,
  listarBloqueosAmenity
} from '../services/amenities'
import type {
  Amenity,
  AmenityInput,
  BloqueoAmenity,
  BloqueoAmenityInput,
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
  horaInicio: '11:00',
  horaFin: '16:00',
  activo: true,
}

const fechaLocal = (fecha: Date) => {
  const anio = fecha.getFullYear()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')

  return `${anio}-${mes}-${dia}`
}

const hoy = () => fechaLocal(new Date())

const dentroDeTreintaDias = () => {
  const fecha = new Date()
  fecha.setDate(fecha.getDate() + 30)
  return fechaLocal(fecha)
}

export default function AmenitiesPage() {
  const { activo } = useConsorcioActivo()
  const queryClient = useQueryClient()
  const consorcioId = activo?.id ?? ''

  const [amenityId, setAmenityId] =
    useState<string | null>(null)

  const [amenityEditandoId, setAmenityEditandoId] =
    useState<string | null>(null)

  const [turnoEditandoId, setTurnoEditandoId] =
    useState<string | null>(null)

  const [amenityForm, setAmenityForm] =
    useState(AMENITY_INICIAL)

  const [turnoForm, setTurnoForm] =
    useState(TURNO_INICIAL)

  const [desde, setDesde] = useState(hoy)
  const [hasta, setHasta] =
    useState(dentroDeTreintaDias)

  const [mensaje, setMensaje] = useState('')
  const [error, setError] = useState('')

  const amenitiesQuery = useQuery({
    queryKey: ['amenities', consorcioId],
    queryFn: () => listarAmenities(consorcioId),
    enabled: Boolean(consorcioId),
  })

  const amenitySeleccionado =
    amenitiesQuery.data?.find(
      (amenity) => amenity.id === amenityId,
    ) ?? null

  const turnosQuery = useQuery({
    queryKey: ['turnos-amenity', consorcioId, amenityId],
    queryFn: () =>
      listarTurnosAmenity(consorcioId, amenityId!),
    enabled: Boolean(consorcioId && amenityId),
  })

  const bloqueosQuery = useQuery({
    queryKey: [
        'bloqueos-amenity',
        consorcioId,
        amenityId,
    ],
    queryFn: () =>
        listarBloqueosAmenity(
        consorcioId,
        amenityId!,
        ),
    enabled: Boolean(consorcioId && amenityId),
    })

  const errorRango = useMemo(
    () => validarRango(desde, hasta),
    [desde, hasta],
  )

  const reservasQuery = useQuery({
    queryKey: [
      'reservas-amenity',
      consorcioId,
      amenityId,
      desde,
      hasta,
    ],
    queryFn: () =>
      listarReservasAmenity(
        consorcioId,
        amenityId!,
        desde,
        hasta,
      ),
    enabled: Boolean(
      consorcioId &&
        amenityId &&
        desde &&
        hasta &&
        !errorRango,
    ),
  })

  useEffect(() => {
    const primerAmenity = amenitiesQuery.data?.[0]

    if (primerAmenity && !amenityId) {
      setAmenityId(primerAmenity.id)
    }
  }, [amenitiesQuery.data, amenityId])

  useEffect(() => {
    setAmenityId(null)
    setAmenityEditandoId(null)
    setTurnoEditandoId(null)
    setAmenityForm(AMENITY_INICIAL)
    setTurnoForm(TURNO_INICIAL)
    setDesde(hoy())
    setHasta(dentroDeTreintaDias())
    setMensaje('')
    setError('')
  }, [consorcioId])

  const guardarAmenity = useMutation({
    mutationFn: () =>
      amenityEditandoId
        ? actualizarAmenity(
            consorcioId,
            amenityEditandoId,
            amenityForm,
          )
        : crearAmenity(consorcioId, amenityForm),

    onSuccess: async (amenity) => {
      await invalidar(['amenities', consorcioId])

      setAmenityId(amenity.id)
      setAmenityEditandoId(null)
      setAmenityForm(AMENITY_INICIAL)
      exito('Amenity guardado correctamente.')
    },

    onError: manejarError,
  })

  const cambiarAmenity = useMutation({
    mutationFn: ({
      id,
      activo: nuevoEstado,
    }: {
      id: string
      activo: boolean
    }) =>
      cambiarEstadoAmenity(
        consorcioId,
        id,
        nuevoEstado,
      ),

    onSuccess: async () => {
      await invalidar(['amenities', consorcioId])
      exito('Estado del amenity actualizado.')
    },

    onError: manejarError,
  })

  const guardarTurno = useMutation({
    mutationFn: () => {
      if (!amenityId) {
        throw new Error('Seleccioná un amenity.')
      }

      return turnoEditandoId
        ? actualizarTurnoAmenity(
            consorcioId,
            amenityId,
            turnoEditandoId,
            turnoForm,
          )
        : crearTurnoAmenity(
            consorcioId,
            amenityId,
            turnoForm,
          )
    },

    onSuccess: async () => {
      await invalidar([
        'turnos-amenity',
        consorcioId,
        amenityId,
      ])

      setTurnoEditandoId(null)
      setTurnoForm(TURNO_INICIAL)
      exito('Turno guardado correctamente.')
    },

    onError: manejarError,
  })

  const cambiarTurno = useMutation({
    mutationFn: ({
      id,
      activo: nuevoEstado,
    }: {
      id: string
      activo: boolean
    }) => {
      if (!amenityId) {
        throw new Error('Seleccioná un amenity.')
      }

      return cambiarEstadoTurnoAmenity(
        consorcioId,
        amenityId,
        id,
        nuevoEstado,
      )
    },

    onSuccess: async () => {
      await invalidar([
        'turnos-amenity',
        consorcioId,
        amenityId,
      ])

      exito('Estado del turno actualizado.')
    },

    onError: manejarError,
  })

  const crearBloqueo = useMutation({
    mutationFn: (datos: BloqueoAmenityInput) => {
        if (!amenityId) {
        throw new Error('Seleccioná un amenity.')
        }

        return crearBloqueoAmenity(
        consorcioId,
        amenityId,
        datos,
        )
    },

    onSuccess: async () => {
        await queryClient.invalidateQueries({
        queryKey: [
            'bloqueos-amenity',
            consorcioId,
            amenityId,
        ],
        })

        await queryClient.invalidateQueries({
        queryKey: [
            'reservas-amenity',
            consorcioId,
            amenityId,
        ],
        })

        exito('Turno bloqueado correctamente.')
    },

    onError: manejarError,
    })

    const desbloquear = useMutation({
        mutationFn: (bloqueo: BloqueoAmenity) => {
            if (!amenityId) {
            throw new Error('Seleccioná un amenity.')
            }

            return desbloquearAmenity(
            consorcioId,
            amenityId,
            bloqueo.id,
            )
        },

        onSuccess: async () => {
            await queryClient.invalidateQueries({
            queryKey: [
                'bloqueos-amenity',
                consorcioId,
                amenityId,
            ],
            })

            exito('Turno desbloqueado correctamente.')
        },

        onError: manejarError,
        })

  function invalidar(queryKey: unknown[]) {
    return queryClient.invalidateQueries({ queryKey })
  }

  function exito(texto: string) {
    setError('')
    setMensaje(texto)
  }

  function manejarError(errorDesconocido: unknown) {
    setMensaje('')
    setError(obtenerMensaje(errorDesconocido))
  }

  function editarAmenity(amenity: Amenity) {
    setAmenityId(amenity.id)
    setAmenityEditandoId(amenity.id)
    setAmenityForm({
      nombre: amenity.nombre,
      descripcion: amenity.descripcion ?? '',
      activo: amenity.activo,
    })
  }

  function editarTurno(turno: TurnoAmenity) {
    setTurnoEditandoId(turno.id)
    setTurnoForm({
      nombre: turno.nombre,
      horaInicio: turno.horaInicio,
      horaFin: turno.horaFin,
      activo: turno.activo,
    })
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
        <h1 className="text-2xl font-bold">Amenities</h1>

        <p className="mt-1 text-sm text-slate-500">
          Configurá amenities y turnos para{' '}
          <strong>{activo.nombre}</strong>.
        </p>
      </header>

      {mensaje && (
        <Aviso tipo="exito">{mensaje}</Aviso>
      )}

      {error && <Aviso tipo="error">{error}</Aviso>}

      <div className="grid gap-6 xl:grid-cols-[360px_1fr]">
        <section className="space-y-4">
          <AmenityForm
            datos={amenityForm}
            editando={Boolean(amenityEditandoId)}
            guardando={guardarAmenity.isPending}
            onChange={setAmenityForm}
            onGuardar={() => guardarAmenity.mutate()}
            onCancelar={() => {
              setAmenityEditandoId(null)
              setAmenityForm(AMENITY_INICIAL)
            }}
          />

          <AmenityList
            amenities={amenitiesQuery.data ?? []}
            seleccionadoId={amenityId}
            cargando={amenitiesQuery.isLoading}
            error={amenitiesQuery.isError}
            cambiandoEstado={cambiarAmenity.isPending}
            onSeleccionar={setAmenityId}
            onEditar={editarAmenity}
            onCambiarEstado={(amenity) =>
              cambiarAmenity.mutate({
                id: amenity.id,
                activo: !amenity.activo,
              })
            }
            onReintentar={() => {
              void amenitiesQuery.refetch()
            }}
          />
        </section>

        <section className="space-y-4">
          {amenitySeleccionado ? (
            <>
              <div className="card p-5">
                <h2 className="text-xl font-bold">
                  {amenitySeleccionado.nombre}
                </h2>

                <p className="mt-1 text-sm text-slate-500">
                  {amenitySeleccionado.descripcion ??
                    'Sin descripción'}
                </p>
              </div>

              <TurnoForm
                datos={turnoForm}
                editando={Boolean(turnoEditandoId)}
                guardando={guardarTurno.isPending}
                amenityActivo={amenitySeleccionado.activo}
                onChange={setTurnoForm}
                onGuardar={() => guardarTurno.mutate()}
                onCancelar={() => {
                  setTurnoEditandoId(null)
                  setTurnoForm(TURNO_INICIAL)
                }}
              />

              <TurnoList
                turnos={turnosQuery.data ?? []}
                cargando={turnosQuery.isLoading}
                error={turnosQuery.isError}
                cambiandoEstado={cambiarTurno.isPending}
                onEditar={editarTurno}
                onCambiarEstado={(turno) =>
                  cambiarTurno.mutate({
                    id: turno.id,
                    activo: !turno.activo,
                  })
                }
                onReintentar={() => {
                  void turnosQuery.refetch()
                }}
              />

              <ReservasSection
                desde={desde}
                hasta={hasta}
                errorRango={errorRango}
                reservas={reservasQuery.data ?? []}
                cargando={reservasQuery.isLoading}
                error={reservasQuery.isError}
                onCambiarDesde={setDesde}
                onCambiarHasta={setHasta}
                onReintentar={() => {
                  void reservasQuery.refetch()
                }}
              />
              <BloqueosSection
                turnos={turnosQuery.data ?? []}
                bloqueos={bloqueosQuery.data ?? []}
                cargando={bloqueosQuery.isLoading}
                error={bloqueosQuery.isError}
                guardando={crearBloqueo.isPending}
                desbloqueando={desbloquear.isPending}
                amenityActivo={amenitySeleccionado.activo}
                onCrear={(datos) => crearBloqueo.mutate(datos)}
                onDesbloquear={(bloqueo) =>
                    desbloquear.mutate(bloqueo)
                }
                onReintentar={() => {
                    void bloqueosQuery.refetch()
                }}
                />
            </>
          ) : (
            <div className="card p-8 text-center text-slate-500">
              Seleccioná o creá un amenity.
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

function Aviso({
  tipo,
  children,
}: {
  tipo: 'exito' | 'error'
  children: string
}) {
  const estilo =
    tipo === 'exito'
      ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
      : 'border-red-200 bg-red-50 text-red-700'

  return (
    <div
      className={`rounded-xl border p-3 text-sm ${estilo}`}
    >
      {children}
    </div>
  )
}

function validarRango(desde: string, hasta: string) {
  if (!desde || !hasta) {
    return 'Completá ambas fechas.'
  }

  const fechaDesde = new Date(`${desde}T00:00:00`)
  const fechaHasta = new Date(`${hasta}T00:00:00`)

  if (fechaHasta < fechaDesde) {
    return 'La fecha hasta no puede ser anterior a la fecha desde.'
  }

  const limite = new Date(fechaDesde)
  limite.setDate(limite.getDate() + 30)

  return fechaHasta > limite
    ? 'El rango no puede superar los 31 días.'
    : ''
}

function obtenerMensaje(error: unknown) {
  if (error instanceof ApiError || error instanceof Error) {
    return error.message
  }

  return 'No se pudo completar la operación.'
}