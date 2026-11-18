import { Cargando, ErrorCarga } from '../Estados'
import type { Amenity } from '../../types'

interface AmenitySelectorProps {
  amenities: Amenity[]
  seleccionadoId: string | null
  cargando: boolean
  error: boolean
  onSeleccionar: (amenityId: string) => void
  onReintentar: () => void
}

export default function AmenitySelector({
  amenities,
  seleccionadoId,
  cargando,
  error,
  onSeleccionar,
  onReintentar,
}: AmenitySelectorProps) {
  if (cargando) {
    return <Cargando />
  }

  if (error) {
    return <ErrorCarga onReintentar={onReintentar} />
  }

  if (amenities.length === 0) {
    return (
      <div className="card p-6 text-center text-slate-500">
        No tenés amenities disponibles en los consorcios
        asociados a tu cuenta.
      </div>
    )
  }

  return (
    <section className="space-y-3">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">
          Amenities disponibles
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          Seleccioná un espacio para consultar sus fechas y
          turnos.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {amenities.map((amenity) => {
          const seleccionado =
            amenity.id === seleccionadoId

          return (
            <button
              key={amenity.id}
              className={`card border-2 p-5 text-left transition ${
                seleccionado
                  ? 'border-emerald-600 bg-emerald-50'
                  : 'border-transparent hover:border-slate-300'
              }`}
              type="button"
              onClick={() => onSeleccionar(amenity.id)}
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="font-semibold text-slate-900">
                    {amenity.nombre}
                  </h3>

                  <p className="mt-2 text-sm text-slate-500">
                    {amenity.descripcion || 'Sin descripción'}
                  </p>
                </div>

                {seleccionado && (
                  <span className="inline-flex shrink-0 rounded-full bg-emerald-600 px-2 py-1 text-xs font-semibold text-white">
                    Seleccionado
                  </span>
                )}
              </div>
            </button>
          )
        })}
      </div>
    </section>
  )
}