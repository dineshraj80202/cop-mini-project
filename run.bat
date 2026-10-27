@echo off
REM ========================================================
REM E-Commerce System - Launch Script
REM ========================================================

if not exist bin\com\ecommerce\App.class (
    echo [INFO] Binaries not found. Building project first...
    call build.bat
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)

echo [INFO] Starting E-Commerce Product Management System...
java -cp "bin;lib/mysql-connector-j-8.3.0.jar;lib/h2-2.2.224.jar;src/main/resources" com.ecommerce.App
