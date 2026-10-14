import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { login } from '../services/auth'
import type { LoginRequest } from '../types'

export default function LoginPage() {
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
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
    } catch {
      setError('No se pudo iniciar sesión. Revisá tus datos o intentá más tarde.')
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="w-full max-w-sm bg-white rounded-xl shadow-sm border border-slate-200 p-8 space-y-5"
        noValidate
      >
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Administración de Consorcios</h1>
          <p className="text-sm text-slate-500 mt-1">Ingresá con tu cuenta</p>
        </div>

        <div className="space-y-1">
          <label htmlFor="email" className="block text-sm font-medium text-slate-700">
            Email
          </label>
          <input
            id="email"
            type="email"
            autoComplete="username"
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            {...register('email', { required: 'Ingresá tu email' })}
          />
          {errors.email && <p className="text-xs text-red-600">{errors.email.message}</p>}
        </div>

        <div className="space-y-1">
          <label htmlFor="password" className="block text-sm font-medium text-slate-700">
            Contraseña
          </label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            {...register('password', { required: 'Ingresá tu contraseña' })}
          />
          {errors.password && <p className="text-xs text-red-600">{errors.password.message}</p>}
        </div>

        {error && (
          <p role="alert" className="text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-lg bg-indigo-600 text-white text-sm font-medium py-2.5 hover:bg-indigo-700 disabled:opacity-60 cursor-pointer"
        >
          {isSubmitting ? 'Ingresando…' : 'Ingresar'}
        </button>
      </form>
    </main>
  )
}
