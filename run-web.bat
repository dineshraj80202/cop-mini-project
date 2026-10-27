@echo off
REM ========================================================
REM NexCommerce - Launch Full-Stack Web App Server
REM ========================================================

if not exist bin\com\ecommerce\web\ApiServer.class (
    echo [INFO] Building project first...
    call build.bat
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)

echo [INFO] Starting NexCommerce Web Application on http://localhost:8080 ...
start "" "http://localhost:8080"
java -cp "bin;lib/mysql-connector-j-8.3.0.jar;lib/h2-2.2.224.jar;src/main/resources" com.ecommerce.App
