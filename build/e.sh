#!/bin/bash

# Script to run e.jar on macOS/Linux
# Works even if Java is not in PATH

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$SCRIPT_DIR/e.jar"
JRE_DIR="$SCRIPT_DIR/jre"

# Check if JRE is bundled (macOS has different structure)
OS_NAME=$(uname -s)
if [ "$OS_NAME" = "Darwin" ]; then
    # macOS JRE structure
    if [ -f "$JRE_DIR/Contents/Home/bin/java" ]; then
        JAVA_CMD="$JRE_DIR/Contents/Home/bin/java"
        echo "Using bundled JRE (macOS)..."
    elif [ -f "$JRE_DIR/bin/java" ]; then
        JAVA_CMD="$JRE_DIR/bin/java"
        echo "Using bundled JRE..."
    else
        JAVA_CMD=$(which java 2>/dev/null)
        if [ -z "$JAVA_CMD" ]; then
            echo "Error: Java not found. Please install Java or use bundled JRE."
            exit 1
        fi
        echo "Using system Java: $JAVA_CMD"
    fi
else
    # Linux JRE structure
    if [ -f "$JRE_DIR/bin/java" ]; then
        JAVA_CMD="$JRE_DIR/bin/java"
        echo "Using bundled JRE..."
    else
        JAVA_CMD=$(which java 2>/dev/null)
        if [ -z "$JAVA_CMD" ]; then
            echo "Error: Java not found. Please install Java or use bundled JRE."
            exit 1
        fi
        echo "Using system Java: $JAVA_CMD"
    fi
fi

# Run the application
if [ -f "$JAR_FILE" ]; then
    echo "Starting application..."
    "$JAVA_CMD" -jar "$JAR_FILE"
else
    echo "Error: e.jar not found in $SCRIPT_DIR"
    exit 1
fi

