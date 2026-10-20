import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Cargando, ErrorCarga, MensajeError } from '../components/Estados'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import { listarUnidades, reemplazarUnidades } from '../services/consorcios'
import { exportarCsv } from '../services/csv'

interface Fila {
  clave: number
  identificador: string
  coeficiente: string
}

const aNumero = (texto: string) => Number(texto.replace(',', '.'))
const redondear = (n: number) => Math.round(n * 1e6) / 1e6

export default function UnidadesPage() {
  const { activo, cargando } = useConsorcioActivo()
  const qc = useQueryClient()
  const consorcioId = activo?.id

  const unidades = useQuery({
    queryKey: ['unidades', consorcioId],
    queryFn: () => listarUnidades(consorcioId!),
    enabled: !!consorcioId,
  })

  // Borrador editable: el backend recibe el conjunto completo de unidades y valida que sume 100.
  const [filas, setFilas] = useState<Fila[]>([])
  const [siguiente, setSiguiente] = useState(0)
  const [buscar, setBuscar] = useState('')
  const [min, setMin] = useState('')
  const [max, setMax] = useState('')

  const desdeServidor = (datos: { identificador: string; coeficientePropiedad: number }[]) => {
    setFilas(datos.map((u, i) => ({ clave: i, identificador: u.identificador, coeficiente: String(u.coeficientePropiedad) })))
    setSiguiente(datos.length)
  }

  useEffect(() => {
    if (unidades.data) desdeServidor(unidades.data)
  }, [unidades.data])

  const guardar = useMutation({
    mutationFn: () =>
      reemplazarUnidades(
        consorcioId!,
        filas.map((f) => ({ identificador: f.identificador.trim(), coeficientePropiedad: aNumero(f.coeficiente) })),
      ),
    onSuccess: (datos) => {
      qc.setQueryData(['unidades', consorcioId], datos)
      qc.invalidateQueries({ queryKey: ['consorcios'] })
    },
  })

  const suma = useMemo(() => redondear(filas.reduce((s, f) => s + (aNumero(f.coeficiente) || 0), 0)), [filas])
  const sumaOk = filas.length === 0 || Math.abs(suma - 100) <= 0.0001
  const identificadores = filas.map((f) => f.identificador.trim())
  const datosOk =
    filas.every((f) => f.identificador.trim() !== '' && aNumero(f.coeficiente) > 0 && aNumero(f.coeficiente) <= 100) &&
    new Set(identificadores).size === identificadores.length

  const cambios = JSON.stringify(
    filas.map((f) => [f.identificador.trim(), aNumero(f.coeficiente)]),
  ) !== JSON.stringify((unidades.data ?? []).map((u) => [u.identificador, u.coeficientePropiedad]))

  const editar = (clave: number, campo: 'identificador' | 'coeficiente', valor: string) =>
    setFilas((fs) => fs.map((f) => (f.clave === clave ? { ...f, [campo]: valor } : f)))

  const agregar = () => {
    setFilas((fs) => [...fs, { clave: siguiente, identificador: '', coeficiente: '' }])
    setSiguiente((n) => n + 1)
  }

  const visibles = filas.filter((f) => {
    const c = aNumero(f.coeficiente)
    return (
      f.identificador.toLowerCase().includes(buscar.toLowerCase()) &&
      (min === '' || c >= aNumero(min)) &&
      (max === '' || c <= aNumero(max))
    )
  })

  if (cargando) return <Cargando />
  if (!activo) return <p className="text-sm text-slate-500">No hay un consorcio para mostrar.</p>

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Unidades · {activo.nombre}</h1>
        <button className="btn-primary" onClick={agregar}>+ Nueva unidad</button>
      </div>

      <div className={`card p-4 ${sumaOk ? 'border-emerald-300' : 'border-amber-300'}`}>
        <div className="flex items-center justify-between text-sm font-semibold">
          <span>Coeficientes de propiedad</span>
          <span className={sumaOk ? 'text-emerald-600' : 'text-amber-600'}>{suma.toLocaleString('es-AR', { maximumFractionDigits: 6 })}%</span>
        </div>
        <div className="mt-2 h-2 overflow-hidden rounded-full bg-slate-100">
          <div className={`h-full ${sumaOk ? 'bg-emerald-500' : 'bg-amber-500'}`} style={{ width: `${Math.min(suma, 100)}%` }} />
        </div>
        {!sumaOk && <p className="mt-2 text-xs text-amber-700">La suma debe ser 100 para poder guardar.</p>}
      </div>

      <div className="card grid gap-4 p-4 sm:grid-cols-3">
        <div>
          <label htmlFor="f-uni" className="label">Unidad</label>
          <input id="f-uni" className="input" placeholder="Buscar por unidad" value={buscar} onChange={(e) => setBuscar(e.target.value)} />
        </div>
        <div>
          <label htmlFor="f-min" className="label">Coeficiente mínimo</label>
          <input id="f-min" inputMode="decimal" className="input" value={min} onChange={(e) => setMin(e.target.value)} />
        </div>
        <div>
          <label htmlFor="f-max" className="label">Coeficiente máximo</label>
          <input id="f-max" inputMode="decimal" className="input" value={max} onChange={(e) => setMax(e.target.value)} />
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-slate-500">Mostrando {visibles.length} de {filas.length} unidades</p>
        <button
          className="btn-secondary"
          onClick={() => exportarCsv('unidades.csv', ['Unidad', 'Coeficiente %'], visibles.map((f) => [f.identificador, f.coeficiente]))}
        >
          Exportar CSV
        </button>
      </div>

      {unidades.isLoading && <Cargando />}
      {unidades.isError && <ErrorCarga onReintentar={() => unidades.refetch()} />}

      {unidades.data && (
        <div className="card overflow-x-auto">
          <table className="w-full">
            <thead className="bg-brand">
              <tr>
                <th className="th rounded-tl-2xl">Unidad</th>
                <th className="th">Coeficiente %</th>
                <th className="th rounded-tr-2xl text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {visibles.map((f) => (
                <tr key={f.clave}>
                  <td className="td">
                    <input
                      aria-label="Identificador de la unidad"
                      maxLength={20}
                      className="input max-w-40"
                      value={f.identificador}
                      onChange={(e) => editar(f.clave, 'identificador', e.target.value)}
                    />
                  </td>
                  <td className="td">
                    <input
                      aria-label="Coeficiente de propiedad"
                      inputMode="decimal"
                      className="input max-w-40"
                      value={f.coeficiente}
                      onChange={(e) => editar(f.clave, 'coeficiente', e.target.value)}
                    />
                  </td>
                  <td className="td text-right">
                    <button className="link-accion text-red-600" onClick={() => setFilas((fs) => fs.filter((x) => x.clave !== f.clave))}>
                      Quitar
                    </button>
                  </td>
                </tr>
              ))}
              {filas.length === 0 && (
                <tr>
                  <td className="td text-slate-500" colSpan={3}>Este consorcio todavía no tiene unidades.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {guardar.isError && <MensajeError>{guardar.error.message}</MensajeError>}
      {guardar.isSuccess && !cambios && <p role="status" className="text-sm text-emerald-700">Cambios guardados.</p>}

      <div className="flex justify-end gap-2">
        <button className="btn-secondary" disabled={!cambios} onClick={() => unidades.data && desdeServidor(unidades.data)}>
          Descartar cambios
        </button>
        <button className="btn-primary" disabled={!cambios || !sumaOk || !datosOk || guardar.isPending} onClick={() => guardar.mutate()}>
          Guardar unidades
        </button>
      </div>
    </div>
  )
}
