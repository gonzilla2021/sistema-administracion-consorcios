# 🏢 Sistema de Administración de Consorcios

### Trabajo Final Integrador — Proyecto Final

**Tecnicatura Universitaria en Programación — UTN a Distancia**

**Grupo 128 — Universidad Tecnológica Nacional (UTN)**

Sistema web desarrollado para centralizar y optimizar la administración de consorcios, facilitando la gestión de unidades funcionales, personas, gastos, liquidaciones de expensas y pagos mediante una plataforma con acceso diferenciado según el rol de cada usuario.

---

## 📋 1. Información general del proyecto

| Integrante | Legajo |
|---|---|
| Buchek, Lautaro | 0587 |
| Casalderrey, Hernán | 0265 |
| Castellini, Gonzalo | 4828 |

**Tutora:** María Candela Grosso

**Proyecto Publicado**
https://administracion-consorcios.netlify.app

**Estado:** Versión funcional con frontend y backend desplegados.

**Repositorio oficial:**  
https://github.com/gonzilla2021/sistema-administracion-consorcios

---

## 🎯 2. Problemática y solución propuesta

### Problemática

La administración de consorcios pequeños y medianos suele realizarse mediante herramientas dispersas, como planillas de Excel, documentos digitales y comprobantes enviados por aplicaciones de mensajería.

Esta modalidad presenta diversas dificultades:

- Información descentralizada y desorganizada.
- Riesgo de pérdida, duplicación o alteración de datos.
- Dificultades para realizar el seguimiento de gastos y liquidaciones.
- Mayor cantidad de tareas administrativas manuales.
- Acceso limitado a la información por parte de propietarios e inquilinos.
- Falta de trazabilidad y transparencia en la gestión.

### Solución

El Sistema de Administración de Consorcios propone centralizar la información en una aplicación web, utilizando una base de datos relacional y estableciendo diferentes permisos según el rol del usuario.

La plataforma permite administrar consorcios, registrar gastos, generar liquidaciones mensuales y consultar la información correspondiente a cada unidad funcional.

La digitalización de estos procesos busca mejorar la organización, reducir errores administrativos, preservar la integridad de los datos y brindar mayor transparencia a la gestión.

---

## 🌐 3. Sistema desplegado

El proyecto cuenta con una arquitectura de frontend, backend y base de datos desplegados en servicios independientes.

