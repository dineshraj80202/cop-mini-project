# E-Commerce System - PowerShell Run Script
if (-not (Test-Path "bin/com/ecommerce/App.class")) {
    Write-Host "[INFO] Project not compiled yet. Running build.ps1..." -ForegroundColor Yellow
    .\build.ps1
}

Write-Host "[INFO] Launching E-Commerce Product Management System..." -ForegroundColor Green
java -cp "bin;lib/mysql-connector-j-8.3.0.jar;lib/h2-2.2.224.jar;src/main/resources" com.ecommerce.App
