# Primera entrega - Propuesta de proyecto y repositorio

## Trabajo Final Integrador

**Proyecto:** Sistema de Administración de Consorcios  

****Integrantes: Grupo 128**** 
  Buchek, Lautaro (Legajo 0587)
  Casalderrey, Hernán (Legajo 0265)
  Castellini, Gonzalo (Legajo 4828)  

**Tutora:** María Candela Grosso  

**Fecha de entrega:** 30 de agosto de 2026  

**Repositorio único:** https://github.com/gonzilla2021/sistema-administracion-consorcios.git

---

## 1. Resumen ejecutivo

El Sistema de Administración de Consorcios será una plataforma web destinada a centralizar la administración de uno o más consorcios. Permitirá gestionar edificios, unidades funcionales, propietarios, inquilinos, gastos, liquidaciones de expensas y accesos diferenciados por rol.

El proyecto surge ante la utilización frecuente de planillas de cálculo, documentos independientes y grupos de mensajería para administrar procesos relacionados entre sí. La dispersión de la información genera trabajo manual, dificulta el seguimiento histórico y limita el acceso de propietarios e inquilinos a datos actualizados.

La solución se desarrollará de manera incremental. La primera versión funcional se concentrará en la gestión administrativa y la liquidación de expensas. Una vez estabilizado ese núcleo, se incorporarán módulos complementarios de acuerdo con el avance del equipo.

## 2. Contexto y problemática

La administración de un consorcio requiere mantener información sobre edificios, unidades, personas vinculadas, gastos comunes, coeficientes de distribución, liquidaciones mensuales, pagos y reclamos. Estos datos forman parte de un mismo proceso, pero muchas veces se conservan en herramientas separadas.

Cuando la gestión se realiza mediante planillas, archivos y conversaciones de mensajería aparecen las siguientes dificultades:

- duplicación o inconsistencia de datos;
- errores durante el cálculo y la distribución de gastos;
- comunicaciones importantes mezcladas con conversaciones informales.
- Reservas de Amenities y espacios de usos multiples.
- dependencia del administrador para acceder a información básica;
--
### Mejoras
- dificultad para reconstruir el historial de liquidaciones y pagos;
- reclamos sin un estado o responsable claramente identificable;

Por lo tanto, el problema no se reduce a la ausencia de una aplicación. El problema central es la falta de una fuente única, estructurada y trazable para administrar la información del consorcio y ofrecer a cada participante el acceso que le corresponde.

## 3. Actores involucrados

### 3.1 Superadministrador

Gestionará las cuentas administrativas y los distintos consorcios incorporados a la plataforma. Su necesidad principal será mantener separados y organizados los datos de cada consorcio.

### 3.2 Administrador de consorcio

Registrará unidades, personas, gastos, liquidaciones, pagos y reclamos. Necesitará reducir tareas repetitivas, consultar información histórica y obtener una visión actualizada de la situación del edificio.

### 3.3 Propietario / Inquilino

Podrá consultar la información de su unidad, sus liquidaciones, pagos y comunicaciones habilitadas. Necesitará acceder a información clara sin depender permanentemente del administrador.


## 4. Flujo actual resumido

1. El administrador recibe facturas y comprobantes de distintos proveedores.
2. Registra o copia los gastos en una planilla.
3. Calcula el importe correspondiente a cada unidad utilizando los coeficientes de propiedad.
4. Comunica las liquidaciones por correo electrónico o mensajería.
--
### Mejoras
5. Registra los pagos en otra planilla o modifica manualmente el estado de cada unidad.
6. Recibe reclamos por diferentes canales y realiza su seguimiento de manera informal.
7. Ante una consulta, busca información en archivos y conversaciones previas.

Este proceso depende en gran medida de tareas manuales y no garantiza que todos los participantes consulten la misma versión de la información.

## 5. Propuesta de solución

Se propone desarrollar una aplicación web responsive que centralice la información operativa de uno o más consorcios. El sistema aplicará permisos según el rol y la relación de cada usuario con el consorcio o la unidad funcional.

La plataforma permitirá:

- registrar consorcios y unidades funcionales;
- asociar propietarios e inquilinos mediante períodos de vigencia;
- registrar y clasificar gastos ordinarios y extraordinarios;
- restringir las operaciones y los datos visibles según el rol.
- calcular liquidaciones mensuales mediante los coeficientes de propiedad;
- Reserva de Amenities y salon de usos multiples.
--
### Mejoras
- registrar pagos y mantener el estado de cuenta de cada unidad;
- consultar el historial de liquidaciones;
- crear y seguir reclamos;

Con esto se busca transformar un conjunto de tareas aisladas en un flujo digital integrado y trazable.

## 6. Objetivos

### 6.1 Objetivo general

Desarrollar y desplegar una plataforma web que centralice los principales procesos de administración de consorcios y facilite el acceso seguro a la información por parte de administradores, propietarios e inquilinos.

### 6.2 Objetivos específicos

- Documentar la instalación, arquitectura, decisiones técnicas y uso de la aplicación (Objetivo Técnico).
- Modelar consorcios, unidades funcionales y relaciones temporales con propietarios e inquilinos.
- Implementar autenticación y autorización para los distintos roles del sistema.
- Automatizar el prorrateo de gastos según los coeficientes definidos para cada unidad.
- Evitar el acceso cruzado a información de otros consorcios o unidades.


