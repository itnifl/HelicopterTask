@echo off
REM Build script for Helicopter game
REM This script builds the desktop JAR and creates a delivery ZIP

echo ========================================
echo Helicopter Game - Build Script
echo ========================================
echo.

REM Clean previous builds
echo [1/4] Cleaning previous builds...
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)

REM Build desktop JAR
echo.
echo [2/4] Building desktop JAR...
call gradlew.bat :lwjgl3:jar
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: JAR build failed!
    pause
    exit /b 1
)

REM Create dist folder
echo.
echo [3/4] Creating distribution folder...
if not exist "dist" mkdir dist
copy /Y "lwjgl3\build\libs\Helicopter-1.0.0.jar" "dist\"

REM Create ZIP file
echo.
echo [4/4] Creating delivery ZIP...
if exist "dist\Helicopter-delivery.zip" del "dist\Helicopter-delivery.zip"

REM Use PowerShell to create ZIP
powershell -Command "Compress-Archive -Path 'assets', 'core\src', 'lwjgl3\src', 'android\src', 'android\res', 'android\AndroidManifest.xml', 'android\build.gradle', 'core\build.gradle', 'lwjgl3\build.gradle', 'build.gradle', 'settings.gradle', 'gradle.properties', 'gradlew', 'gradlew.bat', 'gradle', 'README.md', 'dist\Helicopter-1.0.0.jar' -DestinationPath 'dist\Helicopter-delivery.zip' -Force"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: ZIP creation failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build completed successfully!
echo ========================================
echo.
echo Output files in dist\ folder:
echo   - Helicopter-1.0.0.jar (runnable JAR)
echo   - Helicopter-delivery.zip (source + JAR)
echo.
echo To run the game:
echo   java -jar dist\Helicopter-1.0.0.jar
echo.
pause
