import { useForm } from 'react-hook-form'
import { useMutation } from '@tanstack/react-query'
import { MensajeError } from '../components/Estados'
import { cambiarPassword } from '../services/auth'

interface Formulario {
  passwordActual: string
  passwordNueva: string
  confirmacion: string
}

export default function CuentaPage() {
  const { register, handleSubmit, reset, watch, formState: { errors } } = useForm<Formulario>()
  const cambiar = useMutation({
    mutationFn: (d: Formulario) => cambiarPassword(d.passwordActual, d.passwordNueva),
    onSuccess: () => reset(),
  })

  return (
    <div className="max-w-md space-y-6">
      <h1 className="text-2xl font-bold">Cambiar contraseña</h1>
      <form onSubmit={handleSubmit((d) => cambiar.mutate(d))} className="card space-y-4 p-6" noValidate>
        <div>
          <label htmlFor="actual" className="label">Contraseña actual</label>
          <input id="actual" type="password" autoComplete="current-password" className="input" {...register('passwordActual', { required: 'Ingresá tu contraseña actual' })} />
          {errors.passwordActual && <p className="mt-1 text-xs text-red-600">{errors.passwordActual.message}</p>}
        </div>
        <div>
          <label htmlFor="nueva" className="label">Contraseña nueva</label>
          <input
            id="nueva"
            type="password"
            autoComplete="new-password"
            className="input"
            {...register('passwordNueva', {
              required: 'Ingresá la nueva contraseña',
              minLength: { value: 8, message: 'Mínimo 8 caracteres' },
              maxLength: { value: 72, message: 'Máximo 72 caracteres' },
            })}
          />
          {errors.passwordNueva && <p className="mt-1 text-xs text-red-600">{errors.passwordNueva.message}</p>}
        </div>
        <div>
          <label htmlFor="confirmacion" className="label">Repetir contraseña nueva</label>
          <input
            id="confirmacion"
            type="password"
            autoComplete="new-password"
            className="input"
            {...register('confirmacion', { validate: (v) => v === watch('passwordNueva') || 'Las contraseñas no coinciden' })}
          />
          {errors.confirmacion && <p className="mt-1 text-xs text-red-600">{errors.confirmacion.message}</p>}
        </div>
        {cambiar.isError && <MensajeError>{cambiar.error.message}</MensajeError>}
        {cambiar.isSuccess && <p role="status" className="text-sm text-emerald-700">Contraseña actualizada.</p>}
        <button type="submit" className="btn-primary" disabled={cambiar.isPending}>Guardar contraseña</button>
      </form>
    </div>
  )
}
