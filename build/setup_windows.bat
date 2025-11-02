@echo off
REM Script to set up complete Windows build with JRE
REM This downloads Windows JRE and prepares all resources

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set JRE_DIR=%SCRIPT_DIR%jre
set RESOURCES_DIR=%SCRIPT_DIR%resources

echo ==========================================
echo Windows Build Setup
echo ==========================================
echo.

REM Step 1: Download Windows JRE
echo Step 1: Downloading Windows JRE 11...
echo.
call download_jre.bat

if not exist "%JRE_DIR%\bin\java.exe" (
    echo.
    echo Error: JRE download failed or verification failed
    echo Please check the jre directory
    pause
    exit /b 1
)

echo.
echo Step 2: Preparing resources for Windows...
echo.

REM Note: Windows users need to download tesseract.dll separately
REM or we need to include it in resources
echo.
echo Note: For Windows, you need to download tesseract.dll
echo Download from: https://github.com/UB-Mannheim/tesseract/wiki
echo Or install Tesseract-OCR and copy tesseract.dll to resources folder
echo.

echo.
echo ==========================================
echo Windows Build Setup Complete!
echo ==========================================
echo.
echo Files in build directory:
dir /b "%SCRIPT_DIR%"
echo.
echo To run: e.bat
echo.

pause

