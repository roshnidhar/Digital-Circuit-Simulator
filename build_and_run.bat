@echo off
echo ========================================
echo Digital Circuit Simulator - Build Script
echo ========================================

echo [1/3] Cleaning old build files...
if exist bin\*.class del /s /q bin\*.class >nul 2>&1

echo [2/3] Compiling Java source files...
javac -d bin -sourcepath src src\com\circuitsimulator\gui\MainWindow.java

echo [3/3] Running application...
java -cp bin com.circuitsimulator.gui.MainWindow

pause