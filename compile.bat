@echo off
REM Digital Circuit Simulator - Compile Only Script for Windows
REM This script compiles all Java files without running the application

echo.
echo ==========================================
echo Digital Circuit Simulator - Compile Script
echo ==========================================
echo.

set SRC_DIR=src
set BIN_DIR=bin

REM Check if Java compiler is installed
javac -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java compiler (javac) not found!
    echo Please install Java Development Kit (JDK)
    pause
    exit /b 1
)

REM Create bin directory if it doesn't exist
if not exist %BIN_DIR% mkdir %BIN_DIR%

echo Compiling all Java source files...

for /r %SRC_DIR% %%F in (*.java) do (
    javac -d %BIN_DIR% -sourcepath %SRC_DIR% "%%F"
)

if errorlevel 1 (
    echo.
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo Build directory: %BIN_DIR%
echo.
echo To run the application, execute:
echo   java -cp %BIN_DIR% com.circuitsimulator.gui.MainWindow
echo.
pause
