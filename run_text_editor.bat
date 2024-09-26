@echo off
chcp 65001
echo 编译 Java 文件...
javac -encoding UTF-8 src\TextEditor.java
if %errorlevel% neq 0 (
    echo 编译失败，请检查错误信息。
    pause
    exit /b %errorlevel%
)

echo 运行程序...
java -Dfile.encoding=UTF-8 -cp src TextEditor
pause
