#!/bin/bash

# Script to download Tesseract OCR tessdata from GitHub
# This downloads the English language data files

TESSDATA_DIR="tessdata"
TESSDATA_URL="https://github.com/tesseract-ocr/tessdata/raw/main"

# Create tessdata directory if it doesn't exist
mkdir -p "$TESSDATA_DIR"

echo "Downloading Tesseract OCR language data files..."
echo "This may take a few minutes..."

# Download English language data file (required)
echo "Downloading eng.traineddata (English)..."
curl -L -o "$TESSDATA_DIR/eng.traineddata" "$TESSDATA_URL/eng.traineddata"

if [ $? -eq 0 ]; then
    echo "Successfully downloaded eng.traineddata"
    echo "File size: $(ls -lh $TESSDATA_DIR/eng.traineddata | awk '{print $5}')"
    echo ""
    echo "Tessdata setup complete!"
    echo "The tessdata directory is ready to use."
else
    echo "Error: Failed to download tessdata files"
    exit 1
fi

