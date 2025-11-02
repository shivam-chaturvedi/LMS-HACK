# Windows Setup - Quick Start

## Complete Windows Setup (2 steps)

### Step 1: Download Windows JRE

Run this command in the `build` folder:
```batch
download_windows_jre.bat
```

This will automatically:
- Download JRE 11 for Windows x64 (36-40 MB)
- Extract it to `jre/` folder
- Verify installation

**OR manually:**
1. Download from: https://adoptium.net/temurin/releases/
   - Select: Java 11, Windows x64, JRE
2. Extract the zip
3. Copy all contents to `build/jre/`
4. Verify: `build/jre/bin/java.exe` exists

### Step 2: Get Tesseract DLL for Windows

**Download tesseract.dll:**
1. Visit: https://github.com/UB-Mannheim/tesseract/wiki
2. Download: **tesseract-ocr-w64-setup** (latest version)
3. Install it OR extract to get `tesseract.dll`
4. Copy `tesseract.dll` to `build/resources/tesseract.dll`

**Location options:**
- From installer: `C:\Program Files\Tesseract-OCR\bin\tesseract.dll`
- From GitHub releases: Extract and find in `bin/` folder

### Step 3: Run the Application

```batch
cd build
e.bat
```

That's it! The app will start using the bundled JRE.

## Final Windows Build Structure

```
build/
├── e.jar                           ✅ Main application
├── e.bat                           ✅ Windows launcher
├── jre/                            ✅ Windows JRE
│   └── bin/
│       └── java.exe                ✅ Must exist
└── resources/                      ✅ Resources
    ├── tesseract.dll               ✅ Windows native library (REQUIRED)
    └── tessdata/
        └── eng.traineddata         ✅ Language data
```

## File Checklist

Before distributing, verify:
- [ ] `e.jar` exists (~22 MB)
- [ ] `e.bat` exists
- [ ] `jre/bin/java.exe` exists
- [ ] `resources/tesseract.dll` exists (~2-3 MB)
- [ ] `resources/tessdata/eng.traineddata` exists (~22 MB)

## Total Size: ~97-107 MB

The `build/` folder is now a complete Windows standalone distribution!

