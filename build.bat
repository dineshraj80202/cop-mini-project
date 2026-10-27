@echo off
REM ========================================================
REM E-Commerce System - Build Script (Java 17/21+)
REM ========================================================
echo [INFO] Building E-Commerce Product Management System...

if not exist bin mkdir bin

REM Generate source file list
dir /s /b src\main\java\*.java > sources.txt

REM Compile
javac -encoding UTF-8 -cp "lib/mysql-connector-j-8.3.0.jar;lib/h2-2.2.224.jar;src/main/resources" -d bin @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation successful! Classes compiled to 'bin/'
) else (
    echo [ERROR] Compilation failed. Please check the errors above.
    exit /b %ERRORLEVEL%
)

del sources.txt
