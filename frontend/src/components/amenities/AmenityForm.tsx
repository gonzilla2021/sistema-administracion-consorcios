import type { FormEvent } from 'react'
import type { AmenityInput } from '../../types'

interface AmenityFormProps {
  datos: AmenityInput
  editando: boolean
  guardando: boolean
  onChange: (datos: AmenityInput) => void
  onGuardar: () => void
  onCancelar: () => void
}

export default function AmenityForm({
  datos,
  editando,
  guardando,
  onChange,
  onGuardar,
  onCancelar,
}: AmenityFormProps) {
  const enviar = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onGuardar()
  }

  return (
    <form className="card space-y-4 p-5" onSubmit={enviar}>
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
          className="input min-h-24 resize-y"
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

      <label className="flex items-center gap-2 text-sm text-slate-700">
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

        Amenity activo
      </label>

      <div className="flex flex-wrap gap-2">
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
            disabled={guardando}
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