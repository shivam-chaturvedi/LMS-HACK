#!/bin/bash

# Main build script - creates standalone distribution
# This builds the JAR and prepares the build directory

echo "=========================================="
echo "Building Standalone Distribution"
echo "=========================================="
echo ""

# Build JAR with all dependencies
echo "Step 1: Building JAR with dependencies..."
cd java
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Error: Build failed!"
    exit 1
fi

echo ""
echo "Step 2: Preparing build directory..."
cd ..
cd build
./prepare_build.sh

echo ""
echo "=========================================="
echo "Build Complete!"
echo "=========================================="
echo ""
echo "Build directory structure:"
echo "  build/"
echo "    ├── e.jar              (Main application JAR)"
echo "    ├── e.sh               (Run script for macOS/Linux)"
echo "    ├── e.bat              (Run script for Windows)"
echo "    ├── resources/         (Tesseract libraries and data)"
echo "    ├── jre/               (Place JRE here if needed)"
echo "    └── README_JRE.md      (JRE setup instructions)"
echo ""
echo "To use:"
echo "  macOS/Linux: ./build/e.sh"
echo "  Windows:     build\\e.bat"
echo ""
echo "Note: If Java is not installed, download JRE 11 and place in build/jre/"
echo "      See build/README_JRE.md for instructions"

