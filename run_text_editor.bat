@echo off
chcp 65001

echo 检查 Java 安装...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Java 未正确安装或未添加到系统路径。请安装 Java 并设置环境变量。
    pause
    exit /b 1
)

echo 编译 Java 文件...
javac -encoding UTF-8 src\TextEditor.java src\TextDocument.java
if %errorlevel% neq 0 (
    echo 编译失败，请检查错误信息。
    pause
    exit /b %errorlevel%
)

echo 运行程序...
java -Dfile.encoding=UTF-8 -cp src TextEditor
if %errorlevel% neq 0 (
    echo 程序运行失败，请检查错误信息。
    pause
    exit /b %errorlevel%
)

pause