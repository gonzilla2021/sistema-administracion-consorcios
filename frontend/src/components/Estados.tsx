export function Cargando() {
  return <p className="text-sm text-slate-500">Cargando…</p>
}

export function ErrorCarga({ onReintentar }: { onReintentar: () => void }) {
  return (
    <div role="alert" className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
      No se pudieron cargar los datos.{' '}
      <button onClick={onReintentar} className="underline cursor-pointer">
        Reintentar
      </button>
    </div>
  )
}

export function MensajeError({ children }: { children: React.ReactNode }) {
  return (
    <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
      {children}
    </p>
  )
}
