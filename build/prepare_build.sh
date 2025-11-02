#!/bin/bash

# Script to prepare the build directory with all resources
# This copies the JAR and sets up the build structure

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
JAVA_DIR="$PROJECT_ROOT/java"

echo "Preparing build directory..."

# Copy JAR file
if [ -f "$JAVA_DIR/target/lms-project-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    cp "$JAVA_DIR/target/lms-project-1.0-SNAPSHOT-jar-with-dependencies.jar" "$SCRIPT_DIR/e.jar"
    echo "✓ Copied JAR file to e.jar"
else
    echo "Error: JAR file not found. Please run 'mvn package' first."
    exit 1
fi

# Copy resources
if [ -d "$JAVA_DIR/src/main/resources" ]; then
    cp -r "$JAVA_DIR/src/main/resources"/* "$SCRIPT_DIR/resources/" 2>/dev/null
    echo "✓ Copied resources"
fi

echo ""
echo "Build directory prepared!"
echo ""
echo "Next steps:"
echo "1. If needed, download and place JRE in the 'jre' folder (see README_JRE.md)"
echo "2. Run e.sh (macOS/Linux) or e.bat (Windows)"
echo ""
echo "Files in build directory:"
ls -lh "$SCRIPT_DIR"

