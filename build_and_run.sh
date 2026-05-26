#!/bin/bash
# Digital Circuit Simulator - Build and Run Script for Linux/Mac
# This script compiles all Java files and runs the application

echo ""
echo "========================================"
echo "Digital Circuit Simulator - Build Script"
echo "========================================"
echo ""

SRC_DIR="src"
BIN_DIR="bin"
MAIN_CLASS="com.circuitsimulator.gui.MainWindow"

# Check if Java is installed
if ! command -v javac &> /dev/null; then
    echo "Error: Java compiler (javac) not found!"
    echo "Please install Java Development Kit (JDK)"
    exit 1
fi

echo "[1/3] Cleaning old build files..."
rm -rf "$BIN_DIR"/*.class "$BIN_DIR"/com

echo "[2/3] Compiling Java source files..."
javac -d "$BIN_DIR" -sourcepath "$SRC_DIR" "$SRC_DIR"/com/circuitsimulator/gui/MainWindow.java

if [ $? -ne 0 ]; then
    echo ""
    echo "Error: Compilation failed!"
    exit 1
fi

echo "[3/3] Running application..."
echo ""
java -cp "$BIN_DIR" "$MAIN_CLASS"
