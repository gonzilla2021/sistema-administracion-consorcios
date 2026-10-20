import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import Modal from '../components/Modal'
import { Cargando, ErrorCarga, MensajeError } from '../components/Estados'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import { cambiarEstadoUsuario, crearUsuario, listarUsuarios } from '../services/consorcios'
import { ROL_LEGIBLE, type Rol, type UsuarioInput } from '../types'

const ROLES = Object.keys(ROL_LEGIBLE) as Rol[]

function FormUsuario({ onClose }: { onClose: () => void }) {
  const qc = useQueryClient()
  const { consorcios } = useConsorcioActivo()
  const { register, handleSubmit, watch, formState: { errors } } = useForm<UsuarioInput>({
    defaultValues: { rol: 'ADMINISTRADOR_CONSORCIO', consorcioId: consorcios[0]?.id ?? '' },
  })
  const esAdmin = watch('rol') === 'ADMINISTRADOR_CONSORCIO'
  const crear = useMutation({
    mutationFn: (d: UsuarioInput) => crearUsuario({ ...d, consorcioId: d.rol === 'ADMINISTRADOR_CONSORCIO' ? d.consorcioId : null }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['usuarios'] })
      onClose()
    },
  })

  return (
    <Modal titulo="Nuevo usuario" onClose={onClose}>
      <form onSubmit={handleSubmit((d) => crear.mutate(d))} className="space-y-4" noValidate>
        <div>
          <label htmlFor="u-email" className="label">Email</label>
          <input id="u-email" type="email" className="input" {...register('email', { required: 'Ingresá el email' })} />
          {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email.message}</p>}
        </div>
        <div>
          <label htmlFor="u-pass" className="label">Contraseña inicial</label>
          <input
            id="u-pass"
            type="password"
            autoComplete="new-password"
            className="input"
            {...register('password', { required: 'Ingresá una contraseña', minLength: { value: 8, message: 'Mínimo 8 caracteres' } })}
          />
          {errors.password && <p className="mt-1 text-xs text-red-600">{errors.password.message}</p>}
        </div>
        <div>
          <label htmlFor="u-rol" className="label">Rol</label>
          <select id="u-rol" className="input" {...register('rol')}>
            {ROLES.map((r) => (
              <option key={r} value={r}>{ROL_LEGIBLE[r]}</option>
            ))}
          </select>
        </div>
        {esAdmin && (
          <div>
            <label htmlFor="u-cons" className="label">Consorcio que administra</label>
            <select id="u-cons" className="input" {...register('consorcioId', { required: true })}>
              {consorcios.map((c) => (
                <option key={c.id} value={c.id}>{c.nombre}</option>
              ))}
            </select>
          </div>
        )}
        {crear.isError && <MensajeError>{crear.error.message}</MensajeError>}
        <div className="flex justify-end gap-2">
          <button type="button" className="btn-secondary" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn-primary" disabled={crear.isPending}>Crear</button>
        </div>
      </form>
    </Modal>
  )
}

export default function UsuariosPage() {
  const qc = useQueryClient()
  const { consorcios } = useConsorcioActivo()
  const [email, setEmail] = useState('')
  const [rol, setRol] = useState<Rol | ''>('')
  const [creando, setCreando] = useState(false)

  const usuarios = useQuery({ queryKey: ['usuarios'], queryFn: listarUsuarios })
  const cambiar = useMutation({
    mutationFn: ({ id, activo }: { id: string; activo: boolean }) => cambiarEstadoUsuario(id, activo),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['usuarios'] }),
  })

  const nombreConsorcio = (id: string | null) => consorcios.find((c) => c.id === id)?.nombre ?? '—'
  const visibles = (usuarios.data ?? []).filter(
    (u) => u.email.toLowerCase().includes(email.toLowerCase()) && (rol === '' || u.rol === rol),
  )

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Usuarios</h1>
        <button className="btn-primary" onClick={() => setCreando(true)}>+ Nuevo usuario</button>
      </div>

      <div className="card grid gap-4 p-4 sm:grid-cols-2">
        <div>
          <label htmlFor="f-email" className="label">Email</label>
          <input id="f-email" className="input" placeholder="Buscar por email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div>
          <label htmlFor="f-rol" className="label">Rol</label>
          <select id="f-rol" className="input" value={rol} onChange={(e) => setRol(e.target.value as Rol | '')}>
            <option value="">Todos</option>
            {ROLES.map((r) => (
              <option key={r} value={r}>{ROL_LEGIBLE[r]}</option>
            ))}
          </select>
        </div>
      </div>

      {usuarios.isLoading && <Cargando />}
      {usuarios.isError && <ErrorCarga onReintentar={() => usuarios.refetch()} />}
      {cambiar.isError && <MensajeError>{cambiar.error.message}</MensajeError>}

      {usuarios.data && (
        <div className="card overflow-x-auto">
          <table className="w-full">
            <thead className="bg-brand">
              <tr>
                <th className="th rounded-tl-2xl">Email</th>
                <th className="th">Rol</th>
                <th className="th">Consorcio</th>
                <th className="th">Estado</th>
                <th className="th rounded-tr-2xl text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {visibles.map((u) => (
                <tr key={u.id}>
                  <td className="td font-medium">{u.email}</td>
                  <td className="td">{ROL_LEGIBLE[u.rol]}</td>
                  <td className="td">{nombreConsorcio(u.consorcioId)}</td>
                  <td className="td">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${u.activo ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                      {u.activo ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td className="td text-right">
                    <button className="link-accion" disabled={cambiar.isPending} onClick={() => cambiar.mutate({ id: u.id, activo: !u.activo })}>
                      {u.activo ? 'Desactivar' : 'Activar'}
                    </button>
                  </td>
                </tr>
              ))}
              {visibles.length === 0 && (
                <tr>
                  <td className="td text-slate-500" colSpan={5}>No hay usuarios que coincidan.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {creando && <FormUsuario onClose={() => setCreando(false)} />}
    </div>
  )
}
