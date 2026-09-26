# Primera entrega - Propuesta de proyecto y repositorio

## Trabajo Final Integrador

**Proyecto:** Sistema de Administración de Consorcios  

**Integrantes - Grupo 128**

| Integrante | Legajo |
|---|---:|
| Buchek, Lautaro | 0587 |
| Casalderrey, Hernán | 0265 |
| Castellini, Gonzalo | 4828 |

**Tutora:** María Candela Grosso  

**Fecha de entrega:** 30 de agosto de 2026  

**Repositorio único:** https://github.com/gonzilla2021/sistema-administracion-consorcios

---

## 1. Resumen ejecutivo

El Sistema de Administración de Consorcios será una plataforma web destinada a centralizar la administración de uno o más consorcios. Permitirá gestionar edificios, unidades funcionales, propietarios, inquilinos, gastos, liquidaciones de expensas, un control simple de pagos y accesos diferenciados por rol.

El proyecto surge ante la utilización frecuente de planillas de cálculo, documentos independientes y grupos de mensajería para administrar procesos relacionados entre sí. La dispersión de la información genera trabajo manual, dificulta el seguimiento histórico y limita el acceso de propietarios e inquilinos a datos actualizados.

La solución se desarrollará de manera incremental. La primera versión funcional se concentrará en la gestión administrativa y la liquidación de expensas. Una vez estabilizado ese núcleo, se incorporarán módulos complementarios de acuerdo con el avance del equipo.

## 2. Contexto y problemática

La administración de un consorcio requiere mantener información sobre edificios, unidades, personas vinculadas, gastos comunes, coeficientes de distribución y liquidaciones mensuales. Estos datos forman parte de un mismo proceso, pero muchas veces se conservan en herramientas separadas.

Cuando la gestión se realiza mediante planillas, archivos y conversaciones de mensajería aparecen las siguientes dificultades:

- duplicación o inconsistencia de datos;
- errores durante el cálculo y la distribución de gastos;
- dependencia del administrador para acceder a información básica;
- comunicaciones importantes mezcladas con conversaciones informales;
- dificultad para reconstruir el historial de liquidaciones.

Por lo tanto, el problema no se reduce a la ausencia de una aplicación. El problema central es la falta de una fuente única, estructurada y trazable para administrar la información del consorcio y ofrecer a cada participante el acceso que le corresponde.

## 3. Actores involucrados

### 3.1 Superadministrador

Gestionará las cuentas administrativas y los distintos consorcios incorporados a la plataforma. Podrá crear y administrar consorcios, así como gestionar las cuentas de los administradores. Accederá únicamente a la información administrativa de los consorcios bajo su gestión.

### 3.2 Administrador de consorcio

Registrará unidades, personas vinculadas, gastos y liquidaciones. También podrá marcar una liquidación como pendiente o pagada mediante un control simple. Necesitará reducir tareas repetitivas, consultar información histórica y obtener una visión actualizada de la situación del edificio. Podrá operar exclusivamente sobre el consorcio que tenga asignado.

### 3.3 Propietario

Podrá consultar los datos de las unidades de las que sea titular y las liquidaciones que les correspondan. No podrá modificar gastos, liquidaciones ni información de otras unidades o consorcios.

### 3.4 Inquilino

Podrá consultar la información habilitada de la unidad que ocupa y las liquidaciones que el administrador defina como visibles para su rol. No accederá a información patrimonial, estados de cuenta ni datos de otras unidades.

### 3.5 Matriz preliminar de permisos

| Acción o información | Superadministrador | Administrador de consorcio | Propietario | Inquilino |
|---|:---:|:---:|:---:|:---:|
| Crear y administrar consorcios | Sí | No | No | No |
| Gestionar unidades y personas vinculadas | No | Sí, en su consorcio | No | No |
| Registrar gastos, generar liquidaciones y marcar pagos | No | Sí, en su consorcio | No | No |
| Consultar una liquidación | No | Sí, en su consorcio | Sí, en sus unidades | Sí, si está habilitada para su unidad |
| Consultar datos de unidades o consorcios no autorizados | No | No | No | No |


## 4. Flujo actual resumido

1. El administrador recibe facturas y comprobantes de distintos proveedores.
2. Registra o copia los gastos en una planilla.
3. Calcula el importe correspondiente a cada unidad utilizando los coeficientes de propiedad.
4. Comunica las liquidaciones por correo electrónico o mensajería.
5. Ante una consulta, busca información en archivos y conversaciones previas.

