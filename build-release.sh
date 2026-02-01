#!/bin/bash
# Build script for Helicopter & Pong Games

echo "========================================"
echo "Game Collection - Build Script"
echo "========================================"

# Clean and build
echo "[1/4] Cleaning previous builds..."
./gradlew clean

echo "[2/4] Building desktop JAR..."
./gradlew :lwjgl3:jar

# Create dist folder and copy JAR
echo "[3/4] Creating distribution folder..."
mkdir -p dist
cp lwjgl3/build/libs/Helicopter-1.0.0.jar dist/

# Create ZIP
echo "[4/4] Creating delivery ZIP..."
rm -f dist/Helicopter-delivery.zip
zip -r dist/Helicopter-delivery.zip \
    assets \
    core/src \
    lwjgl3/src \
    android/src \
    android/res \
    android/AndroidManifest.xml \
    android/build.gradle \
    core/build.gradle \
    lwjgl3/build.gradle \
    build.gradle \
    settings.gradle \
    gradle.properties \
    gradlew \
    gradlew.bat \
    gradle \
    README.md \
    dist/Helicopter-1.0.0.jar

echo ""
echo "========================================"
echo "Build completed!"
echo "========================================"
echo ""
echo "Output in dist/ folder:"
echo "  - Helicopter-1.0.0.jar (runnable JAR)"
echo "  - Helicopter-delivery.zip (source + JAR)"
echo ""
echo "Run with: java -jar dist/Helicopter-1.0.0.jar"
