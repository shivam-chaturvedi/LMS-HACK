# Build Instructions

## Complete Build Process

### 1. Build the Application

From project root, run:
```bash
./build.sh
```

Or manually:
```bash
cd java
mvn clean package -DskipTests
cd ../build
./prepare_build.sh
```

### 2. Add JRE (Optional - only if users don't have Java)

#### Windows:
1. Download JRE 11 Windows x64 from: https://adoptium.net/temurin/releases/
   - Choose: Java 11, Windows x64, JRE
2. Extract the zip file
3. Copy the entire extracted folder contents to `build/jre/`
4. Structure: `build/jre/bin/java.exe` should exist

#### macOS:
1. Download JRE 11 macOS from: https://adoptium.net/temurin/releases/
   - Choose: Java 11, macOS (ARM64 or x64 based on your Mac)
2. Extract the .tar.gz file
3. Copy the entire extracted folder to `build/jre/`
4. Structure: `build/jre/Contents/Home/bin/java` should exist

#### Linux:
1. Download JRE 11 Linux from: https://adoptium.net/temurin/releases/
2. Extract the .tar.gz file
3. Copy the entire extracted folder contents to `build/jre/`
4. Structure: `build/jre/bin/java` should exist

## Distribution

The `build/` folder is now a standalone distribution. You can:
- Zip the entire `build/` folder
- Share it with others
- They can run `e.bat` (Windows) or `e.sh` (macOS/Linux) without installing Java

## File Structure

```
build/
├── e.jar                 # Main application (22MB)
├── e.bat                 # Windows launcher
├── e.sh                  # macOS/Linux launcher
├── resources/            # Tesseract libraries
│   ├── libtesseract.dylib
│   └── tessdata/
│       └── eng.traineddata
├── jre/                  # Java Runtime (optional)
│   └── [JRE files here]
└── README.md             # User instructions
```

## Testing

Before distributing, test the build:
- Windows: `cd build && e.bat`
- macOS/Linux: `cd build && ./e.sh`

The application should start and listen for 's' key presses.

