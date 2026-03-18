#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:5173}"
EMAIL="smoke.admin@factura.local"
PASSWORD="Smoke12345!"

echo "==> Checking frontend"
curl -fsS "$FRONTEND_URL" >/dev/null

echo "==> Checking backend health"
curl -fsS "$BASE_URL/actuator/health" | grep '"UP"' >/dev/null

echo "==> Registering (idempotent if already exists)"
COMPANY_TAX_ID="20$RANDOM$RANDOM"
curl -sS -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"fullName\":\"Smoke Admin\",\"companyName\":\"Smoke Company\",\"companyTaxId\":\"$COMPANY_TAX_ID\"}" >/dev/null || true

echo "==> Login"
TOKEN=$(curl -sS -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

if [[ -z "$TOKEN" ]]; then
  echo "Login failed"
  exit 1
fi

echo "Smoke test prerequisites passed. Use PowerShell script for full E2E on Windows."
