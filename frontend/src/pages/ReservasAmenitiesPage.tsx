import { useEffect, useMemo, useState } from 'react'
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'

import AmenitySelector from '../components/amenities/AmenitySelector'
import DisponibilidadSection from '../components/amenities/DisponibilidadSection'
import MisReservasSection from '../components/amenities/MisReservasSection'
import { ApiError } from '../services/api'
import {
  cancelarReservaAmenity,
  consultarDisponibilidadAmenity,
  crearReservaAmenity,
  listarMisAmenities,
  listarMisReservasAmenity,
} from '../services/amenities'
import type {
  DisponibilidadAmenity,
  ReservaAmenity,
} from '../types'

const fechaLocal = (fecha: Date) => {
  const anio = fecha.getFullYear()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')

  return `${anio}-${mes}-${dia}`
}

const hoy = () => fechaLocal(new Date())

const dentroDeSieteDias = () => {
  const fecha = new Date()
  fecha.setDate(fecha.getDate() + 7)

  return fechaLocal(fecha)
}

export default function ReservasAmenitiesPage() {
  const queryClient = useQueryClient()

  const [amenityId, setAmenityId] =
    useState<string | null>(null)

  const [desde, setDesde] = useState(hoy)
  const [hasta, setHasta] = useState(dentroDeSieteDias)

  const [mensaje, setMensaje] = useState('')
  const [error, setError] = useState('')

  const errorRango = useMemo(
    () => validarRango(desde, hasta),
    [desde, hasta],
  )

  const amenitiesQuery = useQuery({
    queryKey: ['mis-amenities'],
    queryFn: listarMisAmenities,
  })

  const disponibilidadQuery = useQuery({
    queryKey: [
      'disponibilidad-amenity',
      amenityId,
      desde,
      hasta,
    ],
    queryFn: () =>
      consultarDisponibilidadAmenity(
        amenityId!,
        desde,
        hasta,
      ),
    enabled: Boolean(
      amenityId &&
        desde &&
        hasta &&
        !errorRango,
    ),
  })

  const reservasQuery = useQuery({
    queryKey: ['mis-reservas-amenities'],
    queryFn: listarMisReservasAmenity,
  })

  useEffect(() => {
    const primerAmenity = amenitiesQuery.data?.[0]

    if (primerAmenity && !amenityId) {
      setAmenityId(primerAmenity.id)
    }
  }, [amenitiesQuery.data, amenityId])

  const refrescarDisponibilidad = () =>
    queryClient.invalidateQueries({
      queryKey: ['disponibilidad-amenity'],
    })

  const refrescarReservas = () =>
    queryClient.invalidateQueries({
      queryKey: ['mis-reservas-amenities'],
    })

  const reservar = useMutation({
    mutationFn: (
      disponibilidad: DisponibilidadAmenity,
    ) => {
      if (!amenityId) {
        throw new Error('Seleccioná un amenity.')
      }

      return crearReservaAmenity(amenityId, {
        turnoId: disponibilidad.turnoId,
        fecha: disponibilidad.fecha,
      })
    },

    onSuccess: async () => {
      await Promise.all([
        refrescarDisponibilidad(),
        refrescarReservas(),
      ])

      mostrarExito('Reserva creada correctamente.')
    },

    onError: manejarError,
  })

  const cancelar = useMutation({
    mutationFn: (reserva: ReservaAmenity) =>
      cancelarReservaAmenity(reserva.id),

    onSuccess: async () => {
      await Promise.all([
        refrescarDisponibilidad(),
        refrescarReservas(),
      ])

      mostrarExito('Reserva cancelada correctamente.')
    },

    onError: manejarError,
  })

  function seleccionarAmenity(
    nuevoAmenityId: string,
  ) {
    setAmenityId(nuevoAmenityId)
    setMensaje('')
    setError('')
  }

  function mostrarExito(texto: string) {
    setError('')
    setMensaje(texto)
  }

  function manejarError(errorDesconocido: unknown) {
    setMensaje('')
    setError(obtenerMensaje(errorDesconocido))
  }

  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-2xl font-bold text-slate-900">
          Reservas de amenities
        </h1>

        <p className="mt-1 text-sm text-slate-500">
          Consultá la disponibilidad, reservá un turno y
          administrá tus reservas.
        </p>
      </header>

      {mensaje && (
        <Aviso tipo="exito">
          {mensaje}
        </Aviso>
      )}

      {error && (
        <Aviso tipo="error">
          {error}
        </Aviso>
      )}

      <AmenitySelector
        amenities={amenitiesQuery.data ?? []}
        seleccionadoId={amenityId}
        cargando={amenitiesQuery.isLoading}
        error={amenitiesQuery.isError}
        onSeleccionar={seleccionarAmenity}
        onReintentar={() => {
          void amenitiesQuery.refetch()
        }}
      />

      <DisponibilidadSection
        desde={desde}
        hasta={hasta}
        disponibilidad={
          disponibilidadQuery.data ?? []
        }
        cargando={disponibilidadQuery.isLoading}
        error={disponibilidadQuery.isError}
        errorRango={errorRango}
        reservando={reservar.isPending}
        amenitySeleccionado={Boolean(amenityId)}
        onCambiarDesde={setDesde}
        onCambiarHasta={setHasta}
        onReservar={(disponibilidad) =>
          reservar.mutate(disponibilidad)
        }
        onReintentar={() => {
          void disponibilidadQuery.refetch()
        }}
      />

      <MisReservasSection
        reservas={reservasQuery.data ?? []}
        cargando={reservasQuery.isLoading}
        error={reservasQuery.isError}
        cancelando={cancelar.isPending}
        onCancelar={(reserva) =>
          cancelar.mutate(reserva)
        }
        onReintentar={() => {
          void reservasQuery.refetch()
        }}
      />
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

function validarRango(
  desde: string,
  hasta: string,
) {
  if (!desde || !hasta) {
    return 'Completá ambas fechas.'
  }

  const hoyLocal = new Date()
  hoyLocal.setHours(0, 0, 0, 0)

  const fechaDesde =
    new Date(`${desde}T00:00:00`)

  const fechaHasta =
    new Date(`${hasta}T00:00:00`)

  if (fechaDesde < hoyLocal) {
    return 'La fecha desde no puede ser anterior a hoy.'
  }

  if (fechaHasta < fechaDesde) {
    return 'La fecha hasta no puede ser anterior a la fecha desde.'
  }

  const limite = new Date(fechaDesde)
  limite.setDate(limite.getDate() + 30)

  if (fechaHasta > limite) {
    return 'El rango no puede superar los 31 días.'
  }

  return ''
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