import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Cargando, ErrorCarga } from '../components/Estados'
import { listarMisLiquidaciones } from '../services/miCuenta'
import { moneda, type EstadoPago } from '../types'

const formatearPeriodo = (periodo: string) => {
  const [anio, mes] = periodo.split('-').map(Number)

  if (!anio || !mes) {
    return periodo
  }

  return new Intl.DateTimeFormat('es-AR', {
    month: 'long',
    year: 'numeric',
  }).format(new Date(anio, mes - 1, 1))
}

const formatearFecha = (fecha: string | null) => {
  if (!fecha) {
    return 'Sin registrar'
  }

  return new Intl.DateTimeFormat('es-AR').format(
    new Date(`${fecha}T00:00:00`),
  )
}

export default function MisLiquidacionesPage() {
  const [estado, setEstado] = useState<'' | EstadoPago>('')

  const consulta = useQuery({
    queryKey: ['mis-liquidaciones'],
    queryFn: listarMisLiquidaciones,
    retry: false,
  })

  const datos = consulta.data ?? []

  const liquidaciones = useMemo(
    () =>
      datos.filter(
        (liquidacion) =>
          estado === '' || liquidacion.estadoPago === estado,
      ),
    [datos, estado],
  )

  const totalPendiente = liquidaciones
    .filter((liquidacion) => liquidacion.estadoPago === 'PENDIENTE')
    .reduce((total, liquidacion) => total + liquidacion.importe, 0)

  const totalPagado = liquidaciones
    .filter((liquidacion) => liquidacion.estadoPago === 'PAGADO')
    .reduce((total, liquidacion) => total + liquidacion.importe, 0)

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold text-slate-900">
          Mis liquidaciones
        </h1>

        <p className="mt-1 text-sm text-slate-500">
          Consultá las expensas correspondientes a tus unidades.
        </p>
      </header>

      {consulta.isLoading && <Cargando />}

      {consulta.isError && (
        <ErrorCarga onReintentar={() => consulta.refetch()} />
      )}

      {consulta.data && (
        <>
          <section className="grid gap-4 sm:grid-cols-3">
            <TarjetaResumen
              titulo="Liquidaciones"
              valor={String(liquidaciones.length)}
            />

            <TarjetaResumen
              titulo="Total pagado"
              valor={moneda(totalPagado)}
              color="verde"
            />

            <TarjetaResumen
              titulo="Total pendiente"
              valor={moneda(totalPendiente)}
              color="amarillo"
            />
          </section>

          <section className="card p-4">
            <label
              htmlFor="estado-pago"
              className="mb-1 block text-sm font-medium text-slate-700"
            >
              Estado de pago
            </label>

            <select
              id="estado-pago"
              className="input max-w-xs"
              value={estado}
              onChange={(event) =>
                setEstado(event.target.value as '' | EstadoPago)
              }
            >
              <option value="">Todos</option>
              <option value="PENDIENTE">Pendientes</option>
              <option value="PAGADO">Pagados</option>
            </select>
          </section>

          <section className="card overflow-x-auto">
            <table className="w-full min-w-[700px]">
              <thead className="bg-brand">
                <tr>
                  <th className="th rounded-tl-2xl">Período</th>
                  <th className="th">Unidad</th>
                  <th className="th text-right">Importe</th>
                  <th className="th">Estado</th>
                  <th className="th rounded-tr-2xl">
                    Fecha de pago
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-slate-100">
                {liquidaciones.map((liquidacion) => (
                  <tr
                    key={`${liquidacion.liquidacionId}-${liquidacion.unidadFuncionalId}`}
                  >
                    <td className="td capitalize">
                      {formatearPeriodo(liquidacion.periodo)}
                    </td>

                    <td className="td font-medium">
                      {liquidacion.unidad}
                    </td>

                    <td className="td text-right">
                      {moneda(liquidacion.importe)}
                    </td>

                    <td className="td">
                      <EtiquetaEstado
                        estado={liquidacion.estadoPago}
                      />
                    </td>

                    <td className="td">
                      {formatearFecha(liquidacion.fechaPago)}
                    </td>
                  </tr>
                ))}

                {liquidaciones.length === 0 && (
                  <tr>
                    <td
                      colSpan={5}
                      className="td py-10 text-center text-slate-500"
                    >
                      {datos.length === 0
                        ? 'Todavía no tenés liquidaciones disponibles.'
                        : 'No hay liquidaciones para el filtro seleccionado.'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </section>
        </>
      )}
    </div>
  )
}

interface TarjetaResumenProps {
  titulo: string
  valor: string
  color?: 'normal' | 'verde' | 'amarillo'
}

function TarjetaResumen({
  titulo,
  valor,
  color = 'normal',
}: TarjetaResumenProps) {
  const colores = {
    normal: {
      tarjeta: '',
      titulo: 'text-slate-500',
      valor: 'text-slate-900',
    },
    verde: {
      tarjeta: 'border-emerald-200',
      titulo: 'text-emerald-700',
      valor: 'text-emerald-700',
    },
    amarillo: {
      tarjeta: 'border-amber-200',
      titulo: 'text-amber-700',
      valor: 'text-amber-700',
    },
  }

  const estilo = colores[color]

  return (
    <article className={`card p-5 ${estilo.tarjeta}`}>
      <p
        className={`text-xs font-semibold uppercase tracking-wide ${estilo.titulo}`}
      >
        {titulo}
      </p>

      <p className={`mt-2 text-2xl font-bold ${estilo.valor}`}>
        {valor}
      </p>
    </article>
  )
}

function EtiquetaEstado({ estado }: { estado: EstadoPago }) {
  const pagado = estado === 'PAGADO'

  return (
    <span
      className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
        pagado
          ? 'bg-emerald-100 text-emerald-700'
          : 'bg-amber-100 text-amber-700'
      }`}
    >
      {pagado ? 'Pagado' : 'Pendiente'}
    </span>
  )
}
