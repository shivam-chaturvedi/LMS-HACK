# Getting Tesseract DLL for Windows

## Quick Download Options

### Option 1: Direct Download (Recommended)
1. Visit: https://github.com/UB-Mannheim/tesseract/wiki
2. Download: **tesseract-ocr-w64-setup-5.3.3.20231005.exe** (or latest version)
3. Install it OR extract the installer to get `tesseract.dll`
4. Copy `tesseract.dll` from installation directory to `build/resources/tesseract.dll`

### Option 2: Download Pre-built DLL
1. Visit: https://github.com/UB-Mannheim/tesseract/releases
2. Download the Windows binary package
3. Extract and find `tesseract.dll` (usually in `bin/` folder)
4. Copy to `build/resources/tesseract.dll`

### Option 3: Use vcpkg (if you have it)
```batch
vcpkg install tesseract:x64-windows
```
Then copy the DLL from vcpkg installation.

## Installation Path (if installed):
Default location: `C:\Program Files\Tesseract-OCR\`

Copy these files to `build/resources/`:
- `tesseract.dll` → `build/resources/tesseract.dll`
- `tessdata/eng.traineddata` → `build/resources/tessdata/eng.traineddata` (if not already there)

## Verification

After placing `tesseract.dll` in `build/resources/`, verify:
```batch
dir build\resources\tesseract.dll
```

File should exist and be ~2-3 MB in size.

