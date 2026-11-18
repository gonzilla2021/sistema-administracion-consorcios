import { Cargando, ErrorCarga } from '../Estados'
import type { Amenity } from '../../types'

interface AmenityListProps {
  amenities: Amenity[]
  seleccionadoId: string | null
  cargando: boolean
  error: boolean
  cambiandoEstado: boolean
  onSeleccionar: (id: string) => void
  onEditar: (amenity: Amenity) => void
  onCambiarEstado: (amenity: Amenity) => void
  onReintentar: () => void
}

export default function AmenityList({
  amenities,
  seleccionadoId,
  cargando,
  error,
  cambiandoEstado,
  onSeleccionar,
  onEditar,
  onCambiarEstado,
  onReintentar,
}: AmenityListProps) {
  if (cargando) {
    return <Cargando />
  }

  if (error) {
    return <ErrorCarga onReintentar={onReintentar} />
  }

  return (
    <div className="card overflow-hidden">
      <div className="border-b border-slate-100 p-4">
        <h2 className="font-semibold text-slate-900">
          Amenities configurados
        </h2>
      </div>

      <div className="divide-y divide-slate-100">
        {amenities.map((amenity) => {
          const seleccionado =
            amenity.id === seleccionadoId

          return (
            <article
              key={amenity.id}
              className={`space-y-3 p-4 ${
                seleccionado ? 'bg-blue-50' : ''
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
                      {amenity.descripcion ||
                        'Sin descripción'}
                    </p>
                  </div>

                  <EstadoActivo activo={amenity.activo} />
                </div>
              </button>

              <div className="flex flex-wrap gap-2">
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
                  onClick={() =>
                    onCambiarEstado(amenity)
                  }
                >
                  {amenity.activo
                    ? 'Desactivar'
                    : 'Activar'}
                </button>
              </div>
            </article>
          )
        })}

        {amenities.length === 0 && (
          <p className="p-5 text-sm text-slate-500">
            Este consorcio todavía no tiene amenities
            configurados.
          </p>
        )}
      </div>
    </div>
  )
}

function EstadoActivo({ activo }: { activo: boolean }) {
  return (
    <span
      className={`inline-flex shrink-0 rounded-full px-2 py-1 text-xs font-semibold ${
        activo
          ? 'bg-emerald-100 text-emerald-700'
          : 'bg-slate-200 text-slate-600'
      }`}
    >
      {activo ? 'Activo' : 'Inactivo'}
    </span>
  )
}