# QA: recorrido funcional del sistema

**Proyecto:** Sistema de Administración de Consorcios — Grupo 128
**Fecha del recorrido manual:** 7 de noviembre de 2026
**Actualización de pruebas automatizadas:** 8 de noviembre de 2026
**Tipo de prueba:** recorrido manual de caja negra en navegador contra el backend y Supabase (secciones 1 a 6), complementado con pruebas automatizadas del backend sin conexión a la base de datos (sección 7).

Este documento deja constancia de qué se probó, con qué resultado y qué defectos aparecieron. Sirve como evidencia de validación para el informe final y como registro de la cobertura automatizada y de las verificaciones pendientes.

## 1. Entorno y datos

| Elemento | Detalle |
|---|---|
| Frontend | Vite en `http://localhost:5173` |
| Backend | Spring Boot en `http://localhost:9090` |
| Base de datos | Supabase (PostgreSQL 17), esquema `dbo`, con las migraciones aplicadas |
| Usuario | `admin1@demo.test` (administrador de Demo 1 y Demo 2) |
| Consorcio | **Consorcio Demo 2**: 6 unidades de coeficiente casi igual (16,666667 %), 49 gastos y 6 liquidaciones previas |
| Período | Noviembre de 2026 (`2026-11`), libre de liquidación |

Los datos creados durante la prueba llevan el prefijo `QA` para poder identificarlos y borrarlos (ver sección 6).

## 2. Resumen

| Resultado | Casos |
|---|---:|
| Casos ejecutados correctos (OK) | 30 |
| Casos ejecutados con defecto | 1 |
| **Total de casos ejecutados** | **31** |
| Hallazgos por revisión de código, sin ejecutar (H-2) | 1 |

Los dos defectos de lógica (H-1 y H-2) se corrigieron el mismo día y se reverificaron (sección 4.1). Las reglas de negocio centrales (prorrateo exacto, período cerrado inmutable, pago solo sobre liquidación cerrada, DNI único por consorcio, acceso restringido por consorcio) funcionaron como se espera. El defecto ejecutado fue la falta de validación de vinculaciones duplicadas (H-1).

## 3. Casos ejecutados

Estados: **OK** = el resultado obtenido coincide con el esperado. **DEFECTO** = no coincide.

### 3.1 Acceso y sesión

| ID | Caso | Esperado | Obtenido | Estado |
|---|---|---|---|---|
| AC-01 | Login con contraseña incorrecta | Mensaje de error, sin sesión nueva | "Email o contraseña incorrectos." | OK |
| AC-02 | Login válido de `admin1` | Ingresa al tablero | Ingresa; el menú muestra las pantallas de gestión | OK |
| AC-03 | Cambiar contraseña con la actual incorrecta | Rechazo, la sesión sigue activa | "La contraseña actual no es correcta"; sesión intacta | OK |
| AC-04 | `admin1` consulta personas de Demo 3 (que no administra) | `403` | `403` | OK |
| AC-05 | `admin1` con dos consorcios | Selector con ambos; la lista del API trae solo los suyos | Selector Demo 1 y Demo 2 | OK |
| AC-06 | Administrador sin consorcios (`admin3`) | Pantallas con "No hay un consorcio para mostrar" | Correcto en todas (ver H-4 corregido) | OK |

### 3.2 Personas

| ID | Caso | Esperado | Obtenido | Estado |
|---|---|---|---|---|
| PE-01 | Alta de persona válida | Aparece en el listado | Fila creada, "Mostrando 1 de 1" | OK |
| PE-02 | Alta con DNI ya existente en el consorcio | Conflicto, no se crea | "Ya existe una persona con ese DNI en el consorcio"; sin fila nueva | OK |
| PE-03 | Email con formato inválido | Aviso y botón Guardar deshabilitado | "El email no es válido."; Guardar deshabilitado | OK |
| PE-04 | Edición de teléfono | Cambio persistido | Teléfono actualizado; orden por apellido correcto | OK |

### 3.3 Vinculaciones

| ID | Caso | Esperado | Obtenido | Estado |
|---|---|---|---|---|
| VI-01 | Vincular persona como Propietario | Vigente desde hoy | Fila "Propietario, vigente" | OK |
| VI-02 | Repetir la misma vinculación (misma persona, tipo y unidad) | **Rechazo** por superposición | **Se aceptó**: dos filas vigentes idénticas | **DEFECTO (H-1)** |
| VI-03 | Finalizar con fecha anterior al inicio | Bloqueo | El campo tiene `min` = inicio; el navegador rechaza el envío | OK |
| VI-04 | Finalizar con fecha válida | Se registra la fecha de fin | Fin = 2026-11-07 guardado | OK |
| VI-05 | Vincular otra persona como Inquilino | Se crea | Fila "Inquilino, vigente" | OK |

### 3.4 Gastos

