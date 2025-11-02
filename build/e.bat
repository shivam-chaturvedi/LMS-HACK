@echo off
REM Script to run e.jar on Windows
REM Works even if Java is not in PATH

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set JAR_FILE=%SCRIPT_DIR%e.jar
set JRE_DIR=%SCRIPT_DIR%jre

REM Check if JRE is bundled
if exist "%JRE_DIR%\bin\java.exe" (
    set JAVA_CMD=%JRE_DIR%\bin\java.exe
    echo Using bundled JRE...
) else (
    REM Try system Java
    where java >nul 2>&1
    if errorlevel 1 (
        echo Error: Java not found. Please install Java or use bundled JRE.
        pause
        exit /b 1
    )
    set JAVA_CMD=java
    echo Using system Java...
)

REM Run the application
if exist "%JAR_FILE%" (
    echo Starting application...
    "%JAVA_CMD%" -jar "%JAR_FILE%"
) else (
    echo Error: e.jar not found in %SCRIPT_DIR%
    pause
    exit /b 1
)

pause

