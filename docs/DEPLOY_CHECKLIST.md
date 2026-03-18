# Deploy Checklist - Railway + Vercel

## 1) Dominios reales (rellenar)

- Backend Railway URL: https://api-tu-proyecto.up.railway.app
- Frontend Vercel URL: https://tu-frontend.vercel.app
- Dominio personalizado frontend (opcional): https://app.tudominio.com
- Dominio verificado SendGrid (requerido para MAIL_FROM): billing.tudominio.com

## 2) Railway - Backend service

Configura el servicio desde la carpeta backend usando Dockerfile.

Variables requeridas:

- PORT=8080
- SPRING_PROFILES_ACTIVE=prod
- APP_BASE_URL=https://api-tu-proyecto.up.railway.app
- DB_URL=jdbc:mysql://<host>:<port>/<db>?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC
- DB_USER=<usuario_db>
- DB_PASSWORD=<password_db>
- JWT_SECRET=<secreto_largo_64_plus>
- JWT_ACCESS_EXP_MIN=30
- JWT_REFRESH_EXP_DAYS=7
- MAIL_HOST=smtp.sendgrid.net
- MAIL_PORT=587
- MAIL_USERNAME=apikey
- MAIL_PASSWORD=<sendgrid_api_key>
- MAIL_FROM=facturacion@billing.tudominio.com
- MAIL_SMTP_AUTH=true
- MAIL_SMTP_STARTTLS=true
- CORS_ALLOWED_ORIGINS=https://tu-frontend.vercel.app,https://app.tudominio.com

Healthcheck:

- /actuator/health

## 3) Railway - MySQL service

- Crear servicio MySQL administrado.
- Copiar host, puerto, db, usuario y password al backend.
- Confirmar conectividad JDBC desde backend logs.

## 4) Vercel - Frontend service

Proyecto desde carpeta frontend.

Settings:

- Build command: npm run build
- Output directory: dist

Variable requerida:

- VITE_API_URL=https://api-tu-proyecto.up.railway.app

Archivo ya incluido:

- frontend/vercel.json (rewrite SPA)

## 5) Smoke test post-deploy

PowerShell (Windows):

```powershell
./scripts/smoke-test.ps1 -BaseUrl "https://api-tu-proyecto.up.railway.app" -FrontendUrl "https://tu-frontend.vercel.app"
```

## 6) Criterio de Go-Live

- Actuator health en UP
- Login y emisión de factura funcional
- Descarga de PDF funcional
- Envío de correo funcional con SendGrid
- CORS validado desde dominio frontend real
