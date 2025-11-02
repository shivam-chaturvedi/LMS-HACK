#!/bin/bash

echo "Building JAR file with all dependencies..."
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful! JAR file created:"
    echo "target/lms-project-1.0-SNAPSHOT-jar-with-dependencies.jar"
    echo ""
    echo "To run the application, use:"
    echo "java -jar target/lms-project-1.0-SNAPSHOT-jar-with-dependencies.jar"
else
    echo "Build failed!"
    exit 1
fi