Este proceso depende en gran medida de tareas manuales y no garantiza que todos los participantes consulten la misma versión de la información.

## 5. Propuesta de solución

Se propone desarrollar una aplicación web responsive que centralice la información operativa de uno o más consorcios. El sistema aplicará permisos según el rol y la relación de cada usuario con el consorcio o la unidad funcional.

La plataforma permitirá:

- registrar consorcios y unidades funcionales;
- asociar propietarios e inquilinos mediante períodos de vigencia;
- registrar y clasificar gastos ordinarios y extraordinarios;
- calcular liquidaciones mensuales mediante los coeficientes de propiedad;
- marcar una liquidación como pendiente o pagada mediante un control simple a cargo del administrador;
- restringir las operaciones y los datos visibles según el rol;
- permitir que cada usuario consulte la liquidación habilitada para su unidad.

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
- conservar liquidaciones mensuales consultables;
- mejorar la transparencia de las liquidaciones;
- permitir que cada usuario consulte información actualizada según sus permisos.

El diferencial académico y técnico no estará dado solamente por digitalizar formularios. El sistema incorporará reglas de negocio, relaciones temporales, cálculos monetarios, aislamiento de datos por consorcio y autorización contextual.

## 8. Alcance

### 8.1 Alcance comprometido para el MVP

El producto mínimo viable incluirá únicamente las funcionalidades necesarias para organizar la información del consorcio y generar una liquidación de expensas:

- registro e inicio de sesión;
- autorización según los roles definidos;
- gestión de consorcios, unidades funcionales y coeficientes de propiedad;
- asociación de propietarios e inquilinos a las unidades;
- registro y clasificación de gastos ordinarios y extraordinarios;
- prorrateo de gastos y generación de liquidaciones mensuales;
- marcación simple de cada liquidación como pendiente o pagada, actualizada por el administrador;
- consulta de la liquidación correspondiente a cada unidad según los permisos del usuario.

El MVP se considerará completo cuando un administrador pueda registrar gastos de un período, generar la liquidación para las unidades de su consorcio, marcar cada liquidación como pendiente o pagada y permitir que los usuarios autorizados consulten su liquidación.

### 8.2 Funcionalidades planificadas para etapas posteriores

Una vez completado el alcance comprometido se evaluará incorporar:

- consulta de un estado de cuenta detallado e historial de pagos;
- historial ampliado de liquidaciones;
- registro y seguimiento de reclamos;
- gestión de reservas de amenities, como el SUM, la pileta o el quincho;
- publicación de actas y avisos;
- notificaciones dentro de la aplicación o por correo electrónico;
- generación de liquidaciones descargables en PDF;
- reportes de deuda y evolución de gastos.

Estas funcionalidades se planifican como etapas posteriores porque amplían el número de módulos y reglas de negocio. Su implementación dependerá de que el MVP esté completo, probado y aprobado.

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
- Una persona podrá ser propietaria de una unidad e inquilina de otra. Para los usuarios propietarios e inquilinos, los permisos sobre cada unidad se determinarán por el tipo de vinculación vigente registrado en VinculacionesUnidad. El rol de la cuenta identificará su perfil general de acceso.
- Una liquidación cerrada no permitirá modificar sus importes ni su distribución entre unidades. El administrador podrá actualizar el estado y la fecha de pago. Toda corrección posterior de los importes deberá quedar registrada con su fecha, motivo y responsable.
- Los importes se calcularán con precisión decimal y una regla de redondeo uniforme.
- Un usuario solamente podrá consultar u operar sobre los consorcios y unidades para los cuales tenga autorización.
- Los gastos extraordinarios deberán distinguirse de los ordinarios.
- Un consorcio puede crearse antes de cargar sus unidades. Una liquidación abierta puede estar en preparación, pero para cerrarse deberá contener los importes correspondientes a todas las unidades del consorcio.


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
- Supabase u otro servicio PostgreSQL administrado para la base de datos, sujeto a la disponibilidad de planes gratuitos.
- Postman para documentar y probar la API.
- Docker como apoyo para reproducir el entorno local si el cronograma lo permite.

**Justificación:** los servicios PaaS reducen el trabajo de administración de infraestructura y permiten cumplir el requisito de publicación online. La selección definitiva se realizará verificando límites, disponibilidad y compatibilidad antes de la etapa de despliegue.

## 11. Arquitectura inicial

**Actualización de la segunda entrega:** la selección vigente de servicios y la arquitectura se documentan en [Arquitectura del proyecto](docs/arquitectura.md).

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

