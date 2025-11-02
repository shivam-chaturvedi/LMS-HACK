# Windows Build Setup Instructions

## Complete Windows Distribution Setup

### Step 1: Download Windows JRE

Run the Windows JRE download script:
```batch
download_windows_jre.bat
```

Or manually:
1. Visit: https://adoptium.net/temurin/releases/
2. Download: Java 11, Windows x64, JRE (not JDK)
3. Extract the zip file
4. Copy entire contents to `build/jre/`
5. Verify: `build/jre/bin/java.exe` exists

### Step 2: Download Windows Tesseract DLL

For Windows, you need `tesseract.dll`. Options:

#### Option A: Download from GitHub
1. Visit: https://github.com/UB-Mannheim/tesseract/wiki
2. Download Windows installer or binaries
3. Extract `tesseract.dll` 
4. Place in `build/resources/tesseract.dll`

#### Option B: Install Tesseract-OCR
1. Download Tesseract-OCR for Windows from: https://github.com/UB-Mannheim/tesseract/wiki
2. Install it (default: `C:\Program Files\Tesseract-OCR`)
3. Copy `tesseract.dll` from installation to `build/resources/tesseract.dll`

### Step 3: Verify Build Structure

Your `build/` folder should have:
```
build/
├── e.jar                    # Application JAR
├── e.bat                    # Windows launcher
├── e.sh                     # macOS/Linux launcher (can remove for Windows-only)
├── jre/                     # Windows JRE
│   └── bin/
│       └── java.exe         # ✅ Must exist
├── resources/               # Resources
│   ├── tesseract.dll        # ✅ Windows native library (must exist)
│   └── tessdata/
│       └── eng.traineddata  # ✅ Language data
└── download_windows_jre.bat
```

### Step 4: Test

Run:
```batch
cd build
e.bat
```

The application should start without requiring Java installation.

## Windows-Only Distribution

To create a Windows-only distribution:

1. **Remove macOS/Linux files:**
   - Delete `e.sh`
   - Keep only `e.bat`

2. **Keep Windows files:**
   - `e.jar`
   - `e.bat`
   - `jre/` (Windows JRE)
   - `resources/` (with `tesseract.dll`)

3. **Zip the build folder:**
   - Zip everything in `build/`
   - Name it `lms-ocr-windows.zip`
   - Users can extract and run `e.bat`

## File Sizes (Windows):
- e.jar: ~22 MB
- jre/ (Windows): ~50-60 MB
- resources/: ~25 MB
- **Total: ~97-107 MB**

## Requirements:
- Windows 10 or later
- No Java installation needed (JRE bundled)