| ID | Caso | Esperado | Obtenido | Estado |
|---|---|---|---|---|
| GA-01 | Monto negativo | No se puede guardar | Botón Guardar deshabilitado | OK |
| GA-02 | Alta de gasto ordinario ($10.000) | Se suma al total | Total $612.622,44 pasa a $622.622,44 | OK |
| GA-03 | Alta de gasto extraordinario con decimales ($5.000,50) | Decimales exactos | Total sube exactamente $5.000,50 | OK |
| GA-04 | Edición del monto (a $4.000) | Total recalculado | Total $626.622,44 | OK |
| GA-05 | Eliminación de un gasto | Desaparece y el total baja | 52 gastos pasan a 51; total baja $1,00 | OK |

### 3.5 Liquidaciones

| ID | Caso | Esperado | Obtenido | Estado |
|---|---|---|---|---|
| LI-01 | Generar la liquidación 2026-11 | Total = gastos del mes ($14.000,00), 6 unidades, Abierta | $14.000,00, 6 unidades, Abierta | OK |
| LI-02 | Prorrateo | La suma de los importes es exactamente el total | 5 × $2.333,33 + $2.333,35 = **$14.000,00** | OK |
| LI-03 | Generar un período ya liquidado | Conflicto | "Ya existe una liquidación para este período"; sin duplicado | OK |
| LI-04 | Cerrar la liquidación | Pasa a Cerrada | Estado "Cerrada" | OK |

### 3.6 Pagos

| ID | Caso | Esperado | Obtenido | Estado |
|---|---|---|---|---|
| PA-01 | Pagar con la liquidación abierta | No se permite | Se muestra "Liquidación abierta" y no hay acción de pago | OK |
| PA-02 | Fecha de pago futura | Rechazo | Bloqueada en pantalla (`max` = hoy) y en el backend (`@PastOrPresent`) | OK |
| PA-03 | Registrar pago con liquidación cerrada | Pasa a Pagado, cobrado y pendiente recalculados | Cobrado $2.333,33; pendiente $11.666,67 | OK |
| PA-04 | Revertir el pago | Vuelve a Pendiente | Cobrado $0,00; pendiente $14.000,00 | OK |

### 3.7 Integridad del período cerrado

| ID | Caso | Esperado | Obtenido | Estado |
|---|---|---|---|---|
| CE-01 | Editar un gasto de un mes ya liquidado y cerrado | Rechazo | "La liquidación del período está cerrada"; el monto no cambia | OK |
| CE-02 | Crear un gasto en ese mes | Rechazo | Mismo mensaje; no se crea | OK |
| CE-03 | Eliminar un gasto de ese mes | Rechazo | Mismo mensaje; el gasto sigue | OK |

## 4. Hallazgos

| ID | Severidad | Hallazgo | Detalle | Estado |
|---|---|---|---|---|
| H-1 | **Media** | Vinculaciones duplicadas o superpuestas | `VinculacionUnidadService.crear` no verifica si la persona ya tiene una vinculación vigente del mismo tipo en la unidad. Se pueden cargar dos propietarios idénticos (caso VI-02). Puede duplicar la persona a la que se imputa una unidad. | **Corregido** (ver 4.1) |
| H-2 | Baja | Liquidación en cero | Revisando `LiquidacionService.generar`, no hay validación de "período sin gastos": generaría una liquidación de $0,00 que luego no se puede borrar. **No se ejecutó** para no dejar un registro basura. | **Corregido** (ver 4.1) |
| H-3 | Baja (UX) | Acciones habilitadas que fallan | En gastos de un período cerrado, "Editar" y "Eliminar" aparecen activos y el error llega recién al confirmar. Además, eliminar un gasto, cerrar una liquidación y revertir un pago usan el `confirm()` nativo del navegador. | Abierto |
| H-4 | Media | Pantalla Consorcios mostraba error con lista vacía | Un administrador sin consorcios veía "No se pudieron cargar los datos". Ahora muestra "No tenés consorcios asignados". | **Corregido** |
| H-5 | Info | Sesión previa visible en `/login` | La pantalla de login no redirige al tablero si ya hay una sesión guardada. No afecta la seguridad. | Observación |

### 4.1 Reverificación de las correcciones

| Prueba | Entrada | Resultado |
|---|---|---|
| H-1, vinculación superpuesta sin fecha de fin | Misma persona, tipo y unidad que una vigente | `409` "La persona ya tiene una vinculación de ese tipo en la unidad en esas fechas" |
| H-1, vinculación superpuesta con fecha de fin | Igual, con fin 2026-12-31 | `409`, mismo mensaje |
| H-2, período sin gastos | Liquidación de `2027-03` | `409` "No hay gastos registrados en el período"; no se creó la liquidación |

Después de la prueba, la unidad seguía con 3 vinculaciones (no se agregó ninguna). Cambios en `VinculacionUnidadService.crear` y `LiquidacionService.generar`.

## 5. Alcance no cubierto por el recorrido manual

