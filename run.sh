#!/usr/bin/env bash
# Smart Laboratory Inventory Management - Build & Run Script
set -e
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "=========================================================="
echo "Compiling Smart Laboratory Inventory Management System..."
echo "=========================================================="
mkdir -p bin
javac -d bin -sourcepath src src/main/Main.java

echo "Compilation successful. Launching Desktop Application..."
echo "=========================================================="
java -cp bin main.Main
