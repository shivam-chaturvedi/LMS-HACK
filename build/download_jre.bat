@echo off
REM Script to download and set up JRE on Windows
REM This downloads JRE 11 for Windows x64
REM Calls the improved download_windows_jre.bat script

setlocal

set SCRIPT_DIR=%~dp0

echo This script is deprecated. Using download_windows_jre.bat instead...
echo.

call "%SCRIPT_DIR%download_windows_jre.bat"

