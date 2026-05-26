#!/bin/bash
# Digital Circuit Simulator - Compile Only Script
# This script compiles all Java files without running the application

echo ""
echo "=========================================="
echo "Digital Circuit Simulator - Compile Script"
echo "=========================================="
echo ""

SRC_DIR="src"
BIN_DIR="bin"

# Check if Java compiler is installed
if ! command -v javac &> /dev/null; then
    echo "Error: Java compiler (javac) not found!"
    echo "Please install Java Development Kit (JDK)"
    exit 1
fi

# Create bin directory if it doesn't exist
mkdir -p "$BIN_DIR"

echo "Compiling all Java source files..."
javac -d "$BIN_DIR" -sourcepath "$SRC_DIR" \
    "$SRC_DIR"/com/circuitsimulator/gui/*.java \
    "$SRC_DIR"/com/circuitsimulator/circuit/*.java \
    "$SRC_DIR"/com/circuitsimulator/components/*.java \
    "$SRC_DIR"/com/circuitsimulator/validator/*.java \
    "$SRC_DIR"/com/circuitsimulator/file/*.java

if [ $? -eq 0 ]; then
    echo ""
    echo "Compilation successful!"
    echo "Build directory: $BIN_DIR/"
    echo ""
    echo "To run the application, execute:"
    echo "  java -cp $BIN_DIR com.circuitsimulator.gui.MainWindow"
else
    echo ""
    echo "Compilation failed!"
    exit 1
fi
