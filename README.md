# Sistema de Administración de Consorcios

**Trabajo Final Integrador - Grupo 128**

| Integrante | Legajo |
|---|---:|
| Buchek, Lautaro | 0587 |
| Casalderrey, Hernán | 0265 |
| Castellini, Gonzalo | 4828 |

**Tutora:** María Candela Grosso   
**Repositorio único:** https://github.com/gonzilla2021/sistema-administracion-consorcios

Sistema web para centralizar la administración de consorcios, unidades funcionales, personas, gastos y liquidaciones de expensas, con acceso diferenciado según el rol de cada usuario y un control simple de pagos.

## Estado del proyecto

Proyecto en etapa de propuesta y planificación correspondiente a la primera entrega del Trabajo Final Integrador.
Proyecto en etapa de diseño y documentación correspondiente a la segunda entrega del Trabajo Final Integrador.

## Problema que aborda

En muchos consorcios pequeños y medianos, la información se distribuye entre planillas de cálculo, documentos y grupos de mensajería. Esto dificulta el seguimiento de gastos y liquidaciones, aumenta el trabajo manual y reduce la transparencia para propietarios e inquilinos.

El Sistema de Administración de Consorcios propone una fuente única de información, con accesos diferenciados según el rol de cada usuario.

## Alcance comprometido para el MVP

- Registro e inicio de sesión.
- Gestión de usuarios y permisos por rol.
- Gestión de consorcios, unidades funcionales y coeficientes de propiedad.
- Asociación de propietarios e inquilinos a una unidad funcional.
- Registro y clasificación de gastos ordinarios y extraordinarios.
- Prorrateo de gastos y generación de liquidaciones mensuales.
- Marcación simple del pago de cada liquidación como pendiente o pagado, actualizada por el administrador.
- Consulta de la liquidación correspondiente a cada unidad según el rol del usuario.

## Funcionalidades planificadas para etapas posteriores

- Consulta de un estado de cuenta detallado e historial de pagos.
- Historial ampliado de liquidaciones.
- Registro y seguimiento de reclamos.
- Gestión de reservas de amenities, como el SUM, la pileta o el quincho.
- Publicación de actas y avisos.
- Notificaciones y reportes avanzados.

Estas funcionalidades no forman parte del alcance comprometido del MVP. Se evaluarán una vez que el núcleo de gestión y liquidación de expensas se encuentre terminado y probado.

## Tecnologías propuestas

- Frontend: React, TypeScript, Tailwind y Vite.
- Backend: Java y Spring Boot.
- Base de datos: PostgreSQL.
- Seguridad: Spring Security y JWT.
- Despliegue: Vercel o Netlify para el frontend y un servicio PaaS compatible con Spring Boot para el backend (Railway). PostgreSQL se alojará en un servicio administrado con plan gratuito (SupaBase).
- Versionado: Git y GitHub.


## Documentación

- [Primera entrega: propuesta y alcance](Entrega_1.md).
- [Arquitectura del proyecto](docs/arquitectura.md).
- [Listado de módulos y prioridades](docs/modulos.md).
- [Diagrama entidad-relación](database/diagrama-entidad-relacion.md).
- [Esquema de base de datos](database/schema-consorcios.sql).
- [Diagrama de clases del modelo de dominio](database/uml-modelo-dominio.md).

## Roles y acceso a la información

| Rol | Acciones principales | Información a la que accede |
|---|---|---|
| Superadministrador | Gestiona las cuentas administrativas y crea o administra consorcios. | Información general y administrativa de los consorcios que gestiona. |
| Administrador de consorcio | Gestiona unidades, personas vinculadas, gastos y liquidaciones de su consorcio; además marca los pagos como pendientes o pagados. | Información completa del consorcio asignado, sin acceso a otros consorcios. |
| Propietario | Consulta la información y las liquidaciones de sus unidades. | Datos de sus unidades y sus liquidaciones correspondientes. |
| Inquilino | Consulta la información autorizada de la unidad que ocupa. | Datos básicos y liquidaciones habilitadas de su unidad; no accede a información patrimonial ni a otros inmuebles. |

## Estructura prevista

```text
/
|- backend/    API y lógica de negocio
|- frontend/   Aplicación web
|- database/   Scripts, migraciones y documentación de datos
|- docs/       Informes y entregas académicas
`- README.md   Presentación general del proyecto
```

## Ejecución local

Las instrucciones de instalación y ejecución se incorporarán cuando se creen las aplicaciones de frontend y backend.

## Repositorio

URL pública: https://github.com/gonzilla2021/sistema-administracion-consorcios
