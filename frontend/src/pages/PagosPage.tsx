import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Cargando, ErrorCarga, MensajeError } from '../components/Estados'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import Modal from '../components/Modal'
import { listarPagos, registrarPago, revertirPago } from '../services/pagos'
import { listarLiquidaciones } from '../services/gastos'
import { exportarCsv } from '../services/csv'
import { exportarExcel } from '../services/excel'
import { moneda, type EstadoPago, type Pago } from '../types'

const hoy = () => new Date().toISOString().slice(0, 10)

export default function PagosPage() {
  const { activo, cargando } = useConsorcioActivo()
  const qc = useQueryClient()
  const consorcioId = activo?.id
  const [aPagar, setAPagar] = useState<Pago | null>(null)
  const [fecha, setFecha] = useState(hoy())
  const [periodo, setPeriodo] = useState('')
  const [estado, setEstado] = useState<'' | EstadoPago>('')
  const [buscar, setBuscar] = useState('')

  const pagos = useQuery({
    queryKey: ['pagos', consorcioId],
    queryFn: () => listarPagos(consorcioId!),
    enabled: !!consorcioId,
  })
  // El pago trae solo el id de la liquidación: se cruza con la lista para mostrar período y estado.
  const liquidaciones = useQuery({
    queryKey: ['liquidaciones', consorcioId],
    queryFn: () => listarLiquidaciones(consorcioId!),
    enabled: !!consorcioId,
  })
  const liqPorId = new Map((liquidaciones.data ?? []).map((l) => [l.id, l]))
  const periodoDe = (p: Pago) => liqPorId.get(p.liquidacionId)?.periodo ?? '—'
  const cerrada = (p: Pago) => liqPorId.get(p.liquidacionId)?.estado === 'CERRADA'

  const refrescar = () => {
    qc.invalidateQueries({ queryKey: ['pagos', consorcioId] })
    qc.invalidateQueries({ queryKey: ['liq-detalle', consorcioId] })
  }

  const pagar = useMutation({
    mutationFn: () => registrarPago(consorcioId!, aPagar!.id, fecha),
    onSuccess: () => {
      refrescar()
      setAPagar(null)
    },
  })

  const revertir = useMutation({
    mutationFn: (id: string) => revertirPago(consorcioId!, id),
    onSuccess: refrescar,
  })

  const abrir = (p: Pago) => {
    pagar.reset()
    setFecha(hoy())
    setAPagar(p)
  }

  const visibles = (pagos.data ?? [])
    .filter(
      (p) =>
        (periodo === '' || periodoDe(p) === periodo) &&
        (estado === '' || p.estadoPago === estado) &&
        p.identificadorUnidad.toLowerCase().includes(buscar.toLowerCase()),
    )
    .sort((a, b) => periodoDe(b).localeCompare(periodoDe(a)) || a.identificadorUnidad.localeCompare(b.identificadorUnidad, 'es', { numeric: true }))

  const periodos = [...new Set((liquidaciones.data ?? []).map((l) => l.periodo))].sort().reverse()
  const pendiente = visibles.filter((p) => p.estadoPago === 'PENDIENTE').reduce((s, p) => s + p.importe, 0)
  const cobrado = visibles.filter((p) => p.estadoPago === 'PAGADO').reduce((s, p) => s + p.importe, 0)
  const etiqueta = (e: EstadoPago) => (e === 'PAGADO' ? 'Pagado' : 'Pendiente')

  if (cargando) return <Cargando />
  if (!activo) return <p className="text-sm text-slate-500">No hay un consorcio para mostrar.</p>

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Pagos · {activo.nombre}</h1>

      <div className="card grid gap-4 p-4 sm:grid-cols-3">
        <div>
          <label htmlFor="p-uni" className="label">Unidad</label>
          <input id="p-uni" className="input" placeholder="Buscar por unidad" value={buscar} onChange={(e) => setBuscar(e.target.value)} />
        </div>
        <div>
          <label htmlFor="p-per" className="label">Período</label>
          <select id="p-per" className="input" value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
            <option value="">Todos</option>
            {periodos.map((p) => (
              <option key={p} value={p}>{p}</option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="p-est" className="label">Estado</label>
          <select id="p-est" className="input" value={estado} onChange={(e) => setEstado(e.target.value as '' | EstadoPago)}>
            <option value="">Todos</option>
            <option value="PENDIENTE">Pendiente</option>
            <option value="PAGADO">Pagado</option>
          </select>
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-slate-500">
          {visibles.length} registros · Cobrado <span className="font-semibold text-emerald-700">{moneda(cobrado)}</span> · Pendiente{' '}
          <span className="font-semibold text-amber-700">{moneda(pendiente)}</span>
        </p>
        <div className="flex gap-2">
          <button
            className="btn-secondary"
            onClick={() =>
              exportarExcel(
                'pagos.xlsx',
                'Pagos',
                [
                  { titulo: 'Período', ancho: 12 },
                  { titulo: 'Unidad', ancho: 18 },
                  { titulo: 'Importe', ancho: 16, formato: 'moneda' },
                  { titulo: 'Estado', ancho: 14 },
                  { titulo: 'Fecha de pago', ancho: 14 },
                ],
                visibles.map((p) => [periodoDe(p), p.identificadorUnidad, p.importe, etiqueta(p.estadoPago), p.fechaPago ?? '']),
              )
            }
          >
            Exportar Excel
          </button>
          <button
            className="btn-secondary"
            onClick={() =>
              exportarCsv(
                'pagos.csv',
                ['Período', 'Unidad', 'Importe', 'Estado', 'Fecha de pago'],
                visibles.map((p) => [periodoDe(p), p.identificadorUnidad, String(p.importe), etiqueta(p.estadoPago), p.fechaPago ?? '']),
              )
            }
          >
            Exportar CSV
          </button>
        </div>
      </div>

      {(pagos.isLoading || liquidaciones.isLoading) && <Cargando />}
      {(pagos.isError || liquidaciones.isError) && (
        <ErrorCarga
          onReintentar={() => {
            pagos.refetch()
            liquidaciones.refetch()
          }}
        />
      )}
      {revertir.isError && <MensajeError>{revertir.error.message}</MensajeError>}

      {pagos.data && liquidaciones.data && (
        <div className="card overflow-x-auto">
          <table className="w-full">
            <thead className="bg-brand">
              <tr>
                <th className="th rounded-tl-2xl">Período</th>
                <th className="th">Unidad</th>
                <th className="th text-right">Importe</th>
                <th className="th">Estado</th>
                <th className="th">Fecha de pago</th>
                <th className="th rounded-tr-2xl text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {visibles.map((p) => (
                <tr key={p.id}>
                  <td className="td">{periodoDe(p)}</td>
                  <td className="td">{p.identificadorUnidad}</td>
                  <td className="td text-right">{moneda(p.importe)}</td>
                  <td className="td">
                    <span className={p.estadoPago === 'PAGADO' ? 'text-emerald-700' : 'text-amber-700'}>{etiqueta(p.estadoPago)}</span>
                  </td>
                  <td className="td">{p.fechaPago ?? '—'}</td>
                  <td className="td text-right">
                    {p.estadoPago === 'PENDIENTE' ? (
                      cerrada(p) ? (
                        <button className="link-accion" onClick={() => abrir(p)}>Registrar pago</button>
                      ) : (
                        <span className="text-xs text-slate-500">Liquidación abierta</span>
                      )
                    ) : (
                      <button
                        className="link-accion text-red-600"
                        disabled={revertir.isPending}
                        onClick={() =>
                          confirm(`¿Revertir el pago de ${p.identificadorUnidad} (${periodoDe(p)})?`) && revertir.mutate(p.id)
                        }
                      >
                        Revertir
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {visibles.length === 0 && (
                <tr>
                  <td className="td text-slate-500" colSpan={6}>No hay registros para mostrar.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {aPagar && (
        <Modal titulo="Registrar pago" onClose={() => setAPagar(null)}>
          <form
            className="space-y-4"
            onSubmit={(e) => {
              e.preventDefault()
              pagar.mutate()
            }}
          >
            <p className="text-sm text-slate-600">
              Unidad <span className="font-semibold">{aPagar.identificadorUnidad}</span> · {periodoDe(aPagar)} ·{' '}
              <span className="font-semibold">{moneda(aPagar.importe)}</span>
            </p>
            <div>
              <label htmlFor="p-fecha" className="label">Fecha de pago</label>
              <input id="p-fecha" type="date" max={hoy()} className="input" value={fecha} onChange={(e) => setFecha(e.target.value)} />
            </div>
            {pagar.isError && <MensajeError>{pagar.error.message}</MensajeError>}
            <div className="flex justify-end gap-2">
              <button type="button" className="btn-secondary" onClick={() => setAPagar(null)}>Cancelar</button>
              <button type="submit" className="btn-primary" disabled={!fecha || pagar.isPending}>Registrar</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
