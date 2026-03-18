param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$FrontendUrl = "http://localhost:5173",
  [switch]$SkipRegister
)

$ErrorActionPreference = "Stop"

function Step($message) {
  Write-Host "`n==> $message" -ForegroundColor Cyan
}

function Ensure-Ok($condition, $message) {
  if (-not $condition) {
    throw $message
  }
}

function Wait-ForHealth($url, $maxAttempts = 30, $sleepSeconds = 2) {
  for ($i = 1; $i -le $maxAttempts; $i++) {
    try {
      $response = Invoke-RestMethod -Uri $url -Method Get
      if ($response.status -eq "UP") {
        return
      }
    } catch {
      Start-Sleep -Seconds $sleepSeconds
      continue
    }
    Start-Sleep -Seconds $sleepSeconds
  }
  throw "Backend health no alcanzó estado UP en el tiempo esperado"
}

$registerEmail = "smoke.admin@factura.local"
$registerPassword = "Smoke12345!"
$companyTaxId = "20" + (Get-Random -Minimum 100000000 -Maximum 999999999)

Step "Verificando frontend"
$frontendStatus = (Invoke-WebRequest -Uri $FrontendUrl -UseBasicParsing).StatusCode
Ensure-Ok ($frontendStatus -eq 200) "Frontend no responde 200"

Step "Verificando backend health"
Wait-ForHealth "$BaseUrl/actuator/health"
$health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method Get
Ensure-Ok ($health.status -eq "UP") "Backend health no está UP"

if (-not $SkipRegister) {
  Step "Registrando usuario de smoke test"
  $registerBody = @{
    email = $registerEmail
    password = $registerPassword
    fullName = "Smoke Admin"
    companyName = "Smoke Company"
    companyTaxId = $companyTaxId
  } | ConvertTo-Json

  Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/register" -Method Post -Body $registerBody -ContentType "application/json" | Out-Null
}

Step "Iniciando sesión"
$loginBody = @{
  email = $registerEmail
  password = $registerPassword
} | ConvertTo-Json
$login = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $login.accessToken
$headers = @{ Authorization = "Bearer $token" }

Step "Creando cliente"
$customerBody = @{
  name = "Cliente Smoke"
  taxId = "10" + (Get-Random -Minimum 100000000 -Maximum 999999999)
  email = "cliente.smoke@example.com"
  address = "Lima"
  phone = "999888777"
} | ConvertTo-Json
$customer = Invoke-RestMethod -Uri "$BaseUrl/api/v1/customers" -Method Post -Headers $headers -Body $customerBody -ContentType "application/json"

Step "Creando factura DRAFT"
$invoiceBody = @{
  customerId = $customer.id
  issueDate = (Get-Date).ToString("yyyy-MM-dd")
  dueDate = (Get-Date).AddDays(15).ToString("yyyy-MM-dd")
  notes = "Factura de smoke test"
  items = @(
    @{
      description = "Servicio QA"
      quantity = 1
      unitPrice = 150
      taxPercentage = 18
    }
  )
} | ConvertTo-Json -Depth 5

$invoice = Invoke-RestMethod -Uri "$BaseUrl/api/v1/invoices" -Method Post -Headers $headers -Body $invoiceBody -ContentType "application/json"
Ensure-Ok ($invoice.status -eq "DRAFT") "La factura no quedó en DRAFT"

Step "Emitiendo factura"
$emitBody = @{ reason = "Smoke test" } | ConvertTo-Json
$emitted = Invoke-RestMethod -Uri "$BaseUrl/api/v1/invoices/$($invoice.id)/emit" -Method Post -Headers $headers -Body $emitBody -ContentType "application/json"
Ensure-Ok ($emitted.status -eq "ISSUED") "La factura no pasó a ISSUED"

Step "Descargando PDF"
$pdfResponse = Invoke-WebRequest -Uri "$BaseUrl/api/v1/invoices/$($invoice.id)/pdf" -Headers $headers -Method Get
Ensure-Ok ($pdfResponse.StatusCode -eq 200) "No se pudo descargar PDF"

Step "Enviando correo"
$emailResult = Invoke-RestMethod -Uri "$BaseUrl/api/v1/invoices/$($invoice.id)/send-email" -Headers $headers -Method Post
Ensure-Ok ($emailResult.status -eq "SENT") "El envío no fue SENT"

Step "Smoke test finalizado OK"
Write-Host ("Factura: {0} | Estado final: {1}" -f $emitted.invoiceNumber, $emailResult.status) -ForegroundColor Green
Write-Host "Mailpit UI: http://localhost:8025" -ForegroundColor Yellow