## 7. Propuesta de valor

La solución aportará valor porque permitirá:

- reducir el tiempo dedicado a consolidar información dispersa;
- disminuir errores de cálculo y transcripción;
- conservar un historial consultable de operaciones;
- mejorar la transparencia de las liquidaciones y los estados de cuenta;
- permitir que cada usuario consulte información actualizada según sus permisos.

El diferencial académico y técnico no estará dado solamente por digitalizar formularios. El sistema incorporará reglas de negocio, relaciones temporales, cálculos monetarios, aislamiento de datos por consorcio y autorización contextual.

## 8. Alcance

### 8.1 Alcance comprometido para el MVP

El producto mínimo viable incluirá:

- Autenticación y autorización por roles.
- Gestión de consorcios y unidades funcionales.
- Asociación de propietarios e inquilinos.
- Registro y clasificación de gastos.
- Liquidación de expensas por coeficiente de propiedad.
- Gestion de turnos de Amenities.

Los pagos, las actas, las notificaciones y los reportes avanzados se desarrollarán en etapas posteriores, de acuerdo con el avance del producto.

### 8.2 Ampliaciones

Una vez completado el alcance comprometido se evaluará incorporar:

- publicación de actas y avisos;
- notificaciones dentro de la aplicación o por correo electrónico;
- generación de liquidaciones descargables en PDF;
- reportes de deuda y evolución de gastos.

### 8.3 Fuera de alcance

La primera versión no incluirá:

- cobros electrónicos o integración con bancos y billeteras virtuales;
- facturación electrónica ante ARCA;
- aplicación móvil nativa;
- gestión contable o impositiva completa;
- reconocimiento automático de facturas;
- integración con dispositivos de control de acceso;

Estas exclusiones evitan dependencias externas y mantienen el proyecto dentro de los plazos académicos.

## 9. Reglas de negocio principales

- Cada unidad funcional pertenecerá a un único consorcio.
- La suma de los coeficientes de las unidades de un consorcio deberá respetar el total configurado para ese consorcio.
- Una persona podrá estar vinculada con diferentes unidades y roles.
- Una liquidación cerrada no podrá modificarse directamente; toda corrección deberá quedar registrada.
- Los importes se calcularán con precisión decimal y una regla de redondeo uniforme.
- Un usuario solamente podrá consultar u operar sobre los consorcios y unidades para los cuales tenga autorización.
- Los gastos extraordinarios deberán distinguirse de los ordinarios.
- Los usuarios podran reserva amenities en su unidad registrada.


## 10. Stack tecnológico

### 10.1 Frontend

- React con TypeScript.
- Vite como herramienta de construcción.
- React Router para navegación.
- TanStack Query para comunicación y caché de datos del servidor.
- React Hook Form para formularios.
- Tailwind CSS para estilos.

**Justificación:** React y TypeScript permiten construir una interfaz web modular, tipada y mantenible. Vite reduce la complejidad de configuración. Las bibliotecas elegidas cubren navegación, formularios y acceso a datos sin requerir una arquitectura innecesariamente compleja.

### 10.2 Backend

- Java con Spring Boot.
- Spring Web para la API REST.
- Spring Data JPA/Hibernate para persistencia.
- Spring Security y JWT para autenticación y autorización.
- Bean Validation para validaciones.

**Justificación:** Spring Boot es adecuado para aplicaciones transaccionales con reglas de negocio y permisos complejos. Su ecosistema permite organizar el backend en capas, manejar transacciones, integrar PostgreSQL y aplicar seguridad de manera centralizada.

### 10.3 Base de datos

- PostgreSQL.

**Justificación:** el dominio presenta entidades con relaciones claras y requiere integridad referencial, transacciones y precisión en datos monetarios. Por eso una base relacional resulta más apropiada que una base documental.

### 10.4 Despliegue y herramientas

- GitHub como repositorio único y sistema de control de versiones.
- Vercel para el frontend.
- Render, Railway u otro PaaS compatible con Spring Boot para el backend, sujeto a la disponibilidad de planes gratuitos al momento del despliegue.
- Neon, Supabase u otro servicio PostgreSQL administrado para la base de datos, sujeto a la disponibilidad de planes gratuitos.
- Postman para documentar y probar la API.
- Docker como apoyo para reproducir el entorno local si el cronograma lo permite.

**Justificación:** los servicios PaaS reducen el trabajo de administración de infraestructura y permiten cumplir el requisito de publicación online. La selección definitiva se realizará verificando límites, disponibilidad y compatibilidad antes de la etapa de despliegue.

## 11. Arquitectura inicial

La aplicación tendrá una arquitectura cliente-servidor:

```text
Usuario
   |
   v
Frontend React
   |
   | HTTPS / API REST
   v
Backend Spring Boot
   |
   | JPA / transacciones
   v
PostgreSQL
```

El backend se organizará inicialmente en capas:

- controladores para exponer la API;
- servicios para las reglas de negocio;
- repositorios para el acceso a datos;
- entidades y objetos de transferencia para representar la información;
- componentes de seguridad para autenticación y autorización.

Se utilizará una aplicación modular única, porque es suficiente para la escala prevista y disminuye el costo operativo frente a una arquitectura de microservicios.



