@echo off
REM ============================================
REM EliteG - ADB Permission Grant Script
REM ============================================

REM Check if ADB is in PATH
where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: ADB is not found in your system PATH.
    echo Please install Android Platform Tools and ensure 'adb' is in your PATH.
    pause
    exit /b 1
)

echo ============================================
echo EliteG - ADB Permission Setup
echo ============================================
echo This script will grant necessary permissions to EliteG.
echo Make sure USB debugging is enabled on your device.
echo --------------------------------------------

REM Check if device is connected
echo Checking for connected devices...
adb devices | find "device$" >nul
if %ERRORLEVEL% NEQ 0 (
    echo No devices found. Please ensure:
    echo 1. USB debugging is enabled in Developer Options
    echo 2. Your device is properly connected
    echo 3. You've authorized this computer for USB debugging
    pause
    exit /b 1
)

REM Grant WRITE_SECURE_SETTINGS permission
echo Granting WRITE_SECURE_SETTINGS permission to EliteG...
adb shell pm grant com.dnagda.eliteG android.permission.WRITE_SECURE_SETTINGS

if %ERRORLEVEL% EQU 0 (
    echo Success! EliteG has been granted necessary permissions.
    echo You can now launch the app and enjoy enhanced gaming performance.
) else (
    echo Failed to grant permissions. Please ensure:
    echo 1. The app is installed on your device
    echo 2. USB debugging is properly enabled
    echo 3. You've granted USB debugging authorization
)

echo.
echo Press any key to exit...
pause >nul
