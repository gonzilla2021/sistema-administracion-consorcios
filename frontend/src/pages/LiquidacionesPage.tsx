import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Cargando, ErrorCarga, MensajeError } from '../components/Estados'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import {
  cerrarLiquidacion,
  detalleLiquidacion,
  generarLiquidacion,
  listarLiquidaciones,
} from '../services/gastos'
import { exportarCsv } from '../services/csv'
import { exportarExcel } from '../services/excel'
import { moneda } from '../types'

const periodoActual = () => new Date().toISOString().slice(0, 7)

export default function LiquidacionesPage() {
  const { activo, cargando } = useConsorcioActivo()
  const qc = useQueryClient()
  const consorcioId = activo?.id
  const [periodo, setPeriodo] = useState(periodoActual())
  const [selId, setSelId] = useState<string | null>(null)

  const liquidaciones = useQuery({
    queryKey: ['liquidaciones', consorcioId],
    queryFn: () => listarLiquidaciones(consorcioId!),
    enabled: !!consorcioId,
  })
  const datos = liquidaciones.data ?? []
  const sel = datos.find((l) => l.id === selId) ?? null

  const refrescar = () => qc.invalidateQueries({ queryKey: ['liquidaciones', consorcioId] })

  const generar = useMutation({
    mutationFn: () => generarLiquidacion(consorcioId!, periodo),
    onSuccess: (l) => {
      refrescar()
      setSelId(l.id)
    },
  })

  const cerrar = useMutation({
    mutationFn: (id: string) => cerrarLiquidacion(consorcioId!, id),
    onSuccess: refrescar,
  })

  const detalle = useQuery({
    queryKey: ['liq-detalle', consorcioId, selId],
    queryFn: () => detalleLiquidacion(consorcioId!, selId!),
    enabled: !!consorcioId && !!sel,
  })

  if (cargando) return <Cargando />
  if (!activo) return <p className="text-sm text-slate-500">No hay un consorcio para mostrar.</p>

  const cobrado = (detalle.data ?? []).filter((d) => d.estadoPago === 'PAGADO').reduce((s, d) => s + d.importe, 0)

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Liquidaciones · {activo.nombre}</h1>

      <form
        className="card flex flex-wrap items-end gap-4 p-4"
        onSubmit={(e) => {
          e.preventDefault()
          generar.mutate()
        }}
      >
        <div>
          <label htmlFor="l-per" className="label">Período a liquidar</label>
          <input id="l-per" type="month" className="input" value={periodo} onChange={(e) => setPeriodo(e.target.value)} />
        </div>
        <button className="btn-primary" disabled={!periodo || generar.isPending}>Generar liquidación</button>
        {generar.isError && <MensajeError>{generar.error.message}</MensajeError>}
      </form>

      {liquidaciones.isLoading && <Cargando />}
      {liquidaciones.isError && <ErrorCarga onReintentar={() => liquidaciones.refetch()} />}

      {liquidaciones.data && (
        <div className="card overflow-x-auto">
          <table className="w-full">
            <thead className="bg-brand">
              <tr>
                <th className="th rounded-tl-2xl">Período</th>
                <th className="th">Estado</th>
                <th className="th text-right">Total gastos</th>
                <th className="th text-right">Unidades</th>
                <th className="th rounded-tr-2xl text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {datos.map((l) => (
                <tr key={l.id} className={l.id === selId ? 'bg-slate-50' : ''}>
                  <td className="td">{l.periodo}</td>
                  <td className="td">{l.estado === 'ABIERTA' ? 'Abierta' : 'Cerrada'}</td>
                  <td className="td text-right">{moneda(l.totalGastos)}</td>
                  <td className="td text-right">{l.cantidadUnidades}</td>
                  <td className="td space-x-3 text-right">
                    <button className="link-accion" onClick={() => setSelId(l.id)}>Ver detalle</button>
                    {l.estado === 'ABIERTA' && (
                      <button
                        className="link-accion"
                        disabled={cerrar.isPending}
                        onClick={() => confirm(`¿Cerrar la liquidación ${l.periodo}? No podrá modificarse.`) && cerrar.mutate(l.id)}
                      >
                        Cerrar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {datos.length === 0 && (
                <tr>
                  <td className="td text-slate-500" colSpan={5}>Este consorcio todavía no tiene liquidaciones.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
      {cerrar.isError && <MensajeError>{cerrar.error.message}</MensajeError>}

      {sel && (
        <section className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-lg font-semibold">Detalle {sel.periodo}</h2>
            {detalle.data && (
              <div className="flex gap-2">
                <button
                  className="btn-secondary"
                  onClick={() =>
                    exportarExcel(
                      `liquidacion-${sel.periodo}.xlsx`,
                      `Liquidación ${sel.periodo}`,
                      [
                        { titulo: 'Unidad', ancho: 18 },
                        { titulo: 'Importe', ancho: 16, formato: 'moneda' },
                        { titulo: 'Estado de pago', ancho: 16 },
                        { titulo: 'Fecha de pago', ancho: 14 },
                      ],
                      detalle.data.map((d) => [
                        d.identificador,
                        d.importe,
                        d.estadoPago === 'PAGADO' ? 'Pagado' : 'Pendiente',
                        d.fechaPago ?? '',
                      ]),
                    )
                  }
                >
                  Exportar Excel
                </button>
                <button
                  className="btn-secondary"
                  onClick={() =>
                    exportarCsv(
                      `liquidacion-${sel.periodo}.csv`,
                      ['Unidad', 'Importe', 'Estado de pago', 'Fecha de pago'],
                      detalle.data.map((d) => [d.identificador, String(d.importe), d.estadoPago, d.fechaPago ?? '']),
                    )
                  }
                >
                  Exportar CSV
                </button>
              </div>
            )}
          </div>
          {detalle.isLoading && <Cargando />}
          {detalle.isError && <ErrorCarga onReintentar={() => detalle.refetch()} />}
          {detalle.data && (
            <>
              <p className="text-sm text-slate-500">
                Cobrado <span className="font-semibold text-slate-900">{moneda(cobrado)}</span> de {moneda(sel.totalGastos)}
              </p>
              <div className="card overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-brand">
                    <tr>
                      <th className="th rounded-tl-2xl">Unidad</th>
                      <th className="th text-right">Importe</th>
                      <th className="th">Estado de pago</th>
                      <th className="th rounded-tr-2xl">Fecha de pago</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {detalle.data.map((d) => (
                      <tr key={d.id}>
                        <td className="td">{d.identificador}</td>
                        <td className="td text-right">{moneda(d.importe)}</td>
                        <td className="td">
                          <span className={d.estadoPago === 'PAGADO' ? 'text-emerald-700' : 'text-amber-700'}>
                            {d.estadoPago === 'PAGADO' ? 'Pagado' : 'Pendiente'}
                          </span>
                        </td>
                        <td className="td">{d.fechaPago ?? '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </section>
      )}
    </div>
  )
}
