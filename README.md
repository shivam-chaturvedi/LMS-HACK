# LMS OCR Application

Windows standalone OCR application with global hotkey support.

## Quick Start

After cloning the repository:

```batch
cd build
e.bat
```

That's it! The application will start and listen for the 's' key to capture and OCR screenshots.

## What's Included

The `build/` folder contains:
- **e.jar** - Application with all dependencies
- **e.bat** - Windows launcher script
- **jre/** - Bundled Windows Java Runtime (no Java installation needed)
- **resources/** - Tesseract OCR library and language data
  - `tesseract.dll` - Windows native OCR library
  - `tessdata/eng.traineddata` - English language model

## Requirements

- Windows 10 or later
- No additional software needed (Java and Tesseract are bundled)

## Usage

1. Run `e.bat` from the `build/` folder
2. Press 's' key anywhere to capture screenshot
3. OCR text will be displayed in the console
4. Press Ctrl+C to exit

## Building from Source

If you need to rebuild:

```bash
./build.sh
```

This compiles the Java application and prepares the build folder.

## Troubleshooting

- If the application doesn't start: Check that `jre/bin/java.exe` exists
- If OCR doesn't work: Verify `resources/tesseract.dll` is present
- If you see Java errors: The bundled JRE should work, but you can install Java 11+ separately

