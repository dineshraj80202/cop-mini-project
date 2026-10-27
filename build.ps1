# E-Commerce System - PowerShell Build Script
Write-Host "[INFO] Building E-Commerce System..." -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$sources = (Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java").FullName
[System.IO.File]::WriteAllLines("sources.txt", $sources)

javac -encoding UTF-8 -cp "lib/mysql-connector-j-8.3.0.jar;lib/h2-2.2.224.jar;src/main/resources" -d "bin" "@sources.txt"

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation completed successfully! Binaries in bin/" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
}

Remove-Item "sources.txt" -ErrorAction SilentlyContinue
