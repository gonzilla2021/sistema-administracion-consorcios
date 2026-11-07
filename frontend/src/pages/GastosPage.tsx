import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Cargando, ErrorCarga, MensajeError } from '../components/Estados'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import Modal from '../components/Modal'
import { actualizarGasto, crearGasto, eliminarGasto, listarGastos } from '../services/gastos'
import { exportarCsv } from '../services/csv'
import { exportarExcel } from '../services/excel'
import { moneda, type Gasto, type TipoGasto } from '../types'

interface Form {
  descripcion: string
  monto: string
  tipo: TipoGasto
  fecha: string
  comprobante: string
}

const hoy = () => new Date().toISOString().slice(0, 10)
const vacio = (): Form => ({ descripcion: '', monto: '', tipo: 'ORDINARIO', fecha: hoy(), comprobante: '' })
const aNumero = (t: string) => Number(t.replace(',', '.'))

export default function GastosPage() {
  const { activo, cargando } = useConsorcioActivo()
  const qc = useQueryClient()
  const consorcioId = activo?.id
  const [editando, setEditando] = useState<Gasto | 'nuevo' | null>(null)
  const [form, setForm] = useState<Form>(vacio())
  const [tipo, setTipo] = useState<'' | TipoGasto>('')
  const [mes, setMes] = useState('')

  const gastos = useQuery({
    queryKey: ['gastos', consorcioId],
    queryFn: () => listarGastos(consorcioId!),
    enabled: !!consorcioId,
  })

  const cerrar = () => setEditando(null)
  const refrescar = () => qc.invalidateQueries({ queryKey: ['gastos', consorcioId] })

  const guardar = useMutation({
    mutationFn: () => {
      const datos = {
        descripcion: form.descripcion.trim(),
        monto: aNumero(form.monto),
        tipo: form.tipo,
        fecha: form.fecha,
        comprobante: form.comprobante.trim() || null,
      }
      return editando === 'nuevo'
        ? crearGasto(consorcioId!, datos)
        : actualizarGasto(consorcioId!, (editando as Gasto).id, datos)
    },
    onSuccess: () => {
      refrescar()
      cerrar()
    },
  })

  const borrar = useMutation({
    mutationFn: (id: string) => eliminarGasto(consorcioId!, id),
    onSuccess: refrescar,
  })

  const abrir = (g: Gasto | 'nuevo') => {
    guardar.reset()
    setForm(
      g === 'nuevo'
        ? vacio()
        : { descripcion: g.descripcion, monto: String(g.monto), tipo: g.tipo, fecha: g.fecha, comprobante: g.comprobante ?? '' },
    )
    setEditando(g)
  }

  const visibles = (gastos.data ?? [])
    .filter((g) => (tipo === '' || g.tipo === tipo) && (mes === '' || g.fecha.startsWith(mes)))
    .sort((a, b) => b.fecha.localeCompare(a.fecha))
  const total = visibles.reduce((s, g) => s + g.monto, 0)
  const valido = form.descripcion.trim() !== '' && aNumero(form.monto) > 0 && form.fecha !== ''

  if (cargando) return <Cargando />
  if (!activo) return <p className="text-sm text-slate-500">No hay un consorcio para mostrar.</p>

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Gastos · {activo.nombre}</h1>
        <button className="btn-primary" onClick={() => abrir('nuevo')}>+ Nuevo gasto</button>
      </div>

      <div className="card grid gap-4 p-4 sm:grid-cols-2">
        <div>
          <label htmlFor="f-tipo" className="label">Tipo</label>
          <select id="f-tipo" className="input" value={tipo} onChange={(e) => setTipo(e.target.value as '' | TipoGasto)}>
            <option value="">Todos</option>
            <option value="ORDINARIO">Ordinario</option>
            <option value="EXTRAORDINARIO">Extraordinario</option>
          </select>
        </div>
        <div>
          <label htmlFor="f-mes" className="label">Mes</label>
          <input id="f-mes" type="month" className="input" value={mes} onChange={(e) => setMes(e.target.value)} />
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-slate-500">
          {visibles.length} gastos · Total <span className="font-semibold text-slate-900">{moneda(total)}</span>
        </p>
        <div className="flex gap-2">
          <button
            className="btn-secondary"
            onClick={() =>
              exportarExcel(
                'gastos.xlsx',
                'Gastos',
                [
                  { titulo: 'Fecha', ancho: 12 },
                  { titulo: 'Descripción', ancho: 44 },
                  { titulo: 'Tipo', ancho: 16 },
                  { titulo: 'Monto', ancho: 16, formato: 'moneda' },
                  { titulo: 'Comprobante', ancho: 24 },
                ],
                visibles.map((g) => [g.fecha, g.descripcion, g.tipo === 'ORDINARIO' ? 'Ordinario' : 'Extraordinario', g.monto, g.comprobante ?? '']),
              )
            }
          >
            Exportar Excel
          </button>
          <button
            className="btn-secondary"
            onClick={() =>
              exportarCsv(
                'gastos.csv',
                ['Fecha', 'Descripción', 'Tipo', 'Monto', 'Comprobante'],
                visibles.map((g) => [g.fecha, g.descripcion, g.tipo, String(g.monto), g.comprobante ?? '']),
              )
            }
          >
            Exportar CSV
          </button>
        </div>
      </div>

      {gastos.isLoading && <Cargando />}
      {gastos.isError && <ErrorCarga onReintentar={() => gastos.refetch()} />}
      {borrar.isError && <MensajeError>{borrar.error.message}</MensajeError>}

      {gastos.data && (
        <div className="card overflow-x-auto">
          <table className="w-full">
            <thead className="bg-brand">
              <tr>
                <th className="th rounded-tl-2xl">Fecha</th>
                <th className="th">Descripción</th>
                <th className="th">Tipo</th>
                <th className="th text-right">Monto</th>
                <th className="th rounded-tr-2xl text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {visibles.map((g) => (
                <tr key={g.id}>
                  <td className="td">{g.fecha}</td>
                  <td className="td">
                    {g.descripcion}
                    {g.comprobante && <span className="block text-xs text-slate-500">Comp.: {g.comprobante}</span>}
                  </td>
                  <td className="td">{g.tipo === 'ORDINARIO' ? 'Ordinario' : 'Extraordinario'}</td>
                  <td className="td text-right">{moneda(g.monto)}</td>
                  <td className="td space-x-3 text-right">
                    <button className="link-accion" onClick={() => abrir(g)}>Editar</button>
                    <button
                      className="link-accion text-red-600"
                      disabled={borrar.isPending}
                      onClick={() => confirm(`¿Eliminar "${g.descripcion}"?`) && borrar.mutate(g.id)}
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
              {visibles.length === 0 && (
                <tr>
                  <td className="td text-slate-500" colSpan={5}>No hay gastos para mostrar.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {editando && (
        <Modal titulo={editando === 'nuevo' ? 'Nuevo gasto' : 'Editar gasto'} onClose={cerrar}>
          <form
            className="space-y-4"
            onSubmit={(e) => {
              e.preventDefault()
              guardar.mutate()
            }}
          >
            <div>
              <label htmlFor="g-desc" className="label">Descripción</label>
              <input id="g-desc" maxLength={300} className="input" value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="g-monto" className="label">Monto</label>
                <input id="g-monto" inputMode="decimal" className="input" value={form.monto} onChange={(e) => setForm({ ...form, monto: e.target.value })} />
              </div>
              <div>
                <label htmlFor="g-fecha" className="label">Fecha</label>
                <input id="g-fecha" type="date" className="input" value={form.fecha} onChange={(e) => setForm({ ...form, fecha: e.target.value })} />
              </div>
            </div>
            <div>
              <label htmlFor="g-tipo" className="label">Tipo</label>
              <select id="g-tipo" className="input" value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value as TipoGasto })}>
                <option value="ORDINARIO">Ordinario</option>
                <option value="EXTRAORDINARIO">Extraordinario</option>
              </select>
            </div>
            <div>
              <label htmlFor="g-comp" className="label">Comprobante (opcional)</label>
              <input id="g-comp" maxLength={500} className="input" value={form.comprobante} onChange={(e) => setForm({ ...form, comprobante: e.target.value })} />
            </div>
            {guardar.isError && <MensajeError>{guardar.error.message}</MensajeError>}
            <div className="flex justify-end gap-2">
              <button type="button" className="btn-secondary" onClick={cerrar}>Cancelar</button>
              <button type="submit" className="btn-primary" disabled={!valido || guardar.isPending}>Guardar</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
