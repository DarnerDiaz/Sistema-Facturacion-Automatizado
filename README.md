# 🚀 Sistema de Facturacion Electronica Automatizado

![CI](https://img.shields.io/github/actions/workflow/status/DarnerDiaz/Sistema-Facturacion-Automatizado/ci.yml?branch=main&label=CI&style=for-the-badge)
![Version](https://img.shields.io/github/v/release/DarnerDiaz/Sistema-Facturacion-Automatizado?display_name=tag&style=for-the-badge)
![Stack](https://img.shields.io/badge/Stack-Spring%20Boot%20%7C%20React%20%7C%20MySQL-2ea44f?style=for-the-badge)

MVP orientado a Peru (PEN + IGV 18%) para gestionar facturas de extremo a extremo.

## ✨ Flujo principal del sistema

1. 👤 Registrar cliente
2. 🧾 Crear factura con items
3. ✅ Validar calculos y reglas
4. 📤 Emitir factura
5. 📄 Generar PDF
6. 📧 Enviar por correo
7. 🕘 Mantener historial y trazabilidad

## 📦 Que incluye este proyecto

- ⚙️ Backend API con Spring Boot, JWT, persistencia y migraciones
- 🖥️ Frontend web con React para operar clientes y facturas
- 📄 Generacion de PDF con iText
- ✉️ Envio de correo SMTP (recomendado SendGrid)
- 🐳 Contenedores Docker para entorno local
- 🔁 CI en GitHub Actions
- ☁️ Archivos listos para despliegue en Railway (backend) y Vercel (frontend)

## 🧠 Stack tecnico

- Backend: Java 17, Spring Boot, Spring Security, JPA, Flyway, MySQL, iText
- Frontend: React + Vite
- Infra: Docker Compose
- Auth: JWT (access + refresh)

## 🗂️ Estructura del repositorio

- backend: API REST, seguridad y logica de negocio
- frontend: aplicacion React
- database: artefactos de base de datos
- scripts: smoke tests
- docs: guias operativas de despliegue

## ⚡ Inicio rapido (recomendado)

### 🧩 Prerequisitos

- Docker + Docker Compose
- Puertos libres 5173, 8080, 3306, 8025

### 🛠️ Pasos

1. Copia `.env.example` a `.env`
2. Ajusta secretos y credenciales
3. Ejecuta:

```bash
docker compose up --build
```

### 🌐 Accesos locales

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Health: http://localhost:8080/actuator/health
- Mailpit (correo de prueba): http://localhost:8025

## 🧪 Inicio rapido sin Docker

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

### En Windows

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## 🎯 Primera prueba funcional (5 minutos)

1. Registra un usuario en `/api/v1/auth/register`
2. Inicia sesion en la UI
3. Crea un cliente
4. Crea una factura con items
5. Emite la factura
6. Descarga el PDF
7. Envia la factura por correo

💡 Tip: si usas Mailpit en local, revisa los correos en http://localhost:8025.

## 🔐 Variables de entorno

Usa `.env.example` como plantilla principal.

### Backend (clave)

- SPRING_PROFILES_ACTIVE
- APP_BASE_URL
- DB_URL (o DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD)
- JWT_SECRET
- JWT_ACCESS_EXP_MIN
- JWT_REFRESH_EXP_DAYS
- MAIL_HOST
- MAIL_PORT
- MAIL_USERNAME
- MAIL_PASSWORD
- MAIL_FROM
- CORS_ALLOWED_ORIGINS

### Frontend (clave)

- VITE_API_URL

## 🔌 Endpoints principales

### Auth

- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh

### Clientes

- POST /api/v1/customers
- GET /api/v1/customers
- GET /api/v1/customers/{id}

### Facturas

- POST /api/v1/invoices
- POST /api/v1/invoices/validate
- GET /api/v1/invoices
- GET /api/v1/invoices/{id}
- POST /api/v1/invoices/{id}/emit
- GET /api/v1/invoices/{id}/pdf
- POST /api/v1/invoices/{id}/send-email

## 🚀 Despliegue

### 🚂 Railway (backend)

1. Crea proyecto en Railway
2. Agrega MySQL
3. Crea servicio backend apuntando a carpeta `backend`
4. Configura variables de entorno (ver `.env.example`)
5. Usa healthcheck en `/actuator/health`

📝 Nota: Flyway ejecuta migraciones automaticamente al arranque.

### ▲ Vercel (frontend)

1. Crea proyecto apuntando a carpeta `frontend`
2. Configura `VITE_API_URL` con la URL publica del backend
3. Build command: `npm run build`
4. Output directory: `dist`

El archivo `frontend/vercel.json` ya incluye rewrite SPA para rutas internas.

## ✅ Verificacion post deploy

### Checklist completa

- docs/DEPLOY_CHECKLIST.md

### Smoke test (PowerShell)

```powershell
./scripts/smoke-test.ps1 -BaseUrl "https://api-tu-proyecto.up.railway.app" -FrontendUrl "https://tu-frontend.vercel.app"
```

### Smoke test (bash)

```bash
./scripts/smoke-test.sh "https://api-tu-proyecto.up.railway.app" "https://tu-frontend.vercel.app"
```

## 🧱 Estado del MVP

- 🔐 JWT y control de acceso por roles (ADMIN, EMISOR)
- 👥 Modulo de clientes
- 🧾 Modulo de facturas con validacion
- 📤 Emision, PDF y envio por correo
- 🕓 Historial basico de eventos
- 🟢 CI con build backend y frontend
