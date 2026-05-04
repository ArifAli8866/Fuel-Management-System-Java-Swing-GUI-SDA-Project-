#!/bin/bash
# ============================================================
# University Fuel Management System - Compile & Run Script
# Pop!_OS / Linux with OpenJDK 17
# ============================================================

echo "========================================"
echo "  University Fuel Management System"
echo "  FAST NUCES - SDA Assignment"
echo "========================================"
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found. Install with:"
    echo "  sudo apt install openjdk-17-jdk"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -1)
echo "Java found: $JAVA_VER"

# Check javac
if ! command -v javac &> /dev/null; then
    echo "ERROR: javac not found. Install JDK (not just JRE):"
    echo "  sudo apt install openjdk-17-jdk"
    exit 1
fi

# Create output directory
mkdir -p out

echo ""
echo "Compiling..."
find src -name "*.java" | xargs javac -d out
if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Compilation failed. Check error messages above."
    exit 1
fi

echo "Compilation successful!"
echo ""
echo "Launching application..."
echo ""
java -cp out com.ufms.Main
