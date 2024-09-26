# 多文档富文本编辑器

这是一个基于 Java Swing 开发的多文档富文本编辑器。它提供了丰富的文本编辑功能，支持多个文档的同时编辑，以及各种文本格式化选项。

## 主要功能

1. 多文档管理：支持同时打开、编辑和管理多个文档。
2. 富文本编辑：提供各种文本格式化选项，如字体、大小、粗体、斜体、下划线等。
3. 文件操作：支持新建、打开、保存、另存为文档。
4. 编辑功能：包括复制、粘贴、剪切、撤销、重做等基本编辑操作。
5. 查找和替换：支持在文档中查找和替换文本。
6. 文本对齐：支持左对齐、居中对齐和右对齐。
7. 快捷键支持：为常用操作提供快捷键。
8. 窗口管理：支持窗口的层叠、水平平铺和垂直平铺排列。
9. 背景图片：支持自定义背景图片，可调整透明度和大小。
10. 文档状态跟踪：跟踪文档的修改状态，提示用户保存更改。

## 使用说明

1. 运行程序后，会自动创建一个新文档。
2. 使用顶部菜单栏或工具栏进行各种操作。
3. 可以通过内部窗口切换不同的文档。
4. 使用工具栏上的下拉菜单选择字体和字号。
5. 使用工具栏上的按钮进行文本格式化和对齐操作。
6. 通过"窗口"菜单可以调整多个文档窗口的排列方式。

## 快捷键

- Ctrl+N：新建文档
- Ctrl+O：打开文档
- Ctrl+S：保存文档
- Ctrl+Shift+S：另存为
- Ctrl+W：关闭当前文档
- Ctrl+Shift+Q：关闭所有文档
- Ctrl+Z：撤销
- Ctrl+Y：重做
- Ctrl+C：复制
- Ctrl+V：粘贴
- Ctrl+X：剪切

## 系统要求

- Java Runtime Environment (JRE) 8 或更高版本

## 如何运行

1. 确保您的系统已安装 Java Runtime Environment (JRE) 8 或更高版本。
2. 下载项目文件。
3. 在项目根目录下找到 `run_text_editor.bat` 文件。
4. 双击 `run_text_editor.bat` 文件来编译和运行程序。

如果您想手动编译和运行程序，可以按照以下步骤操作：

1. 打开命令提示符（Windows）或终端（macOS/Linux）。
2. 导航到项目的 `src` 目录。
3. 编译 Java 文件：
   ```
   javac -encoding UTF-8 TextEditor.java
   ```
4. 运行程序：
   ```
   java -Dfile.encoding=UTF-8 TextEditor
   ```

注意：使用 UTF-8 编码可以确保正确处理中文字符。

## 示例图
![屏幕截图 2024-09-26 223859](https://github.com/user-attachments/assets/511d891f-cf73-497e-a2e6-fe6e844bd8ff)

## 注意事项

- 请确保 `src/icons` 目录中包含所有必要的图标文件，否则工具栏按钮可能无法正确显示。
- 在关闭文档或退出程序时，如果文档有未保存的更改，程序会提示用户保存。
- 背景图片文件应放在 `src/images` 目录中，默认使用 `background_2.png`。

## 贡献

欢迎对此项目提出改进建议或贡献代码。请随时创建 issue 或提交 pull request。

## 许可

此项目采用 [MIT 许可证](LICENSE)。