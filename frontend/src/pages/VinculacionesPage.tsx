import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Cargando, ErrorCarga, MensajeError } from '../components/Estados'
import { useConsorcioActivo } from '../components/ConsorcioActivo'
import Modal from '../components/Modal'
import { listarUnidades } from '../services/consorcios'
import {
  crearVinculacion,
  finalizarVinculacion,
  listarPersonas,
  listarVinculaciones,
} from '../services/personas'
import { exportarExcel } from '../services/excel'
import type { TipoVinculo, Vinculacion } from '../types'

const hoy = () => new Date().toISOString().slice(0, 10)
const tipoLegible = (t: TipoVinculo) => (t === 'PROPIETARIO' ? 'Propietario' : 'Inquilino')

export default function VinculacionesPage() {
  const { activo, cargando } = useConsorcioActivo()
  const qc = useQueryClient()
  const consorcioId = activo?.id
  const [unidadSel, setUnidadSel] = useState('')
  const [nueva, setNueva] = useState(false)
  const [personaId, setPersonaId] = useState('')
  const [tipo, setTipo] = useState<TipoVinculo>('PROPIETARIO')
  const [inicio, setInicio] = useState(hoy())
  const [aFinalizar, setAFinalizar] = useState<Vinculacion | null>(null)
  const [fin, setFin] = useState(hoy())

  const unidades = useQuery({
    queryKey: ['unidades', consorcioId],
    queryFn: () => listarUnidades(consorcioId!),
    enabled: !!consorcioId,
  })
  const unidadId = unidades.data?.some((u) => u.id === unidadSel) ? unidadSel : (unidades.data?.[0]?.id ?? '')
  const unidad = unidades.data?.find((u) => u.id === unidadId)

  const vinculos = useQuery({
    queryKey: ['vinculaciones', consorcioId, unidadId],
    queryFn: () => listarVinculaciones(consorcioId!, unidadId),
    enabled: !!consorcioId && !!unidadId,
  })

  const personas = useQuery({ queryKey: ['personas', consorcioId], queryFn: () => listarPersonas(consorcioId!), enabled: nueva && !!consorcioId, retry: false })

  const refrescar = () => qc.invalidateQueries({ queryKey: ['vinculaciones', consorcioId, unidadId] })

  const crear = useMutation({
    mutationFn: () => crearVinculacion(consorcioId!, unidadId, { personaId, tipo, fechaInicio: inicio }),
    onSuccess: () => {
      refrescar()
      setNueva(false)
    },
  })

  const finalizar = useMutation({
    mutationFn: () => finalizarVinculacion(consorcioId!, unidadId, aFinalizar!.id, fin),
    onSuccess: () => {
      refrescar()
      setAFinalizar(null)
    },
  })

  const abrirNueva = () => {
    crear.reset()
    setPersonaId('')
    setTipo('PROPIETARIO')
    setInicio(hoy())
    setNueva(true)
  }

  const lista = (vinculos.data ?? []).slice().sort((a, b) => b.fechaInicio.localeCompare(a.fechaInicio))

  if (cargando) return <Cargando />
  if (!activo) return <p className="text-sm text-slate-500">No hay un consorcio para mostrar.</p>

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Vinculaciones · {activo.nombre}</h1>
        <button className="btn-primary" disabled={!unidadId} onClick={abrirNueva}>+ Nueva vinculación</button>
      </div>

      {unidades.isLoading && <Cargando />}
      {unidades.isError && <ErrorCarga onReintentar={() => unidades.refetch()} />}

      {unidades.data && (
        <div className="card p-4">
          <label htmlFor="v-uni" className="label">Unidad</label>
          <select id="v-uni" className="input max-w-xs" value={unidadId} onChange={(e) => setUnidadSel(e.target.value)}>
            {unidades.data.map((u) => (
              <option key={u.id} value={u.id}>{u.identificador}</option>
            ))}
          </select>
          {unidades.data.length === 0 && <p className="mt-2 text-sm text-slate-500">Este consorcio no tiene unidades.</p>}
        </div>
      )}

      {unidadId && (
        <>
          <div className="flex justify-end">
            <button
              className="btn-secondary"
              disabled={lista.length === 0}
              onClick={() =>
                exportarExcel(
                  `vinculaciones-${unidad?.identificador ?? 'unidad'}.xlsx`,
                  'Vinculaciones',
                  [
                    { titulo: 'Persona', ancho: 32 },
                    { titulo: 'Tipo', ancho: 14 },
                    { titulo: 'Inicio', ancho: 12 },
                    { titulo: 'Fin', ancho: 12 },
                    { titulo: 'Vigente', ancho: 10 },
                  ],
                  lista.map((v) => [`${v.apellido}, ${v.nombre}`, tipoLegible(v.tipo), v.fechaInicio, v.fechaFin ?? '', v.vigente ? 'Sí' : 'No']),
                )
              }
            >
              Exportar Excel
            </button>
          </div>

          {vinculos.isLoading && <Cargando />}
          {vinculos.isError && <ErrorCarga onReintentar={() => vinculos.refetch()} />}

          {vinculos.data && (
            <div className="card overflow-x-auto">
              <table className="w-full">
                <thead className="bg-brand">
                  <tr>
                    <th className="th rounded-tl-2xl">Persona</th>
                    <th className="th">Tipo</th>
                    <th className="th">Inicio</th>
                    <th className="th">Fin</th>
                    <th className="th">Estado</th>
                    <th className="th rounded-tr-2xl text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {lista.map((v) => (
                    <tr key={v.id}>
                      <td className="td">{v.apellido}, {v.nombre}</td>
                      <td className="td">{tipoLegible(v.tipo)}</td>
                      <td className="td">{v.fechaInicio}</td>
                      <td className="td">{v.fechaFin ?? '—'}</td>
                      <td className="td">
                        <span className={v.vigente ? 'text-emerald-700' : 'text-slate-500'}>{v.vigente ? 'Vigente' : 'Finalizada'}</span>
                      </td>
                      <td className="td text-right">
                        {v.vigente && (
                          <button
                            className="link-accion"
                            onClick={() => {
                              finalizar.reset()
                              setFin(hoy())
                              setAFinalizar(v)
                            }}
                          >
                            Finalizar
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                  {lista.length === 0 && (
                    <tr>
                      <td className="td text-slate-500" colSpan={6}>Esta unidad no tiene vinculaciones.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {nueva && (
        <Modal titulo={`Nueva vinculación · ${unidad?.identificador ?? ''}`} onClose={() => setNueva(false)}>
          <form
            className="space-y-4"
            onSubmit={(e) => {
              e.preventDefault()
              crear.mutate()
            }}
          >
            <div>
              <label htmlFor="v-per" className="label">Persona</label>
              {personas.isError ? (
                <MensajeError>No se pudo cargar la lista de personas.</MensajeError>
              ) : (
                <select id="v-per" className="input" value={personaId} onChange={(e) => setPersonaId(e.target.value)}>
                  <option value="">{personas.isLoading ? 'Cargando…' : 'Elegí una persona'}</option>
                  {(personas.data ?? [])
                    .slice()
                    .sort((a, b) => a.apellido.localeCompare(b.apellido, 'es'))
                    .map((p) => (
                      <option key={p.id} value={p.id}>{p.apellido}, {p.nombre} · {p.dni}</option>
                    ))}
                </select>
              )}
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="v-tipo" className="label">Tipo</label>
                <select id="v-tipo" className="input" value={tipo} onChange={(e) => setTipo(e.target.value as TipoVinculo)}>
                  <option value="PROPIETARIO">Propietario</option>
                  <option value="INQUILINO">Inquilino</option>
                </select>
              </div>
              <div>
                <label htmlFor="v-ini" className="label">Fecha de inicio</label>
                <input id="v-ini" type="date" className="input" value={inicio} onChange={(e) => setInicio(e.target.value)} />
              </div>
            </div>
            {crear.isError && <MensajeError>{crear.error.message}</MensajeError>}
            <div className="flex justify-end gap-2">
              <button type="button" className="btn-secondary" onClick={() => setNueva(false)}>Cancelar</button>
              <button type="submit" className="btn-primary" disabled={!personaId || !inicio || crear.isPending}>Vincular</button>
            </div>
          </form>
        </Modal>
      )}

      {aFinalizar && (
        <Modal titulo="Finalizar vinculación" onClose={() => setAFinalizar(null)}>
          <form
            className="space-y-4"
            onSubmit={(e) => {
              e.preventDefault()
              finalizar.mutate()
            }}
          >
            <p className="text-sm text-slate-600">
              {aFinalizar.apellido}, {aFinalizar.nombre} · {tipoLegible(aFinalizar.tipo)} desde {aFinalizar.fechaInicio}
            </p>
            <div>
              <label htmlFor="v-fin" className="label">Fecha de fin</label>
              <input id="v-fin" type="date" min={aFinalizar.fechaInicio} className="input" value={fin} onChange={(e) => setFin(e.target.value)} />
            </div>
            {finalizar.isError && <MensajeError>{finalizar.error.message}</MensajeError>}
            <div className="flex justify-end gap-2">
              <button type="button" className="btn-secondary" onClick={() => setAFinalizar(null)}>Cancelar</button>
              <button type="submit" className="btn-primary" disabled={!fin || finalizar.isPending}>Finalizar</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
