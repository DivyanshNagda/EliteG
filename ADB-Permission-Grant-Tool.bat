@echo off
REM ============================================
REM EliteG - ADB Permission Grant & ADB Mode Tool
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
echo EliteG - ADB Permission & Mode Setup
echo ============================================
echo This script will grant necessary permissions and activate ADB Mode for EliteG.
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

echo Granting WRITE_SECURE_SETTINGS permission to EliteG...
adb shell pm grant com.dnagda.eliteG android.permission.WRITE_SECURE_SETTINGS

if %ERRORLEVEL% EQU 0 (
    echo Success! EliteG has been granted necessary permissions.
) else (
    echo Failed to grant permissions. Please check device connection and try again.
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)

echo Copying ADB toolkit files to device storage...
adb push app\src\main\assets\daemon /storage/emulated/0/Android/data/com.dnagda.eliteG/daemon
adb push app\src\main\assets\toolkit\toybox-outside64 /storage/emulated/0/Android/data/com.dnagda.eliteG/toolkit/toybox-outside64
adb push app\src\main\assets\toolkit\busybox /storage/emulated/0/Android/data/com.dnagda.eliteG/toolkit/busybox
adb push app\src\main\assets\up.sh /storage/emulated/0/Android/data/com.dnagda.eliteG/up.sh

echo Activating ADB Mode for EliteG...
adb shell sh /storage/emulated/0/Android/data/com.dnagda.eliteG/up.sh

echo.
echo EliteG ADB Mode setup complete!
echo Press any key to exit...
pause >nul