| Componente | Tecnología / Servicio | Acceso |
|---|---|---|
| Frontend | React + Netlify | [Abrir aplicación](https://administracion-consorcios.netlify.app/login) |
| Backend | Spring Boot + Railway | [Abrir API](https://sistema-administracion-consorcios-production.up.railway.app) |
| Base de datos | PostgreSQL + Supabase | Acceso privado |
| Estado del servicio | Endpoint de verificación | [Health Check](https://sistema-administracion-consorcios-production.up.railway.app/api/health) |

### Verificación del backend

El endpoint `/api/health` permite comprobar el estado del servicio y de su conexión con la base de datos.

Cuando ambos componentes funcionan correctamente, devuelve una respuesta similar a:

```json
{
  "backend": "UP",
  "baseDeDatos": "UP"
}
```

### Acceso a la aplicación

La aplicación requiere autenticación.

El acceso a las funcionalidades depende del rol y de las vinculaciones de cada usuario con los consorcios y las unidades funcionales.

Las credenciales de evaluación o demostración deben proporcionarse de forma segura y no se publican contraseñas en este repositorio.

---

## ⚙️ 4. Funcionalidades del sistema

### 4.1. Autenticación y gestión de usuarios

- Inicio y cierre de sesión.
- Autenticación mediante JWT.
- Protección de contraseñas.
- Gestión de usuarios.
- Asignación de roles.
- Control de acceso a operaciones y recursos.

### 4.2. Gestión de consorcios

- Alta y administración de consorcios.
- Consulta y actualización de sus datos.
- Asignación de administradores.
- Vinculación de administradores con uno o varios consorcios.
- Separación de la información entre consorcios.

### 4.3. Gestión de unidades funcionales

- Registro de unidades pertenecientes a cada consorcio.
- Actualización de sus datos.
- Administración de coeficientes de propiedad.
- Identificación y consulta de unidades funcionales.
- Asociación de personas a las unidades.

Los coeficientes se utilizan para determinar la participación de cada unidad en la distribución de gastos.

### 4.4. Gestión de personas y vinculaciones

- Registro de personas.
- Asociación de propietarios e inquilinos a unidades funcionales.
- Administración de fechas de inicio y finalización de las vinculaciones.
- Consulta de información según los permisos del usuario.

### 4.5. Gestión de gastos

- Registro de gastos del consorcio.
- Clasificación en ordinarios y extraordinarios.
- Identificación mediante descripción, importe y fecha.
- Imputación de gastos al período correspondiente.
- Registro de referencias a comprobantes cuando corresponda.
- Consulta de gastos administrativos.

La centralización de los gastos contribuye a mantener información organizada y disponible para su utilización en las liquidaciones.

### 4.6. Liquidación de expensas

El sistema permite generar liquidaciones mensuales a partir de los gastos registrados para un período determinado.

El proceso contempla:

1. Identificación del consorcio y del período.
2. Recuperación de los gastos correspondientes.
3. Determinación de los coeficientes de las unidades funcionales.
4. Cálculo y distribución de los importes.
5. Generación de la liquidación mensual.
6. Cierre de la liquidación.

**Regla de negocio: cierre de liquidaciones**

Una vez cerrada una liquidación, no se permite modificar directamente los importes y su distribución.

Esta restricción busca preservar la consistencia de la información liquidada y evitar modificaciones posteriores que alteren los resultados del período.

El registro posterior del estado y la fecha de pago se mantiene disponible según los permisos correspondientes.

### 4.7. Control de pagos

- Consulta del estado de pago de las liquidaciones.
- Identificación de liquidaciones pendientes y pagadas.
- Actualización del estado por parte del administrador.
- Registro de la fecha de pago.

El sistema realiza un control administrativo de pagos. No procesa transferencias bancarias ni cobros electrónicos y no contempla pagos parciales dentro de este módulo.

### 4.8. Consulta de información

Los propietarios e inquilinos pueden consultar la información autorizada de las unidades con las que se encuentran vinculados.

El acceso se controla desde el backend para impedir la consulta o modificación de información perteneciente a otros usuarios o consorcios sin autorización.

### 4.9. Funcionalidades complementarias

El proyecto incluye además el módulo de **Amenities**: el administrador configura espacios comunes (SUM, parrilla, gimnasio, etc.), sus turnos y bloqueos por fecha; propietarios e inquilinos consultan disponibilidad, reservan turnos y cancelan sus propias reservas.

Dentro del alcance evolutivo del proyecto se contemplan:

- Registro y seguimiento de reclamos.
- Historial ampliado de pagos y liquidaciones.
- Actas y avisos del consorcio.
- Notificaciones.
- Reportes administrativos.

Estas funcionalidades se consideran ampliaciones del núcleo de gestión de expensas. Su disponibilidad en la versión final debe determinarse conforme al estado real de implementación y pruebas.

---

## 👥 5. Roles y permisos

El sistema utiliza distintos perfiles para controlar las operaciones disponibles y la información a la que puede acceder cada usuario.

| Rol | Responsabilidades y permisos |
|---|---|
| Superadministrador | Gestión general de usuarios administrativos y consorcios. |
| Administrador | Administración de los consorcios asignados, unidades, personas, gastos, liquidaciones y pagos. |
| Propietario | Consulta de información y liquidaciones correspondientes a sus unidades. |
| Inquilino | Consulta de la información habilitada para la unidad que ocupa. |

## 👥 Accesos Creados

Para facilitar la evaluación del sistema, se dispone de usuarios de demostración con diferentes roles y permisos.

**🌐 Acceso al sistema:**  
https://administracion-consorcios.netlify.app/login

| Usuario (email) | Rol | Acceso |
|---|---|---|
| `superadmin@demo.test` | Superadministrador | Administración general |
| `admin1@demo.test` | Administrador | Consorcios Demo 1 y 2 |
| `admin3@demo.test` | Administrador | Sin consorcios asignados |
| `florencia.sosa3@demo.test` | Propietario | Unidad 1A |
| `maria.perez4@demo.test` | Inquilino | Unidad 2B |

🧪 Pruebas sugeridas

Superadministrador: gestionar consorcios y administradores.

Administrador: registrar gastos, generar liquidaciones y actualizar estados de pago.

Administrador sin consorcios: comprobar el comportamiento del sistema cuando no tiene consorcios asignados.

Propietario e inquilino: verificar el acceso limitado a la información de sus unidades, utilizando las cuentas de demostración correspondientes.


### 🔑 Credenciales de acceso

Las contraseñas de los usuarios de prueba se proporcionan por un canal privado, para evitar publicar credenciales en el repositorio.

## 🛠️ 6. Tecnologías utilizadas

### Frontend

- **React:** construcción de la interfaz de usuario.
- **TypeScript:** desarrollo con tipado estático.
- **Vite:** entorno de desarrollo y compilación.
- **Tailwind CSS:** estilos y diseño de la interfaz.
- **Netlify:** despliegue del frontend.

### Backend

- **Java 21:** lenguaje de programación.
- **Spring Boot 3:** framework principal.
- **Spring Security:** autenticación y autorización.
- **JWT:** gestión de sesiones mediante tokens.
- **Spring Data JPA / Hibernate:** persistencia de datos.
- **Maven:** administración de dependencias y construcción del proyecto.
- **Railway:** despliegue del backend.

### Base de datos

- **PostgreSQL:** sistema de gestión de base de datos relacional.
- **Supabase:** alojamiento de PostgreSQL.
- **SQL:** definición y administración del esquema de datos.

### Herramientas de desarrollo

- Git y GitHub.
- Visual Studio Code y otras herramientas de desarrollo compatibles.
- Maven Wrapper.
- Herramientas de consulta y administración de PostgreSQL.

---

## 🏗️ 7. Arquitectura del sistema

El proyecto utiliza una arquitectura cliente-servidor.

El frontend presenta la interfaz y consume los servicios del backend mediante solicitudes HTTP.

El backend procesa las solicitudes, aplica las reglas de negocio, verifica los permisos y administra el acceso a los datos persistidos en PostgreSQL.

### Flujo general

```text
Usuario
   |
   v
Frontend
React + TypeScript
   |
   | Solicitudes HTTP / JSON
   v
Backend
Java + Spring Boot
   |
   | Reglas de negocio
   | Seguridad y autorizaciones
   | Persistencia
   v
Base de datos
PostgreSQL
```

### Separación de responsabilidades

La arquitectura permite mantener diferenciadas:

- Presentación e interacción con el usuario.
- Recepción y procesamiento de solicitudes.
- Validación de datos.
- Reglas de negocio.
- Seguridad y autorización.
- Acceso y persistencia de información.

Esta organización facilita el mantenimiento, las pruebas y la incorporación de nuevas funcionalidades.

Para conocer el diseño técnico con mayor detalle, consultar la [documentación de arquitectura](docs/arquitectura.md).

---

## 📁 8. Estructura del repositorio

Todo el proyecto se encuentra centralizado en este repositorio de GitHub.

```text
sistema-administracion-consorcios/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── .env.example
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── .env.example
│
├── database/
│   ├── schema-consorcios.sql
│   ├── amenities.sql
│   ├── datos-prueba-carga.sql
│   ├── datos-prueba-amenities.sql
│   ├── datos-prueba-borrar.sql
│   ├── migracion-personas-consorcio.sql
│   ├── migracion-admin-multiconsorcio.sql
│   └── documentación del modelo
│
├── docs/
│   ├── arquitectura.md
│   ├── modulos.md
│   ├── instalacion.md
│   ├── plan-b-defensa.md
│   └── documentación académica
│
├── Entrega_1.md
└── README.md
```

La organización separa los componentes principales y facilita su identificación, ejecución y mantenimiento.

---

## 💻 9. Instalación y ejecución local

### 9.1. Requisitos previos

Para ejecutar el sistema localmente se necesita:

| Herramienta | Versión |
|---|---|
| Git | Reciente |
| Java JDK | 21 |
| Node.js | 20 o superior |
| PostgreSQL | Compatible con el esquema del proyecto |

No es necesario instalar Maven globalmente porque el repositorio incluye Maven Wrapper.

### 9.2. Clonar el repositorio

```bash
git clone https://github.com/gonzilla2021/sistema-administracion-consorcios.git
```

```bash
cd sistema-administracion-consorcios
```

### 9.3. Configurar el backend

Ingresar a la carpeta:

```bash
cd backend
```

En Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Editar el archivo `.env` y configurar las variables requeridas.

Variables principales:

| Variable | Descripción |
|---|---|
| DB_URL | URL JDBC de conexión a PostgreSQL. |
| DB_USER | Usuario de la base de datos. |
| DB_PASSWORD | Contraseña de PostgreSQL. |
| JWT_SECRET | Clave utilizada para firmar los tokens JWT. |
| JWT_EXPIRATION_MINUTES | Duración del token. |
| CORS_ORIGINS | Orígenes autorizados para consumir la API. |
| PORT | Puerto del backend, opcional. |

La clave JWT debe contener al menos 32 caracteres.

**Importante:** el archivo `.env.example` contiene ejemplos de configuración. Las credenciales reales deben permanecer fuera del control de versiones.

### 9.4. Configurar la base de datos

El proyecto dispone del script:

`database/schema-consorcios.sql`

Este archivo permite crear el esquema del núcleo administrativo en una base PostgreSQL nueva. Luego debe ejecutarse `database/amenities.sql`, que agrega las cuatro tablas del módulo de Amenities. El orden completo está en la [guía de instalación](docs/instalacion.md).

Para bases existentes que utilicen una versión anterior del esquema, se dispone de las siguientes migraciones:

1. `database/migracion-personas-consorcio.sql`
2. `database/migracion-admin-multiconsorcio.sql`

Las migraciones deben ejecutarse únicamente cuando correspondan y siguiendo el orden indicado en la documentación.

La aplicación utiliza Hibernate para validar la correspondencia entre las entidades y la estructura de la base.

Para la configuración detallada de Supabase o de PostgreSQL local, consultar la [guía de instalación](docs/instalacion.md).

### 9.5. Ejecutar el backend

Desde la carpeta `backend`, en Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

Por defecto, el backend se ejecuta en:

`http://localhost:8080`

Verificar el estado de la API:

`http://localhost:8080/api/health`

Si se configura otro puerto, deben actualizarse las direcciones correspondientes.

### 9.6. Ejecutar el frontend

Abrir otra terminal e ingresar en:

```bash
cd frontend
```

Crear el archivo de configuración:

```powershell
Copy-Item .env.example .env
```

Configurar:

```env
VITE_API_URL=http://localhost:8080
```

Instalar las dependencias:

```bash
npm install
```

Iniciar el servidor de desarrollo:

```bash
npm run dev
```

Abrir en el navegador:

`http://localhost:5173`

### 9.7. Primer usuario

El sistema no depende de un registro público para crear el primer superadministrador.

La creación inicial de esta cuenta debe realizarse siguiendo el procedimiento de la [guía de instalación](docs/instalacion.md), que explica cómo generar correctamente el hash de la contraseña.

No deben publicarse contraseñas reales ni volcados de bases de datos con información sensible.

---

## 🗄️ 10. Base de datos y migraciones

El sistema utiliza PostgreSQL como mecanismo de persistencia.

El modelo relacional permite representar consorcios, usuarios, unidades funcionales, personas, vinculaciones, gastos, liquidaciones y pagos.

### Documentación disponible

- [Esquema SQL de la base de datos](database/schema-consorcios.sql).
- [Esquema SQL del módulo de Amenities](database/amenities.sql).
- Datos de prueba (DML): [carga](database/datos-prueba-carga.sql), [amenities](database/datos-prueba-amenities.sql) y [borrado](database/datos-prueba-borrar.sql). Las cuentas demo se crean sin contraseña utilizable; ver la [guía de instalación](docs/instalacion.md).
- [Diagrama entidad-relación](database/diagrama-entidad-relacion.md).
- [Diagrama de clases del modelo de dominio](database/uml-modelo-dominio.md).
- [Guía de instalación y migraciones](docs/instalacion.md).

### Integridad de los datos

El diseño busca garantizar:

- Relaciones consistentes entre las entidades.
- Restricciones de integridad.
- Asociación correcta de los datos con sus consorcios.
- Validación de los coeficientes de las unidades.
- Preservación de los datos de las liquidaciones cerradas.
- Acceso controlado a la información.

Para una base nueva debe utilizarse el esquema actualizado; las migraciones documentadas se destinan a bases creadas con versiones anteriores.

---

## 🧪 11. Pruebas y validación

### Pruebas automatizadas del backend

El proyecto dispone de una batería de 55 pruebas automatizadas orientadas a verificar comportamientos del backend: 49 del núcleo administrativo y 6 del módulo de Amenities.

Las pruebas documentadas cubren funcionalidades relacionadas con:

- Autenticación.
- Gestión de personas.
- Vinculaciones.
- Registro de gastos.
- Generación de liquidaciones.
- Control de pagos.
- Validación de permisos y restricciones de acceso.
- Módulo de Amenities: alta de espacios, reservas de turnos y bloqueos administrativos.

### Resultado documentado

| Indicador | Resultado |
|---|---:|
| Pruebas ejecutadas | 55 |
| Pruebas aprobadas | 55 |
| Fallas | 0 |
| Errores | 0 |
| Omitidas | 0 |

Estos resultados corresponden a la ejecución local documentada y no constituyen por sí solos una validación integral de todas las funcionalidades del sistema.

### Ejecución de pruebas

Desde la carpeta `backend`:

```powershell
.\mvnw.cmd test
```

Las pruebas automatizadas documentadas utilizan datos simulados y no requieren conexión a la base de datos Supabase.

### Validaciones complementarias

Además de las pruebas automatizadas, la evaluación final debe contemplar el recorrido completo de las operaciones principales:

1. Inicio de sesión y verificación de roles.
2. Gestión de consorcios y unidades funcionales.
3. Registro y vinculación de personas.
4. Registro de gastos.
5. Generación de liquidaciones.
6. Cierre de liquidaciones.
7. Registro y consulta de estados de pago.
8. Comprobación de permisos de acceso.

El estado de validación integral debe documentarse según las pruebas efectivamente realizadas.

---

## 🔐 12. Seguridad

La seguridad se implementa principalmente en el backend mediante Spring Security y JWT.

Se contemplan los siguientes mecanismos:

- Autenticación de usuarios.
- Protección de contraseñas mediante hash.
- Validación de tokens.
- Autorización basada en roles.
- Restricciones de acceso a recursos.
- Control de la información según las vinculaciones entre usuarios, unidades y consorcios.
- Configuración de orígenes permitidos mediante CORS.
- Administración de secretos mediante variables de entorno.

### Protección de credenciales

Los archivos `.env` reales no deben incluirse en GitHub.

El repositorio proporciona archivos `.env.example` para indicar las variables necesarias sin publicar sus valores sensibles.

No deben incorporarse contraseñas, tokens JWT, claves privadas ni respaldos de bases de datos que contengan información sensible.

---

## ⚠️ 13. Alcances, limitaciones y dependencias externas

El sistema está orientado a la gestión administrativa de consorcios y a la liquidación de expensas.

### Limitaciones

- El control de pagos es administrativo y no incluye procesamiento de transacciones bancarias.
- El módulo de pagos no contempla pagos parciales.
- La disponibilidad de funcionalidades complementarias depende de su implementación y validación.
- La ejecución online depende de los servicios de alojamiento utilizados.
- Las pruebas automatizadas disponibles verifican los casos implementados, pero no sustituyen las pruebas funcionales integrales.

### Dependencias externas

| Servicio | Función |
|---|---|
| Netlify | Alojamiento del frontend. |
| Railway | Ejecución del backend. |
| Supabase | Alojamiento de PostgreSQL. |

### Alternativa para ejecución local

El proyecto incluye un procedimiento de respaldo para ejecutar el sistema utilizando PostgreSQL local mediante Docker.

Este procedimiento permite realizar una demostración sin depender de la conectividad con Supabase o con los servicios de despliegue.

Se encuentra documentado en:

[Plan B para la defensa](docs/plan-b-defensa.md).

Las configuraciones de base de datos sin autenticación incluidas en ese procedimiento están destinadas exclusivamente a un entorno local aislado de demostración y no deben utilizarse en producción.

---

## 📚 14. Documentación del proyecto

Toda la documentación del trabajo se encuentra referenciada desde este README.

| Documento | Contenido |
|---|---|
| [Primera entrega](Entrega_1.md) | Propuesta inicial, problemática y alcance. |
| [Arquitectura](docs/arquitectura.md) | Organización y diseño técnico del sistema. |
| [Módulos funcionales](docs/modulos.md) | Descripción y prioridades de funcionalidades. |
| [Diagrama entidad-relación](database/diagrama-entidad-relacion.md) | Modelo relacional de la base de datos. |
| [Esquema SQL](database/schema-consorcios.sql) | Creación de la estructura de datos. |
| [Esquema SQL de Amenities](database/amenities.sql) | Tablas del módulo de Amenities. |
| [Datos de prueba](database/datos-prueba-carga.sql) | Scripts DML de carga y borrado de datos ficticios. |
| [Diagrama de clases](database/uml-modelo-dominio.md) | Modelo de dominio. |
| [Guía de instalación](docs/instalacion.md) | Configuración y puesta en funcionamiento. |
| [Plan B de defensa](docs/plan-b-defensa.md) | Procedimiento para una ejecución local de respaldo. |
| [Informe Final (PDF)](docs/Informe_Final_Grupo128_Buchek_Casalderrey_Castellini.pdf) | Informe final del Proyecto Final, Grupo 128. |

---

## 🎓 15. Entrega final y defensa

El proyecto se presenta como Trabajo Final Integrador (Proyecto Final) de la Tecnicatura Universitaria en Programación de la UTN.

### Informe Final

El informe reúne la problemática abordada, los objetivos, la arquitectura, el diseño de datos, las decisiones técnicas, las funcionalidades y las pruebas realizadas.

**📄 Informe Final (PDF):** [docs/Informe_Final_Grupo128_Buchek_Casalderrey_Castellini.pdf](docs/Informe_Final_Grupo128_Buchek_Casalderrey_Castellini.pdf)

### Video de presentación

El video contiene una explicación general del proyecto, las principales decisiones técnicas y una demostración de las funcionalidades desarrolladas.

**🎬 Video de presentación final:** https://youtu.be/_2T6zQEDHxs

### Demostración del sistema

La aplicación puede consultarse en:

[Acceder al Sistema de Administración de Consorcios](https://administracion-consorcios.netlify.app/login)

Durante la defensa se pueden demostrar los procesos principales de administración, registro de gastos, generación de liquidaciones y consulta de información según el rol.

---

## 👨‍💻 16. Equipo de desarrollo

**Grupo 128 — Proyecto Final, Tecnicatura Universitaria en Programación (UTN)**

- Lautaro Buchek
- Hernán Casalderrey
- Gonzalo Castellini

**Tutora:** María Candela Grosso

**Universidad Tecnológica Nacional**

---

**Sistema de Administración de Consorcios — Trabajo Final Integrador**

Repositorio oficial: https://github.com/gonzilla2021/sistema-administracion-consorcios

