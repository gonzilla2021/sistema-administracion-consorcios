import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import Modal from '../components/Modal'
import { Cargando, ErrorCarga, MensajeError } from '../components/Estados'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import { actualizarConsorcio, crearConsorcio } from '../services/consorcios'
import { exportarCsv } from '../services/csv'
import { usuarioActual } from '../services/auth'
import type { Consorcio, ConsorcioInput } from '../types'

function FormConsorcio({ inicial, onClose }: { inicial: Consorcio | null; onClose: () => void }) {
  const qc = useQueryClient()
  const { register, handleSubmit, formState: { errors } } = useForm<ConsorcioInput>({
    defaultValues: { nombre: inicial?.nombre ?? '', direccion: inicial?.direccion ?? '' },
  })
  const guardar = useMutation({
    mutationFn: (datos: ConsorcioInput) => (inicial ? actualizarConsorcio(inicial.id, datos) : crearConsorcio(datos)),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['consorcios'] })
      onClose()
    },
  })

  return (
    <Modal titulo={inicial ? 'Editar consorcio' : 'Nuevo consorcio'} onClose={onClose}>
      <form onSubmit={handleSubmit((d) => guardar.mutate(d))} className="space-y-4" noValidate>
        <div>
          <label htmlFor="nombre" className="label">Nombre</label>
          <input id="nombre" className="input" {...register('nombre', { required: 'Ingresá el nombre', maxLength: 150 })} />
          {errors.nombre && <p className="mt-1 text-xs text-red-600">{errors.nombre.message ?? 'Máximo 150 caracteres'}</p>}
        </div>
        <div>
          <label htmlFor="direccion" className="label">Dirección</label>
          <input id="direccion" className="input" {...register('direccion', { required: 'Ingresá la dirección', maxLength: 250 })} />
          {errors.direccion && <p className="mt-1 text-xs text-red-600">{errors.direccion.message ?? 'Máximo 250 caracteres'}</p>}
        </div>
        {guardar.isError && <MensajeError>{guardar.error.message}</MensajeError>}
        <div className="flex justify-end gap-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn-primary" disabled={guardar.isPending}>Guardar</button>
        </div>
      </form>
    </Modal>
  )
}

export default function ConsorciosPage() {
  const navigate = useNavigate()
  const { consorcios, cargando, elegir } = useConsorcioActivo()
  const qc = useQueryClient()
  const esSuper = usuarioActual()?.rol === 'SUPERADMINISTRADOR'
  const [nombre, setNombre] = useState('')
  const [direccion, setDireccion] = useState('')
  const [editando, setEditando] = useState<Consorcio | 'nuevo' | null>(null)

  const visibles = consorcios.filter(
    (c) =>
      c.nombre.toLowerCase().includes(nombre.toLowerCase()) && c.direccion.toLowerCase().includes(direccion.toLowerCase()),
  )

  const verUnidades = (c: Consorcio) => {
    elegir(c.id)
    navigate('/unidades')
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Consorcios</h1>
        {esSuper && (
          <button className="btn-primary" onClick={() => setEditando('nuevo')}>
            + Nuevo consorcio
          </button>
        )}
      </div>

      <div className="card grid gap-4 p-4 sm:grid-cols-2">
        <div>
          <label htmlFor="f-nombre" className="label">Nombre</label>
          <input id="f-nombre" className="input" placeholder="Buscar por nombre" value={nombre} onChange={(e) => setNombre(e.target.value)} />
        </div>
        <div>
          <label htmlFor="f-dir" className="label">Dirección</label>
          <input id="f-dir" className="input" placeholder="Buscar por dirección" value={direccion} onChange={(e) => setDireccion(e.target.value)} />
        </div>
      </div>

      <div className="flex justify-end">
        <button
          className="btn-secondary"
          onClick={() =>
            exportarCsv('consorcios.csv', ['Nombre', 'Dirección', 'Unidades'], visibles.map((c) => [c.nombre, c.direccion, c.cantidadUnidades]))
          }
        >
          Exportar CSV
        </button>
      </div>

      {cargando && <Cargando />}
      {!cargando && consorcios.length === 0 && <ErrorCarga onReintentar={() => qc.invalidateQueries({ queryKey: ['consorcios'] })} />}

      {visibles.length > 0 && (
        <div className="card overflow-x-auto">
          <table className="w-full">
            <thead className="bg-brand">
              <tr>
                <th className="th rounded-tl-2xl">Nombre</th>
                <th className="th">Dirección</th>
                <th className="th">Unidades</th>
                <th className="th rounded-tr-2xl text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {visibles.map((c) => (
                <tr key={c.id}>
                  <td className="td font-medium">{c.nombre}</td>
                  <td className="td">{c.direccion}</td>
                  <td className="td">{c.cantidadUnidades}</td>
                  <td className="td space-x-4 text-right">
                    <button className="link-accion text-brand" onClick={() => verUnidades(c)}>Unidades</button>
                    <button className="link-accion" onClick={() => setEditando(c)}>Editar</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {editando && <FormConsorcio inicial={editando === 'nuevo' ? null : editando} onClose={() => setEditando(null)} />}
    </div>
  )
}
