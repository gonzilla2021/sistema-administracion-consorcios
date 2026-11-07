import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Cargando, ErrorCarga, MensajeError } from '../components/Estados'
import Modal from '../components/Modal'
import { actualizarPersona, crearPersona, listarPersonas } from '../services/personas'
import { exportarCsv } from '../services/csv'
import { exportarExcel } from '../services/excel'
import type { Persona } from '../types'

interface Form {
  nombre: string
  apellido: string
  dni: string
  email: string
  telefono: string
}

const vacio = (): Form => ({ nombre: '', apellido: '', dni: '', email: '', telefono: '' })
const emailOk = (e: string) => e === '' || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e)

export default function PersonasPage() {
  const qc = useQueryClient()
  const [editando, setEditando] = useState<Persona | 'nueva' | null>(null)
  const [form, setForm] = useState<Form>(vacio())
  const [buscar, setBuscar] = useState('')

  const personas = useQuery({ queryKey: ['personas'], queryFn: listarPersonas })

  const guardar = useMutation({
    mutationFn: () => {
      const datos = {
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        dni: form.dni.trim(),
        email: form.email.trim() || null,
        telefono: form.telefono.trim() || null,
      }
      return editando === 'nueva' ? crearPersona(datos) : actualizarPersona((editando as Persona).id, datos)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['personas'] })
      setEditando(null)
    },
  })

  const abrir = (p: Persona | 'nueva') => {
    guardar.reset()
    setForm(
      p === 'nueva'
        ? vacio()
        : { nombre: p.nombre, apellido: p.apellido, dni: p.dni, email: p.email ?? '', telefono: p.telefono ?? '' },
    )
    setEditando(p)
  }

  const q = buscar.trim().toLowerCase()
  const visibles = (personas.data ?? [])
    .filter((p) => q === '' || `${p.apellido} ${p.nombre} ${p.dni} ${p.email ?? ''}`.toLowerCase().includes(q))
    .sort((a, b) => a.apellido.localeCompare(b.apellido, 'es') || a.nombre.localeCompare(b.nombre, 'es'))

  const valido =
    form.nombre.trim() !== '' && form.apellido.trim() !== '' && form.dni.trim() !== '' && emailOk(form.email.trim())
  const set = (campo: keyof Form) => (e: React.ChangeEvent<HTMLInputElement>) => setForm({ ...form, [campo]: e.target.value })

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Personas</h1>
        <button className="btn-primary" onClick={() => abrir('nueva')}>+ Nueva persona</button>
      </div>

      <div className="card p-4">
        <label htmlFor="pe-buscar" className="label">Buscar</label>
        <input id="pe-buscar" className="input" placeholder="Apellido, nombre, DNI o email" value={buscar} onChange={(e) => setBuscar(e.target.value)} />
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-slate-500">Mostrando {visibles.length} de {personas.data?.length ?? 0} personas</p>
        <div className="flex gap-2">
          <button
            className="btn-secondary"
            onClick={() =>
              exportarExcel(
                'personas.xlsx',
                'Personas',
                [
                  { titulo: 'Apellido', ancho: 22 },
                  { titulo: 'Nombre', ancho: 22 },
                  { titulo: 'DNI', ancho: 14 },
                  { titulo: 'Email', ancho: 32 },
                  { titulo: 'Teléfono', ancho: 18 },
                ],
                visibles.map((p) => [p.apellido, p.nombre, p.dni, p.email ?? '', p.telefono ?? '']),
              )
            }
          >
            Exportar Excel
          </button>
          <button
            className="btn-secondary"
            onClick={() =>
              exportarCsv(
                'personas.csv',
                ['Apellido', 'Nombre', 'DNI', 'Email', 'Teléfono'],
                visibles.map((p) => [p.apellido, p.nombre, p.dni, p.email ?? '', p.telefono ?? '']),
              )
            }
          >
            Exportar CSV
          </button>
        </div>
      </div>

      {personas.isLoading && <Cargando />}
      {personas.isError && <ErrorCarga onReintentar={() => personas.refetch()} />}

      {personas.data && (
        <div className="card overflow-x-auto">
          <table className="w-full">
            <thead className="bg-brand">
              <tr>
                <th className="th rounded-tl-2xl">Apellido y nombre</th>
                <th className="th">DNI</th>
                <th className="th">Email</th>
                <th className="th">Teléfono</th>
                <th className="th rounded-tr-2xl text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {visibles.map((p) => (
                <tr key={p.id}>
                  <td className="td">{p.apellido}, {p.nombre}</td>
                  <td className="td">{p.dni}</td>
                  <td className="td">{p.email ?? '—'}</td>
                  <td className="td">{p.telefono ?? '—'}</td>
                  <td className="td text-right">
                    <button className="link-accion" onClick={() => abrir(p)}>Editar</button>
                  </td>
                </tr>
              ))}
              {visibles.length === 0 && (
                <tr>
                  <td className="td text-slate-500" colSpan={5}>No hay personas para mostrar.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {editando && (
        <Modal titulo={editando === 'nueva' ? 'Nueva persona' : 'Editar persona'} onClose={() => setEditando(null)}>
          <form
            className="space-y-4"
            onSubmit={(e) => {
              e.preventDefault()
              guardar.mutate()
            }}
          >
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="pe-nombre" className="label">Nombre</label>
                <input id="pe-nombre" maxLength={100} className="input" value={form.nombre} onChange={set('nombre')} />
              </div>
              <div>
                <label htmlFor="pe-apellido" className="label">Apellido</label>
                <input id="pe-apellido" maxLength={100} className="input" value={form.apellido} onChange={set('apellido')} />
              </div>
            </div>
            <div>
              <label htmlFor="pe-dni" className="label">DNI</label>
              <input id="pe-dni" maxLength={10} inputMode="numeric" className="input" value={form.dni} onChange={set('dni')} />
            </div>
            <div>
              <label htmlFor="pe-email" className="label">Email (opcional)</label>
              <input id="pe-email" type="email" maxLength={254} className="input" value={form.email} onChange={set('email')} />
              {!emailOk(form.email.trim()) && <p className="mt-1 text-xs text-red-600">El email no es válido.</p>}
            </div>
            <div>
              <label htmlFor="pe-tel" className="label">Teléfono (opcional)</label>
              <input id="pe-tel" maxLength={30} inputMode="tel" className="input" value={form.telefono} onChange={set('telefono')} />
            </div>
            {guardar.isError && <MensajeError>{guardar.error.message}</MensajeError>}
            <div className="flex justify-end gap-2">
              <button type="button" className="btn-secondary" onClick={() => setEditando(null)}>Cancelar</button>
              <button type="submit" className="btn-primary" disabled={!valido || guardar.isPending}>Guardar</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
