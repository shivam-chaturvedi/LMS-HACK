#!/bin/bash

# Script to download and set up JRE in the build directory
# This downloads JRE 11 for the current platform

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JRE_DIR="$SCRIPT_DIR/jre"
OS_NAME=$(uname -s)
ARCH=$(uname -m)

echo "Downloading JRE 11 for $OS_NAME ($ARCH)..."
echo ""

# Detect platform
if [ "$OS_NAME" = "Darwin" ]; then
    if [[ "$ARCH" == "arm64" ]]; then
        PLATFORM="mac"
        ARCH_TYPE="aarch64"
        EXT="tar.gz"
        JRE_PATH="jre/Contents/Home/bin/java"
    else
        PLATFORM="mac"
        ARCH_TYPE="x64"
        EXT="tar.gz"
        JRE_PATH="jre/Contents/Home/bin/java"
    fi
elif [ "$OS_NAME" = "Linux" ]; then
    PLATFORM="linux"
    if [[ "$ARCH" == "x86_64" ]]; then
        ARCH_TYPE="x64"
    else
        ARCH_TYPE="x64"  # Default to x64
    fi
    EXT="tar.gz"
    JRE_PATH="jre/bin/java"
else
    echo "Unsupported OS: $OS_NAME"
    echo "Please download JRE manually from: https://adoptium.net/temurin/releases/"
    exit 1
fi

# Adoptium/Eclipse Temurin download URL (using API)
BASE_URL="https://api.adoptium.net/v3/binary/latest/11/ga/${PLATFORM}/${ARCH_TYPE}/jre/hotspot/normal/eclipse"
DOWNLOAD_URL="${BASE_URL}?project=jdk"

echo "Download URL: $DOWNLOAD_URL"
echo ""
echo "Downloading JRE 11 (this may take a few minutes)..."
echo ""

# Download
TEMP_FILE=$(mktemp)
if command -v curl >/dev/null 2>&1; then
    curl -L -o "$TEMP_FILE" "$DOWNLOAD_URL"
elif command -v wget >/dev/null 2>&1; then
    wget -O "$TEMP_FILE" "$DOWNLOAD_URL"
else
    echo "Error: Neither curl nor wget found. Please install one of them."
    exit 1
fi

if [ ! -f "$TEMP_FILE" ] || [ ! -s "$TEMP_FILE" ]; then
    echo "Error: Download failed!"
    exit 1
fi

echo ""
echo "Extracting JRE..."
cd "$SCRIPT_DIR"

# Remove old jre if exists
rm -rf "$JRE_DIR"

# Extract
if [ "$EXT" = "tar.gz" ]; then
    tar -xzf "$TEMP_FILE"
    # Find the extracted folder (usually starts with jdk-11)
    EXTRACTED_DIR=$(find . -maxdepth 1 -type d -name "jdk-11*" -o -name "jre-11*" | head -1)
    if [ -n "$EXTRACTED_DIR" ]; then
        mv "$EXTRACTED_DIR" "jre"
    else
        # Try OpenJDK naming
        EXTRACTED_DIR=$(find . -maxdepth 1 -type d -name "*11*" | grep -E "(jdk|jre)" | head -1)
        if [ -n "$EXTRACTED_DIR" ]; then
            mv "$EXTRACTED_DIR" "jre"
        else
            echo "Error: Could not find extracted JRE directory"
            rm -f "$TEMP_FILE"
            exit 1
        fi
    fi
fi

# Clean up
rm -f "$TEMP_FILE"

# Verify
VERIFIED=false
if [ -f "$JRE_DIR/$JRE_PATH" ]; then
    VERIFIED=true
    JAVA_VERIFY="$JRE_DIR/$JRE_PATH"
elif [ -f "$JRE_DIR/bin/java" ]; then
    VERIFIED=true
    JAVA_VERIFY="$JRE_DIR/bin/java"
elif [ -f "$JRE_DIR/Contents/Home/bin/java" ]; then
    VERIFIED=true
    JAVA_VERIFY="$JRE_DIR/Contents/Home/bin/java"
fi

if [ "$VERIFIED" = true ]; then
    echo ""
    echo "✓ JRE successfully downloaded and extracted to: $JRE_DIR"
    echo ""
    echo "Verification:"
    "$JAVA_VERIFY" -version
else
    echo "Error: JRE extraction verification failed"
    echo "Please check $JRE_DIR directory"
    exit 1
fi

echo ""
echo "JRE setup complete!"

