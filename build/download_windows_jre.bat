@echo off
REM Simplified Windows JRE download script using PowerShell
REM Downloads JRE 11 for Windows x64

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set JRE_DIR=%SCRIPT_DIR%jre
set TEMP_FILE=%TEMP%\jre_windows.zip

echo ==========================================
echo Downloading Windows JRE 11
echo ==========================================
echo.

REM Check if PowerShell is available
where powershell >nul 2>&1
if errorlevel 1 (
    echo Error: PowerShell is required but not found.
    echo Please download JRE manually from: https://adoptium.net/temurin/releases/
    pause
    exit /b 1
)

echo Downloading JRE 11 for Windows x64...
echo This may take a few minutes (36-40 MB download)...
echo.

REM Download using PowerShell
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
"$ProgressPreference = 'SilentlyContinue'; ^
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
$url = 'https://api.adoptium.net/v3/binary/latest/11/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk'; ^
$out = '%TEMP_FILE%'; ^
try { ^
    (New-Object System.Net.WebClient).DownloadFile($url, $out); ^
    Write-Host 'Download complete!' -ForegroundColor Green; ^
    Write-Host 'File size:' (Get-Item $out).Length 'bytes' ^
} catch { ^
    Write-Host 'Download failed:' $_.Exception.Message -ForegroundColor Red; ^
    exit 1 ^
}"

if not exist "%TEMP_FILE%" (
    echo.
    echo Error: Download failed!
    pause
    exit /b 1
)

echo.
echo Extracting JRE...
echo.

REM Remove old jre if exists
if exist "%JRE_DIR%" (
    echo Removing old JRE...
    rmdir /s /q "%JRE_DIR%" 2>nul
)

REM Create jre directory
mkdir "%JRE_DIR%" 2>nul

REM Extract using PowerShell
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
"$ProgressPreference = 'SilentlyContinue'; ^
$zip = '%TEMP_FILE%'; ^
$dest = '%TEMP%\jre_extract'; ^
if (Test-Path $dest) { Remove-Item $dest -Recurse -Force }; ^
Expand-Archive -Path $zip -DestinationPath $dest -Force; ^
$jreFolder = Get-ChildItem $dest -Directory | Where-Object { $_.Name -like '*jdk-11*' -or $_.Name -like '*jre-11*' } | Select-Object -First 1; ^
if ($jreFolder) { ^
    Copy-Item $jreFolder\* '%JRE_DIR%' -Recurse -Force; ^
    Write-Host 'Extraction complete!' -ForegroundColor Green ^
} else { ^
    Write-Host 'Error: Could not find JRE folder in archive' -ForegroundColor Red; ^
    exit 1 ^
}; ^
Remove-Item $dest -Recurse -Force -ErrorAction SilentlyContinue"

REM Clean up
del "%TEMP_FILE%" 2>nul

REM Verify
if exist "%JRE_DIR%\bin\java.exe" (
    echo.
    echo ==========================================
    echo JRE Setup Successful!
    echo ==========================================
    echo.
    echo Verification:
    "%JRE_DIR%\bin\java.exe" -version
    echo.
    echo JRE location: %JRE_DIR%
    echo.
) else (
    echo.
    echo Error: JRE extraction verification failed
    echo Please check %JRE_DIR% directory
    echo.
    echo You may need to manually:
    echo 1. Download JRE 11 from: https://adoptium.net/temurin/releases/
    echo 2. Extract it
    echo 3. Copy contents to %JRE_DIR%
    pause
    exit /b 1
)

echo.
echo Setup complete! JRE is ready in jre folder.
echo.

