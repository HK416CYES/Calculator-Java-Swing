#!/bin/sh

set -eu

PROJECT_ROOT="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"
BUILD_DIR="$PROJECT_ROOT/build/package"
CLASSES_DIR="$BUILD_DIR/classes"
MANIFEST_FILE="$BUILD_DIR/MANIFEST.MF"
WINDOWS_DIR="$PROJECT_ROOT/release/windows/ScientificCalculator"
ZIP_FILE="$PROJECT_ROOT/release/windows/ScientificCalculator.zip"

rm -rf "$BUILD_DIR" "$WINDOWS_DIR"
mkdir -p "$CLASSES_DIR" "$WINDOWS_DIR" "$PROJECT_ROOT/release/windows"

javac -d "$CLASSES_DIR" $(find "$PROJECT_ROOT/src" -name "*.java" | sort)

cat > "$MANIFEST_FILE" <<'EOF'
Main-Class: calculator.gui.AppWindow

EOF

jar cfm "$WINDOWS_DIR/ScientificCalculator.jar" "$MANIFEST_FILE" -C "$CLASSES_DIR" .

cat > "$WINDOWS_DIR/启动科学计算器.bat" <<'EOF'
@echo off
setlocal
cd /d %~dp0
start "" javaw -jar ScientificCalculator.jar
if errorlevel 1 java -jar ScientificCalculator.jar
endlocal
EOF

cat > "$WINDOWS_DIR/README.txt" <<'EOF'
科学计算器 Windows 发布包

运行方式：
1. 双击“启动科学计算器.bat”
2. 或执行：java -jar ScientificCalculator.jar

运行环境：
- 需要安装 JRE/JDK 17 或以上版本

说明：
- 首次运行前请确认 java 或 javaw 命令可用
- 程序会自动保存历史记录和记忆值
EOF

(cd "$PROJECT_ROOT/release/windows" && zip -qr "ScientificCalculator.zip" "ScientificCalculator")

echo "Windows 发布包已生成：$WINDOWS_DIR"
echo "ZIP 文件：$ZIP_FILE"
