# NexCommerce - Launch Full-Stack Web App Server
if (-not (Test-Path "bin/com/ecommerce/web/ApiServer.class")) {
    Write-Host "[INFO] Building project first..." -ForegroundColor Yellow
    .\build.ps1
}

Write-Host "[INFO] Starting NexCommerce Web Application on http://localhost:8080 ..." -ForegroundColor Green
Start-Process "http://localhost:8080"
java -cp "bin;lib/mysql-connector-j-8.3.0.jar;lib/h2-2.2.224.jar;src/main/resources" com.ecommerce.App
