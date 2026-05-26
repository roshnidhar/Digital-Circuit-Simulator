# Makefile for Digital Circuit Simulator
# This file provides convenient commands for building and running the project
# Usage: make <target>

.PHONY: all compile run clean help debug

JAVA_SRC := src
JAVA_BIN := bin
MAIN_CLASS := com.circuitsimulator.gui.MainWindow

# Default target
all: compile

# Compile only
compile:
	@echo "========================================"
	@echo "Compiling Digital Circuit Simulator"
	@echo "========================================"
	@mkdir -p $(JAVA_BIN)
	@echo "[1/2] Finding Java files..."
	@find $(JAVA_SRC) -name "*.java" -type f | wc -l | tr -d '\n'
	@echo " Java files found"
	@echo "[2/2] Compiling..."
	@javac -d $(JAVA_BIN) -sourcepath $(JAVA_SRC) $$(find $(JAVA_SRC) -name "*.java")
	@echo "Compilation complete!"

# Compile and run
run: compile
	@echo "========================================"
	@echo "Running Digital Circuit Simulator"
	@echo "========================================"
	@java -cp $(JAVA_BIN) $(MAIN_CLASS)

# Run only (assuming already compiled)
start:
	@java -cp $(JAVA_BIN) $(MAIN_CLASS)

# Check compilation
check:
	@javac -version
	@java -version

# Clean build files
clean:
	@echo "Cleaning build files..."
	@rm -rf $(JAVA_BIN)/*.class
	@find $(JAVA_BIN) -type d -name "com" -exec rm -rf {} + 2>/dev/null; true
	@echo "Clean complete!"

# Debug mode (compile with verbose output)
debug:
	@echo "Compiling in debug mode..."
	@mkdir -p $(JAVA_BIN)
	@javac -d $(JAVA_BIN) -sourcepath $(JAVA_SRC) -verbose $$(find $(JAVA_SRC) -name "*.java") 2>&1 | head -50

# Display help
help:
	@echo "Digital Circuit Simulator - Makefile"
	@echo ""
	@echo "Available targets:"
	@echo "  make compile  - Compile all Java source files"
	@echo "  make run      - Compile and run the application"
	@echo "  make start    - Run already compiled application"
	@echo "  make clean    - Remove compiled class files"
	@echo "  make check    - Check Java installation"
	@echo "  make debug    - Compile with verbose output"
	@echo "  make help     - Display this help message"
	@echo ""
	@echo "Examples:"
	@echo "  make          - Compile only (default)"
	@echo "  make run      - Compile and start the app"
	@echo "  make clean    - Remove build artifacts"

# Info target
info:
	@echo "Project Information:"
	@echo "  Source Directory: $(JAVA_SRC)"
	@echo "  Build Directory: $(JAVA_BIN)"
	@echo "  Main Class: $(MAIN_CLASS)"
	@echo ""
	@find $(JAVA_SRC) -name "*.java" -exec echo "  {}" \;
