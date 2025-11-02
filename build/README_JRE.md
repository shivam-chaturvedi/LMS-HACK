# JRE Setup Instructions

To make this application work without requiring Java installation, you need to bundle a JRE (Java Runtime Environment).

## Option 1: Download JRE separately and place in `jre/` folder

### For Windows:
1. Download JRE 11 for Windows from: https://adoptium.net/temurin/releases/
2. Extract the JRE
3. Place the entire JRE folder contents into `build/jre/` directory
4. Structure should be: `build/jre/bin/java.exe`

### For macOS:
1. Download JRE 11 for macOS from: https://adoptium.net/temurin/releases/
2. Extract the JRE
3. Place the entire JRE folder contents into `build/jre/` directory
4. Structure should be: `build/jre/Contents/Home/bin/java`

### For Linux:
1. Download JRE 11 for Linux from: https://adoptium.net/temurin/releases/
2. Extract the JRE
3. Place the entire JRE folder contents into `build/jre/` directory
4. Structure should be: `build/jre/bin/java`

## Option 2: Use system Java (if installed)

If Java is already installed on the system, the scripts will automatically use it.
The bundled JRE is only needed if Java is not installed.

## Notes:
- Minimum Java version required: Java 11
- The scripts (`e.bat` for Windows, `e.sh` for macOS/Linux) will automatically detect and use bundled JRE if available
- JRE size is typically 50-100 MB

