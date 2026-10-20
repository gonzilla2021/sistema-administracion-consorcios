/** Descarga una tabla como CSV (separador `;` y BOM para que Excel respete acentos). */
export function exportarCsv(nombreArchivo: string, encabezados: string[], filas: (string | number)[][]) {
  const celda = (v: string | number) => `"${String(v).replace(/"/g, '""')}"`
  const contenido = [encabezados, ...filas].map((f) => f.map(celda).join(';')).join('\r\n')
  const url = URL.createObjectURL(new Blob(['﻿' + contenido], { type: 'text/csv;charset=utf-8' }))
  const a = document.createElement('a')
  a.href = url
  a.download = nombreArchivo
  a.click()
  URL.revokeObjectURL(url)
}
