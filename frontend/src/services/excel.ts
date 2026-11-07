export interface ColumnaExcel {
  titulo: string
  /** Ancho en caracteres. */
  ancho?: number
  /** 'moneda' aplica formato $ y alinea a la derecha. */
  formato?: 'moneda'
}

/** Descarga una tabla como .xlsx: encabezado en negrita con color, filtros, fila fija y columnas con ancho. */
export async function exportarExcel(
  nombreArchivo: string,
  hoja: string,
  columnas: ColumnaExcel[],
  filas: (string | number | null)[][],
) {
  // exceljs es pesado: se carga recién al exportar.
  const { Workbook } = await import('exceljs')
  const libro = new Workbook()
  const ws = libro.addWorksheet(hoja, { views: [{ state: 'frozen', ySplit: 1 }] })

  ws.columns = columnas.map((c) => ({
    header: c.titulo,
    width: c.ancho ?? 18,
    style:
      c.formato === 'moneda'
        ? { numFmt: '"$" #,##0.00', alignment: { horizontal: 'right' as const } }
        : {},
  }))
  ws.addRows(filas)

  const cabecera = ws.getRow(1)
  cabecera.font = { bold: true, color: { argb: 'FFFFFFFF' } }
  cabecera.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF1F3A5F' } }
  cabecera.alignment = { vertical: 'middle' }
  cabecera.height = 22
  ws.autoFilter = { from: { row: 1, column: 1 }, to: { row: 1, column: columnas.length } }

  const buffer = await libro.xlsx.writeBuffer()
  const url = URL.createObjectURL(
    new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
  )
  const a = document.createElement('a')
  a.href = url
  a.download = nombreArchivo
  a.click()
  URL.revokeObjectURL(url)
}
