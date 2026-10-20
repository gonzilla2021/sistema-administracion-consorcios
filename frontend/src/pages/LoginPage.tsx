import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { login } from '../services/auth'
import { ApiError } from '../services/api'
import { verificarConexion, type EstadoConexion } from '../services/health'
import { MensajeError } from '../components/Estados'
import type { LoginRequest } from '../types'

const ESTADOS: Record<EstadoConexion, { texto: string; color: string }> = {
  verificando: { texto: 'Verificando conexión…', color: 'bg-slate-400' },
  conectado: { texto: 'Conectado al servidor y a la base de datos', color: 'bg-emerald-500' },
  'sin-base': { texto: 'Servidor activo, sin conexión a la base de datos', color: 'bg-amber-500' },
  'sin-backend': { texto: 'No se pudo conectar con el servidor', color: 'bg-red-500' },
}

export default function LoginPage() {
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [conexion, setConexion] = useState<EstadoConexion>('verificando')

  useEffect(() => {
    verificarConexion().then(setConexion)
  }, [])
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginRequest>()

  const onSubmit = async (datos: LoginRequest) => {
    setError(null)
    try {
      await login(datos)
      navigate('/', { replace: true })
    } catch (e) {
      setError(
        e instanceof ApiError && e.status === 401
          ? 'Email o contraseña incorrectos.'
          : 'No se pudo iniciar sesión. Intentá de nuevo más tarde.',
      )
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center px-4">
      <form onSubmit={handleSubmit(onSubmit)} className="card w-full max-w-sm space-y-5 p-8 shadow-lg" noValidate>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gold">Administración de Consorcios</h1>
          <p className="mt-1 text-sm text-slate-500">Ingresá con tu cuenta</p>
        </div>

        <div>
          <label htmlFor="email" className="label">
            Email
          </label>
          <input
            id="email"
            type="email"
            autoComplete="username"
            className="input"
            {...register('email', { required: 'Ingresá tu email' })}
          />
          {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email.message}</p>}
        </div>

        <div>
          <label htmlFor="password" className="label">
            Contraseña
          </label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            className="input"
            {...register('password', { required: 'Ingresá tu contraseña' })}
          />
          {errors.password && <p className="mt-1 text-xs text-red-600">{errors.password.message}</p>}
        </div>

        {error && <MensajeError>{error}</MensajeError>}

        <button type="submit" disabled={isSubmitting} className="btn-primary w-full">
          {isSubmitting ? 'Ingresando…' : 'Ingresar'}
        </button>

        <p role="status" className="flex items-center gap-2 text-xs text-slate-500">
          <span aria-hidden="true" className={`inline-block h-2 w-2 rounded-full ${ESTADOS[conexion].color}`} />
          {ESTADOS[conexion].texto}
        </p>
      </form>
    </main>
  )
}
