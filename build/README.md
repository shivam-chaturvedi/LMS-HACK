# Standalone Application Build

This directory contains a fully standalone distribution of the application.

## Quick Start

### Windows:
Double-click `e.bat` or run in Command Prompt:
```
e.bat
```

### macOS/Linux:
Run in terminal:
```bash
./e.sh
```

## Contents

- **e.jar** - Main application JAR file with all dependencies
- **e.bat** - Windows launcher script
- **e.sh** - macOS/Linux launcher script  
- **resources/** - Tesseract libraries and language data
- **jre/** - Place Java Runtime Environment here (optional, see below)

## Java Runtime (JRE)

### Option 1: Use System Java
If Java 11 or higher is installed on your system, the scripts will automatically use it.
No JRE folder needed.

### Option 2: Bundle JRE
If Java is not installed, you can bundle a JRE:

1. Download JRE 11 from: https://adoptium.net/temurin/releases/
2. Extract the JRE
3. Place it in the `jre/` folder:
   - **Windows**: `jre/bin/java.exe` should exist
   - **macOS**: `jre/Contents/Home/bin/java` should exist
   - **Linux**: `jre/bin/java` should exist

The scripts will automatically detect and use the bundled JRE.

## Requirements

- Minimum Java version: Java 11
- OS: Windows, macOS, or Linux

## Troubleshooting

- If you see "Java not found": Install Java 11+ or place JRE in `jre/` folder
- If OCR doesn't work: Make sure `resources/` folder contains tessdata and native libraries
- On first run, macOS may ask for Accessibility permissions (for global key listening)

