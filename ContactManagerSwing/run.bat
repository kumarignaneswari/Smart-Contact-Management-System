@echo off
REM =====================================================
REM Contact Management System - Run Script
REM =====================================================

echo.
echo ========================================
echo Starting Contact Management System...
echo ========================================
echo.

cd /d "%~dp0"

REM Run the application
java -cp ".;mysql-connector-j-9.6.0.jar;lib\*" Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo Application terminated with errors!
    echo ========================================
    echo.
    pause
)
