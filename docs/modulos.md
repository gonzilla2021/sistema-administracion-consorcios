# Listado de módulos funcionales

**Proyecto:** Sistema de Administración de Consorcios  
**Trabajo Final Integrador — Grupo 128**  
**Entrega:** Segunda entrega

## 1. Prioridades

- **Alta:** módulos necesarios para la seguridad, administración y liquidación de expensas.
- **Media:** módulos de consulta que se desarrollarán después del núcleo administrativo. También son obligatorios para completar el MVP.
- **Baja:** funcionalidades futuras, sujetas al avance del proyecto.

## 2. Módulos del producto mínimo viable (MVP)

| Módulo | Descripción | Prioridad |
|---|---|---|
| Autenticación | Registro, inicio y cierre de sesión de los usuarios, con protección de sus credenciales. | Alta |
| Gestión de usuarios y permisos | Gestión de cuentas y control de acceso según los roles de superadministrador, administrador de consorcio, propietario e inquilino. Cada usuario accede únicamente a la información y las operaciones autorizadas. | Alta |
| Gestión de consorcios | Alta, consulta y actualización de los datos de los consorcios y asignación de sus administradores. | Alta |
| Gestión de unidades funcionales | Registro y actualización de las unidades de cada consorcio, sus identificadores y los coeficientes de propiedad utilizados para distribuir los gastos. | Alta |
| Gestión de personas y vinculaciones | Registro de personas y asociación como propietarias o inquilinas de una o más unidades, indicando las fechas de inicio y fin de cada vinculación. | Alta |
| Gestión de gastos | Registro de gastos ordinarios y extraordinarios, indicando descripción, importe, fecha y referencia al comprobante cuando corresponda. | Alta |
| Liquidación de expensas | Generación de liquidaciones mensuales y cálculo del importe correspondiente a cada unidad según sus coeficientes. Incluye el cierre de liquidaciones para impedir cambios directos en los importes y su distribución, manteniendo disponible la actualización del estado y la fecha de pago.| Alta |
| Control simple de pagos | Marcación de la liquidación de cada unidad como pendiente o pagada y registro de la fecha de pago, a cargo del administrador. No incluye cobros electrónicos ni pagos parciales. | Alta |
| Consulta de unidades y liquidaciones | Consulta de los datos y las liquidaciones de las unidades de cada propietario o inquilino, según sus vinculaciones y permisos. | Media |

## 3. Módulos y ampliaciones para etapas posteriores

Estas funcionalidades no forman parte del alcance comprometido del MVP. Su implementación se evaluará una vez que el núcleo del sistema esté terminado y probado.

| Módulo o ampliación | Descripción | Prioridad |
|---|---|---|
| Estado de cuenta e historial ampliado | Consulta de un estado de cuenta detallado y ampliación del historial de pagos y liquidaciones. | Baja |
| Gestión de reclamos | Registro de reclamos y seguimiento de su estado. | Baja |
| Reservas de amenities | Consulta de disponibilidad y gestión de reservas de espacios comunes, como SUM, pileta o quincho. | Baja |
| Actas y avisos | Publicación y consulta de actas y comunicaciones del consorcio. | Baja |
| Notificaciones | Envío de novedades dentro de la aplicación o por correo electrónico. | Baja |
| Liquidaciones en PDF | Descarga de liquidaciones en formato PDF. | Baja |
| Reportes | Consulta de reportes de deuda y evolución de gastos. | Baja |

## 4. Criterio de finalización del MVP

El MVP estará completo cuando el administrador pueda gestionar las unidades y personas de su consorcio, registrar gastos, generar una liquidación mensual, marcar el pago de cada unidad y permitir que los usuarios autorizados consulten la información correspondiente.
