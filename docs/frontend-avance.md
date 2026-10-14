# Frontend: avance de pantallas

**Proyecto:** Sistema de Administración de Consorcios — Grupo 128

Documento de trabajo con las pantallas del frontend implementadas hasta el momento. **Todavía no se probó contra el backend ni contra la base de datos**: las capturas muestran el comportamiento de la interfaz sin conexión.

## 1. Cómo levantarlo

```powershell
cd frontend
copy .env.example .env      # ajustar VITE_API_URL al puerto donde corra el backend
npm install
npm run dev                 # http://localhost:5173
```

## 2. Pantallas

### 2.1 Login (`/login`)

Archivo: `frontend/src/pages/LoginPage.tsx`

- Formulario de email y contraseña (React Hook Form).
- Valida que ambos campos no estén vacíos.
- Envía `POST /api/auth/login`; si responde bien, guarda el token y redirige al dashboard.
- Si falla, muestra un mensaje de error en el formulario.

| Estado inicial | Validación de campos |
|---|---|
| ![Login](capturas/login.png) | ![Login con validación](capturas/login-validacion.png) |

### 2.2 Dashboard (`/`)

Archivo: `frontend/src/pages/DashboardPage.tsx`

- Ruta protegida (`RutaProtegida`): sin token redirige a `/login`.
- Cabecera con el email, el rol del usuario y el botón "Salir".
- Listado de consorcios obtenido con `GET /api/consorcios`, con estados de carga, lista vacía y error con "Reintentar".

![Dashboard sin backend](capturas/dashboard.png)

La captura se tomó con una sesión simulada en el navegador (sin autenticación real) y sin backend: por eso se ve el aviso de error al cargar los consorcios, que es el comportamiento esperado en ese caso.

## 3. Contrato esperado del backend

Definido en `frontend/src/types/index.ts`. **Está por acordar con quien implemente la autenticación y los consorcios.**

| Método y ruta | Cuerpo / respuesta |
|---|---|
| `POST /api/auth/login` | Recibe `{ email, password }`. Devuelve `{ token, email, rol }`. |
| `GET /api/consorcios` | Devuelve una lista de `{ id, nombre, direccion, fechaAlta }`. Requiere `Authorization: Bearer <token>`. |

`rol` es uno de: `SUPERADMINISTRADOR`, `ADMINISTRADOR_CONSORCIO`, `PROPIETARIO`, `INQUILINO`.

## 4. Estructura relevante

```text
frontend/src/
├── components/RutaProtegida.tsx   Redirige a /login si no hay sesión
├── pages/LoginPage.tsx
├── pages/DashboardPage.tsx
├── services/api.ts                fetch con el token JWT
├── services/auth.ts               login, logout y sesión actual
└── types/index.ts                 enums y contrato con la API
```

## 5. Pendiente

- Probar el flujo completo cuando el backend tenga la autenticación y el endpoint de consorcios, y la conexión a Supabase esté verificada (ver [instalacion.md](instalacion.md)).
- Pantallas siguientes del MVP: unidades funcionales, personas y vinculaciones, gastos, liquidaciones y pagos.
- Mostrar menús y acciones según el rol (la seguridad real se aplica en el backend).
