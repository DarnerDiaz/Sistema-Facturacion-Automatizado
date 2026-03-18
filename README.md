# Sistema de Facturacion Electronica Automatizada

MVP para Peru (PEN + IGV 18%) con flujo: crear factura, validar, emitir, generar PDF, enviar por correo y conservar historial.

## Stack

- Backend: Java 17 + Spring Boot + MySQL + Flyway + iText + JWT
- Frontend: React + Vite
- Correo: SMTP (SendGrid recomendado)
- Deploy objetivo: Railway (backend + MySQL) y Vercel (frontend)

## Estructura

- backend: API REST y seguridad
- frontend: UI React
- database: semillas y artefactos SQL

## Variables de entorno

Usa [.env.example](.env.example) como base.

Backend clave:

- SPRING_PROFILES_ACTIVE
- APP_BASE_URL
- DB_URL (o DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD)
- JWT_SECRET, JWT_ACCESS_EXP_MIN, JWT_REFRESH_EXP_DAYS
- MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM
- CORS_ALLOWED_ORIGINS

Frontend clave:

- VITE_API_URL

## Ejecucion local con Docker

1. Copia `.env.example` a `.env` y ajusta credenciales.
2. Ejecuta:

```bash
docker compose up --build
```

3. Accesos:

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Health: http://localhost:8080/actuator/health

## Ejecucion local sin Docker

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Despliegue Railway (Backend)

1. Crea un proyecto en Railway y selecciona el repositorio.
2. Crea un servicio MySQL dentro del proyecto.
3. Crea un servicio para backend apuntando a la carpeta `backend`.
4. Configura variables del backend:

- `SPRING_PROFILES_ACTIVE=prod`
- `PORT=8080`
- `APP_BASE_URL=https://<tu-backend>.up.railway.app`
- `DB_URL=jdbc:mysql://<host>:<port>/<db>?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- `DB_USER=<usuario>`
- `DB_PASSWORD=<password>`
- `JWT_SECRET=<secreto_largo>`
- `JWT_ACCESS_EXP_MIN=30`
- `JWT_REFRESH_EXP_DAYS=7`
- `MAIL_HOST=smtp.sendgrid.net`
- `MAIL_PORT=587`
- `MAIL_USERNAME=apikey`
- `MAIL_PASSWORD=<sendgrid_api_key>`
- `MAIL_FROM=<dominio_verificado_en_sendgrid>`
- `CORS_ALLOWED_ORIGINS=https://<tu-frontend>.vercel.app,http://localhost:5173`

5. Healthcheck recomendado en Railway: `/actuator/health`

Nota: las migraciones Flyway se ejecutan al arranque.

## Despliegue Vercel (Frontend)

1. Crea proyecto en Vercel apuntando a carpeta `frontend`.
2. Configura variable:

- `VITE_API_URL=https://<tu-backend>.up.railway.app`

3. Build settings:

- Build command: `npm run build`
- Output directory: `dist`

4. El archivo [frontend/vercel.json](frontend/vercel.json) ya incluye rewrite SPA para rutas internas.

## Checklist final de despliegue

- Ver detalle en [docs/DEPLOY_CHECKLIST.md](docs/DEPLOY_CHECKLIST.md)

## Smoke test post-deploy

Windows (PowerShell):

```powershell
./scripts/smoke-test.ps1 -BaseUrl "https://api-tu-proyecto.up.railway.app" -FrontendUrl "https://tu-frontend.vercel.app"
```

## Endpoints base

Auth:

- POST `/api/v1/auth/register`
- POST `/api/v1/auth/login`
- POST `/api/v1/auth/refresh`

Clientes:

- POST `/api/v1/customers`
- GET `/api/v1/customers`
- GET `/api/v1/customers/{id}`

Facturas:

- POST `/api/v1/invoices`
- POST `/api/v1/invoices/validate`
- GET `/api/v1/invoices`
- GET `/api/v1/invoices/{id}`
- POST `/api/v1/invoices/{id}/emit`
- GET `/api/v1/invoices/{id}/pdf`
- POST `/api/v1/invoices/{id}/send-email`

## Estado actual

- Seguridad JWT implementada
- Modelo y migracion inicial implementados
- Flujo principal de facturas implementado
- Frontend base conectado al API
- Archivos de despliegue preparados para Railway y Vercel
