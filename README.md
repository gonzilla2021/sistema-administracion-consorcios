## Trabajo Final Integrador

**Proyecto:** Sistema de Administración de Consorcios  

****Integrantes: Grupo 128**** 
  Buchek, Lautaro (Legajo 0587)
  Casalderrey, Hernán (Legajo 0265)
  Castellini, Gonzalo (Legajo 4828)  

**Tutora:** María Candela Grosso  

**Fecha de entrega:** 30 de agosto de 2026  

**Repositorio único:** https://github.com/gonzilla2021/sistema-administracion-consorcios.git

# Sistema de Administración de Consorcios

Sistema web para centralizar la administración de consorcios, unidades funcionales, personas, gastos, expensas, pagos y reclamos.

## Estado del proyecto

Proyecto en etapa de propuesta y planificación correspondiente a la primera entrega del Trabajo Final Integrador.

## Problema que aborda

En muchos consorcios pequeños y medianos, la información se distribuye entre planillas de cálculo, documentos y grupos de mensajería. Esto dificulta el seguimiento de gastos, liquidaciones, pagos y reclamos, aumenta el trabajo manual y reduce la transparencia para propietarios e inquilinos.

El Sistema de Administración de Consorcios propone una fuente única de información, con accesos diferenciados según el rol de cada usuario.

## Alcance inicial

- Autenticación y autorización por roles.
- Gestión de consorcios y unidades funcionales.
- Asociación de propietarios e inquilinos.
- Registro y clasificación de gastos.
- Liquidación de expensas por coeficiente de propiedad.
- Gestion de turnos de Amenities.


Los pagos, las actas, las notificaciones y los reportes avanzados se desarrollarán en etapas posteriores, de acuerdo con el avance del producto.

## Tecnologías propuestas

- Frontend: React, TypeScript, Tailwind y Vite.
- Backend: Java y Spring Boot.
- Base de datos: PostgreSQL.
- Seguridad: Spring Security y JWT.
- Despliegue: Vercel o Netlify para el frontend y un servicio PaaS compatible con Spring Boot para el backend (Railway). PostgreSQL se alojará en un servicio administrado con plan gratuito (SupaBase).
- Versionado: Git y GitHub.


## Documentación

La propuesta completa de la primera entrega se encuentra en [entrega-1.md](/Entrega_1.md).

## Alcance comprometido para el MVP

El producto mínimo viable incluirá:

1. Registro e inicio de sesión.
2. Gestión de usuarios y permisos por rol.
3. Gestión de consorcios.
4. Gestión de unidades funcionales y coeficientes de propiedad.
5. Asociación de propietarios e inquilinos.
6. Registro de gastos ordinarios y extraordinarios.
7. Generación de liquidaciones mensuales, prorrateo de gastos según el coeficiente de cada unidad.
8. Registro de Amenities y salon de usos multiples.
--
### Mejoras
9. Registro de pagos y consulta del estado de cuenta.
10. Historial básico de liquidaciones.
11. Registro y seguimiento de reclamos.

## Actores involucrados

- Superadministrador
- Administrador de consorcio
- Propietario / Inquilino

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

URL pública: https://github.com/gonzilla2021/sistema-administracion-consorcios.git