- **Exportaciones a Excel y CSV:** no se probaron (descargan archivos).
- **Cuentas de propietario e inquilino:** el backend expone `GET /api/mi-cuenta/liquidaciones`, pero la pantalla no existe todavía; no hay nada que recorrer.
- **Reparto proporcional con coeficientes distintos:** las unidades de Demo 2 tienen coeficientes casi iguales, así que el caso LI-02 prueba la suma exacta pero no la proporcionalidad. Este caso ya se cubrió mediante una prueba automática con coeficientes 25 y 75 (sección 7); no se verificó de punta a punta en el navegador.
- **Pruebas de concurrencia, carga y accesibilidad.**
- **Cobertura automatizada:** se agregaron 48 pruebas del backend, detalladas en la sección 7. Complementan el recorrido manual y no equivalen a una prueba integral del sistema.

## 6. Limpieza de los datos de prueba

La prueba dejó en Demo 2 estos registros: 2 personas (DNI 90000001 y 90000002), 3 vinculaciones, 2 gastos de noviembre y la liquidación 2026-11 cerrada (con sus 6 detalles). El sistema no permite borrarlos desde la interfaz. Se eliminan con este script, en el SQL Editor de Supabase:

```sql
begin;
delete from dbo.liquidacionesunidad where liquidacionid in (
  select l.id from dbo.liquidaciones l join dbo.consorcios c on c.id = l.consorcioid
  where c.nombre = 'Consorcio Demo 2' and l.periodo = '2026-11');
delete from dbo.liquidaciones where id in (
  select l.id from dbo.liquidaciones l join dbo.consorcios c on c.id = l.consorcioid
  where c.nombre = 'Consorcio Demo 2' and l.periodo = '2026-11');
delete from dbo.gastos where descripcion like 'QA %';
delete from dbo.vinculacionesunidad where personaid in (select id from dbo.personas where dni in ('90000001','90000002'));
delete from dbo.personas where dni in ('90000001','90000002') and email like 'qa.%@demo.test';
commit;
```

## 7. Pruebas automatizadas del backend

El 8 de noviembre de 2026 se ejecutaron localmente 48 pruebas con JUnit 5, Mockito y Spring Security Test. Los archivos se encuentran en `backend/src/test/java/ar/edu/utn/consorcios/service/`.

| Clase | Casos | Cobertura principal |
|---|---:|---|
| `PagoServiceTest` | 8 | Registro y reversión, pago duplicado, liquidación abierta, registro inexistente y acceso por consorcio |
| `LiquidacionServiceTest` | 7 | Reparto con coeficientes desiguales, ajuste de centavos, período duplicado o sin gastos, unidades y coeficientes inválidos |
| `PersonaServiceTest` | 3 | Alta y normalización, DNI duplicado dentro del consorcio y consulta de persona de otro consorcio |
| `VinculacionUnidadServiceTest` | 8 | Fechas inválidas, pertenencia al consorcio y superposición de períodos, incluidos sus extremos |
| `ConsorcioServiceTest` | 6 | Acceso del superadministrador, administrador asignado y restricciones para otros usuarios |
| `PagoPermisosTest` | 4 | Seguridad de método de Spring sobre la consulta de pagos: permite administradores y rechaza propietarios e inquilinos |
| `AuthServiceTest` | 4 | Login válido, contraseña incorrecta, usuario inexistente y usuario inactivo |
| `GastoServiceTest` | 8 | Alta, modificación y eliminación, períodos cerrados y pertenencia al consorcio |
| **Total** | **48** | |

**Resultado de la ejecución local:** 48 pruebas ejecutadas, 0 fallas, 0 errores y 0 omitidas; `BUILD SUCCESS`.

Se ejecutan desde la carpeta `backend`:

```powershell
.\mvnw.cmd test
```

### 7.1 Entorno y límites

- Los repositorios se simulan con Mockito; las pruebas no conectan con Supabase ni modifican datos compartidos.
- Las pruebas de autenticación usan BCrypt real con credenciales ficticias; la emisión del token JWT está simulada.
- `PagoPermisosTest` utiliza el proxy real de seguridad de métodos de Spring. No ejecuta solicitudes HTTP ni el filtro JWT.
- No se verifican aquí las consultas JPA, las restricciones de PostgreSQL, las transacciones contra una base real, la firma o vencimiento de JWT ni la configuración CORS.
- Los permisos de método se comprueban sobre la consulta de pagos; esto no demuestra la protección de todos los endpoints.
- Siguen pendientes las pruebas integrales entre frontend, API y base de datos, así como concurrencia y carga. Los 31 casos manuales de la sección 2 y los 48 casos automáticos son ejecuciones diferentes y no deben interpretarse como una única cobertura completa.

## 8. Entorno publicado

Actualización: 10 de noviembre de 2026.

| Componente | Servicio | Dirección |
|---|---|---|
| Frontend | Netlify | https://administracion-consorcios.netlify.app/login |
| Backend | Railway | https://sistema-administracion-consorcios-production.up.railway.app |
| Estado del backend | Railway | https://sistema-administracion-consorcios-production.up.railway.app/api/health |
| Base de datos | Supabase | PostgreSQL |

Se confirmó la conexión entre el frontend publicado y el backend.
Previamente se verificó que /api/health devuelve backend y base de
datos en estado UP, y que /api/usuarios sin autenticación responde 401.

Estas comprobaciones no equivalen a repetir el recorrido funcional
completo sobre el entorno publicado.
