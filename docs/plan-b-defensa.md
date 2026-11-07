# Plan B para la defensa: entorno local sin Supabase ni internet

**Proyecto:** Sistema de Administración de Consorcios — Grupo 128

Si durante la defensa fallan Supabase, el servicio donde está desplegado el sistema o la conexión a internet, el sistema se puede ejecutar completo en la computadora de uno de los integrantes: base de datos PostgreSQL local (en Docker), backend y frontend. Este procedimiento se probó de punta a punta el 7/11/2026: se bajó la base de Supabase, se restauró en un contenedor local, y el backend arrancó contra esa copia, validó el esquema y respondió `{"baseDeDatos":"UP","backend":"UP"}`.

El plan B tiene tres partes: **preparar** (con internet, unos días antes), **verificar** (el día anterior) y **ejecutar** (durante la defensa, sin internet).

## 1. Preparar (con internet, antes de la defensa)

### 1.1 Requisitos instalados en la computadora de respaldo

| Herramienta | Para qué |
|---|---|
| Docker Desktop | Base de datos PostgreSQL local y herramienta de volcado (no hace falta instalar PostgreSQL) |
| JDK 21 | Backend |
| Node.js 20 o superior | Frontend |

Con internet y **antes** de la defensa, hay que tener descargado todo lo que luego se usa sin conexión:

```powershell
docker pull postgres:17          # imagen de la base (la misma versión que Supabase)
cd backend
.\mvnw.cmd clean package -DskipTests   # descarga dependencias de Maven y deja el .jar listo
cd ..\frontend
npm install                      # descarga dependencias del frontend
```

> La imagen debe ser **`postgres:17`**: Supabase usa PostgreSQL 17 y `pg_dump` se niega a volcar un servidor de versión mayor (`server version mismatch`).

### 1.2 Bajar la copia de la base

La contraseña de la base está en `backend/.env` (`DB_PASSWORD`). Se carga en una variable de entorno de esa terminal; nunca se escribe en un comando ni en un archivo del repositorio:

```powershell
$env:PGPASSWORD = Read-Host "Contraseña de la base"
mkdir respaldo
docker run --rm -e PGPASSWORD -v "${PWD}\respaldo:/respaldo" postgres:17 pg_dump -h aws-0-us-east-2.pooler.supabase.com -p 5432 -U postgres.gmymbsjmyjwtkcyggtyg -d postgres --schema=dbo --no-owner --no-privileges -f /respaldo/respaldo.sql
Remove-Item Env:PGPASSWORD
```

Resultado esperado: `respaldo/respaldo.sql` (unos 300 KB con los datos de prueba), con el esquema `dbo`, las tablas, la función y el trigger que verifica que los coeficientes de un consorcio sumen 100.

> **Seguridad:** `respaldo.sql` contiene datos reales y los hashes de contraseña. No se sube al repositorio (`respaldo*.sql` está en `.gitignore`) ni se comparte por chats grupales. Guardarlo en la computadora de respaldo y en un pendrive propio.

### 1.3 Probar la restauración

```powershell
docker run -d --name consorcios-db -e POSTGRES_HOST_AUTH_METHOD=trust -e POSTGRES_DB=consorcios -p 5433:5432 postgres:17
docker cp respaldo\respaldo.sql consorcios-db:/tmp/respaldo.sql
docker exec consorcios-db psql -U postgres -d consorcios -v ON_ERROR_STOP=1 -q -f /tmp/respaldo.sql
```

`POSTGRES_HOST_AUTH_METHOD=trust` evita definir una contraseña: sirve solo porque la base escucha en la máquina local y nunca se expone.

Control de que la copia está completa (los números deben coincidir con los de Supabase):

```powershell
docker exec consorcios-db psql -U postgres -d consorcios -c "select (select count(*) from dbo.consorcios) consorcios, (select count(*) from dbo.personas) personas, (select count(*) from dbo.usuarios) usuarios, (select count(*) from dbo.unidadesfuncionales) unidades"
```

Una vez creado el contenedor, para apagarlo y volver a encenderlo: `docker stop consorcios-db` y `docker start consorcios-db`. Los datos se conservan.

## 2. Ejecutar el sistema en local

Con el contenedor `consorcios-db` encendido.

### 2.1 Backend contra la base local

Las variables de entorno del sistema tienen prioridad sobre `backend/.env`, así que **no hace falta editar ese archivo**: se definen en la terminal donde se lanza el backend.

```powershell
cd backend
$env:DB_URL = "jdbc:postgresql://localhost:5433/consorcios"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "local"      # la base local no valida contraseña; el valor es indistinto
$env:PORT = "9090"
java "-Duser.timezone=America/Argentina/Buenos_Aires" -jar target\consorcios-0.0.1-SNAPSHOT.jar
```

> **El parámetro `-Duser.timezone` es obligatorio.** Windows le informa a Java la zona `America/Buenos_Aires` (nombre antiguo) y el PostgreSQL de Docker la rechaza: `FATAL: invalid value for parameter "TimeZone"`, seguido de `Unable to determine Dialect without JDBC metadata`. Supabase la acepta, por eso el problema solo aparece en local.

El backend necesita además `JWT_SECRET` (32 caracteres o más), que se toma de `backend/.env` si ya existe.

Comprobar: `curl http://localhost:9090/api/health` debe responder `{"baseDeDatos":"UP","backend":"UP"}`.

### 2.2 Frontend

En `frontend/.env`, `VITE_API_URL` debe apuntar al puerto del backend local (`http://localhost:9090`). Luego:

```powershell
cd frontend
npm run dev        # http://localhost:5173
```

Para llegar al sistema alcanzan los usuarios de prueba (ver [instalacion.md](instalacion.md), sección "Datos de prueba").

### 2.3 Sin conexión a internet

Una vez preparado (sección 1), nada de lo anterior usa internet: el backend habla con `localhost:5433`, el frontend con `localhost:9090`, y las dependencias ya están descargadas. No funciona sin conexión lo que depende de servicios externos: el sistema desplegado y la base de Supabase.

## 3. Lista de control del día anterior a la defensa

- [ ] `respaldo.sql` actualizado: se bajó **después** de cargar los datos finales de la demo.
- [ ] El contenedor `consorcios-db` arranca y los conteos de las tablas coinciden con Supabase.
- [ ] El backend local arranca con `-Duser.timezone` y `/api/health` da `UP`.
- [ ] El frontend local muestra el login y se puede ingresar con un usuario de prueba.
- [ ] Se recorrió la demo completa en local (consorcios, unidades, personas, gastos, liquidación, pagos).
- [ ] Se hizo una prueba **con el WiFi apagado**.
- [ ] El video grabado, las capturas y el PDF del informe están en la computadora de respaldo.
- [ ] Docker Desktop está instalado y abierto antes de la llamada (tarda un minuto en arrancar).

## 4. Qué se pierde y qué no

| | Plan A (desplegado) | Plan B (local) |
|---|---|---|
| Código, funcionalidades y reglas de negocio | Sí | Sí, el mismo código |
| Datos de la demo | Sí | Sí, copia al momento del volcado |
| Cambios hechos durante la demo | En Supabase | Solo en la copia local |
| URL pública | Sí | No: se comparte pantalla |

En caso de usar el plan B, se explica con naturalidad al comité: el sistema está desplegado, pero se muestra la copia local por un problema de conectividad. El video y las capturas siguen siendo evidencia del sistema desplegado.
