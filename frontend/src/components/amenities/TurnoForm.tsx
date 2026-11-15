import type { FormEvent } from 'react'
import type { TurnoAmenityInput } from '../../types'

interface TurnoFormProps {
  datos: TurnoAmenityInput
  editando: boolean
  guardando: boolean
  amenityActivo: boolean
  onChange: (datos: TurnoAmenityInput) => void
  onGuardar: () => void
  onCancelar: () => void
}

export default function TurnoForm({
  datos,
  editando,
  guardando,
  amenityActivo,
  onChange,
  onGuardar,
  onCancelar,
}: TurnoFormProps) {
  const enviar = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onGuardar()
  }

  return (
    <form className="card space-y-4 p-5" onSubmit={enviar}>
      <div>
        <h2 className="font-semibold text-slate-900">
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
            disabled={!amenityActivo || guardando}
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
            disabled={!amenityActivo || guardando}
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
            disabled={!amenityActivo || guardando}
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

      <label className="flex items-center gap-2 text-sm text-slate-700">
        <input
          type="checkbox"
          disabled={!amenityActivo || guardando}
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

      <div className="flex flex-wrap gap-2">
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