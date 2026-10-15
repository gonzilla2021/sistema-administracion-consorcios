# Guía de instalación y ejecución local

**Proyecto:** Sistema de Administración de Consorcios — Grupo 128

## 1. Requisitos

| Herramienta | Versión | Notas |
|---|---|---|
| Git | cualquiera reciente | |
| JDK | 21 | Windows: `winget install EclipseAdoptium.Temurin.21.JDK` |
| Node.js | 20 o superior | Solo para el frontend |

Maven **no** hace falta instalarlo: el repositorio incluye Maven Wrapper (`mvnw`).

Comprobar:

```powershell
java -version    # debe decir 21
node -v
```

## 2. Clonar el repositorio

```powershell
git clone https://github.com/gonzilla2021/sistema-administracion-consorcios.git
cd sistema-administracion-consorcios
```

## 3. Base de datos (Supabase)

La base ya está creada en el proyecto de Supabase del equipo. El esquema está aplicado; no hay que volver a ejecutar `database/schema-consorcios.sql`.

Las tablas están en el esquema `dbo` (en Supabase: Table Editor → esquema `dbo`).

### Conexión: directa o pooler

Probar si tu red llega por IPv6:

```powershell
Test-NetConnection db.gmymbsjmyjwtkcyggtyg.supabase.co -Port 5432
```

- `TcpTestSucceeded : True` → usar la **conexión directa** (opción A del `.env.example`).
- `False` → usar el **Session pooler** (opción B). El host se copia de Supabase → botón **Connect** → Session pooler.

## 4. Backend

```powershell
cd backend
copy .env.example .env
```

Editar `backend/.env`:

- `DB_PASSWORD`: pedirla al equipo por mensaje privado.
- `JWT_SECRET`: una cadena aleatoria de 32 caracteres o más. Puede ser distinta en cada máquina.
- Si se usa el pooler, cambiar `DB_URL` y `DB_USER` según la opción B.

Ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

El backend queda en `http://localhost:8080` (o en el puerto de `PORT`). El arranque correcto termina con `Started ConsorciosApplication`. Al iniciar, Hibernate valida que las entidades coincidan con las tablas (`ddl-auto=validate`).

### Estado de la conexión a Supabase

El proyecto de Supabase está creado (organización "Grupo 128 UTN", plan Free) y el esquema `database/schema-consorcios.sql` está aplicado en el esquema `dbo`. **La conexión del backend a la base está verificada** (octubre de 2026): el backend arranca, Hibernate valida las 8 tablas y `GET /api/health` responde `{"backend":"UP","baseDeDatos":"UP"}`.

Datos de conexión del Session pooler del proyecto (no son secretos; la contraseña sí):

```
host:     aws-0-us-east-2.pooler.supabase.com
puerto:   5432
base:     postgres
usuario:  postgres.gmymbsjmyjwtkcyggtyg
```

Si el puerto 8080 está ocupado (por ejemplo por Docker), agregar `PORT=9090` en `backend/.env` y `VITE_API_URL=http://localhost:9090` en `frontend/.env`.

### Verificar la conexión

Con el backend levantado:

```powershell
curl http://localhost:9090/api/health
```

| Respuesta | Significado |
|---|---|
| `200` con `"baseDeDatos":"UP"` | El backend llega a Supabase |
| `503` con `"baseDeDatos":"DOWN"` | El backend está activo pero no llega a la base (revisar `DB_URL`, `DB_USER`, `DB_PASSWORD`) |
| Sin respuesta | El backend no está levantado |

La pantalla de login del frontend muestra este mismo estado debajo del botón "Ingresar".

### Cómo levantar el backend sin errores de compilación

Si el IDE (extensión Java de VS Code) está abierto, puede recompilar `target/classes` sin Lombok mientras la aplicación arranca y producir `ClassFormatError ... Consorcio` o `NoClassDefFoundError`. Para evitarlo, empaquetar y ejecutar el `.jar`:

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target\consorcios-0.0.1-SNAPSHOT.jar
```

### Primer usuario (superadministrador)

La tabla `dbo.usuarios` arranca vacía y el sistema no tiene registro público, por lo que el primer superadministrador se crea directamente en la base. Ejecutar en DBeaver o en el SQL Editor de Supabase, reemplazando `<contraseña>` por la elegida (no guardarla en archivos ni en el repositorio):

```sql
insert into dbo.usuarios (email, passwordhash, rol)
values ('admin@consorcios.test',
        extensions.crypt('<contraseña>', extensions.gen_salt('bf', 10)),
        'SUPERADMINISTRADOR')
on conflict (email) do nothing;
```

`pgcrypto` genera un hash `$2a$` que Spring Security (`BCryptPasswordEncoder`) acepta. Para cambiar la contraseña de un usuario existente, usar `update dbo.usuarios set passwordhash = extensions.crypt('<contraseña>', extensions.gen_salt('bf', 10)) where email = '...'`. Si aparece `function extensions.crypt does not exist`, ejecutar antes `create extension if not exists pgcrypto with schema extensions;`.

### Errores frecuentes

| Mensaje | Causa |
|---|---|
| `password authentication failed` | `DB_PASSWORD` incorrecta, o `DB_USER` sin el prefijo `postgres.<ref>` al usar el pooler. **Revisar también si existe una variable de entorno `DB_PASSWORD` en Windows**: tiene prioridad sobre `backend/.env` y puede tener un valor viejo (`[Environment]::GetEnvironmentVariable('DB_PASSWORD','User')`; borrarla con `[Environment]::SetEnvironmentVariable('DB_PASSWORD', $null, 'User')` y reabrir la terminal y el IDE) |
| `Unknown host` (en DBeaver o JDBC) | Error de tipeo en el host. Debe ser `aws-0-us-east-2.pooler.supabase.com` (con la `a` inicial) |
| `UnknownHostException` / timeout | Tu red no tiene IPv6: usar el pooler |
| `Schema-validation: missing table` | El esquema no está aplicado en la base de datos |
| `ClassFormatError` / `NoClassDefFoundError` al arrancar | El IDE recompiló `target/` mientras corría la app: ejecutar el `.jar` (ver arriba) |
| `JWT_SECRET debe tener al menos 32 caracteres` | Falta `JWT_SECRET` en `backend/.env` |
| `JAVA_HOME` / versión de Java | Instalar JDK 21 y reabrir la terminal |

### Conectar con DBeaver

Usar el driver **PostgreSQL** (el estándar; no "Google Cloud SQL - PostgreSQL"), con los datos del pooler de arriba y la contraseña de la base. Los datos de la aplicación están en el esquema `dbo`.

## 5. Frontend

```powershell
cd frontend
copy .env.example .env
npm install
npm run dev
```

La aplicación queda en `http://localhost:5173` y consume la API en `VITE_API_URL` (por defecto `http://localhost:8080`).

## 6. Secretos

- Nunca se suben al repositorio: `.env` está en `.gitignore`.
- La contraseña de la base se comparte solo por mensaje privado, no por el repositorio ni por chats grupales.
- No se usan las claves `anon` ni `service_role` de Supabase: el backend se conecta directamente a PostgreSQL.
