import { Cargando, ErrorCarga } from '../Estados'
import type { TurnoAmenity } from '../../types'

interface TurnoListProps {
  turnos: TurnoAmenity[]
  cargando: boolean
  error: boolean
  cambiandoEstado: boolean
  onEditar: (turno: TurnoAmenity) => void
  onCambiarEstado: (turno: TurnoAmenity) => void
  onReintentar: () => void
}

export default function TurnoList({
  turnos,
  cargando,
  error,
  cambiandoEstado,
  onEditar,
  onCambiarEstado,
  onReintentar,
}: TurnoListProps) {
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