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

El backend queda en `http://localhost:8080`. El arranque correcto termina con `Started ConsorciosApplication`. Al iniciar, Hibernate valida que las entidades coincidan con las tablas (`ddl-auto=validate`).

### Estado de la conexión a Supabase

El proyecto de Supabase está creado (organización "Grupo 128 UTN", plan Free) y el esquema `database/schema-consorcios.sql` se ejecutó con "Run without RLS". **La conexión del backend a la base todavía no fue verificada**: la prueba hecha con la contraseña del proyecto devolvió `password authentication failed` tanto desde el backend como desde otro cliente, por lo que quedó pendiente fijar la contraseña de la base (ver más abajo).

Pendientes para dejarla operativa:

1. Fijar la contraseña de la base. Si el botón "Reset database password" de Supabase no la aplica, se puede hacer desde el SQL Editor (solo letras y números):
   ```sql
   alter user postgres with password '<nueva-contraseña>';
   ```
2. Verificar que existen las 8 tablas en el esquema `dbo` (Table Editor → selector de esquema `dbo`, o con esta consulta):
   ```sql
   select table_name from information_schema.tables where table_schema = 'dbo' order by 1;
   ```
   Deben aparecer: `consorcios`, `gastos`, `liquidaciones`, `liquidacionesunidad`, `personas`, `unidadesfuncionales`, `usuarios`, `vinculacionesunidad` (PostgreSQL guarda en minúsculas los nombres sin comillas).
3. Cargar la contraseña en `backend/.env` y arrancar el backend (sección 4).

Datos de conexión del Session pooler del proyecto (no son secretos; la contraseña sí):

```
host:     aws-0-us-east-2.pooler.supabase.com
puerto:   5432
base:     postgres
usuario:  postgres.gmymbsjmyjwtkcyggtyg
```

Si el puerto 8080 está ocupado (por ejemplo por Docker), agregar `PORT=9090` en `backend/.env`.

### Errores frecuentes

| Mensaje | Causa |
|---|---|
| `password authentication failed` | `DB_PASSWORD` incorrecta (o `DB_USER` sin el prefijo `postgres.<ref>` al usar el pooler) |
| `UnknownHostException` / timeout | Tu red no tiene IPv6: usar el pooler |
| `Schema-validation: missing table` | El esquema no está aplicado en la base de datos |
| `JAVA_HOME` / versión de Java | Instalar JDK 21 y reabrir la terminal |

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
