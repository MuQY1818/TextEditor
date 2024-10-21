import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.text.StyleConstants;

public class ToolbarManager {
    // 定义TextEditor实例
    private final TextEditor editor;
    // 定义字体和字号选择框
    private JComboBox<String> fontComboBox;
    private JComboBox<String> fontSizeComboBox;
    // 定义格式按钮
    private JToggleButton boldButton, italicButton, underlineButton;

    // 构造函数，传入TextEditor实例
    public ToolbarManager(TextEditor editor) {
        this.editor = editor;
    }

    // 创建工具栏并添加各种功能按钮
    public JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false); // 禁止工具栏浮动

        // 添加文件操作按钮
        addFileOperationButtons(toolBar);
        toolBar.addSeparator(); // 添加分隔符
        // 添加编辑操作按钮
        addEditOperationButtons(toolBar);
        toolBar.addSeparator(); // 添加分隔符
        // 添加格式化按钮
        addFormatButtons(toolBar);
        toolBar.addSeparator(); // 添加分隔符
        // 添加字体控制组件
        addFontControls(toolBar);
        toolBar.addSeparator();
        addAIButton(toolBar); // 添加 AI 按钮

        return toolBar;
    }

    // 添加文件操作按钮
    private void addFileOperationButtons(JToolBar toolBar) {
        // 新建文件按钮
        toolBar.add(createButton("src/icons/new.png", "新建文件", e -> editor.newDocument()));
        // 打开文件按钮
        toolBar.add(createButton("src/icons/open.png", "打开文件", e -> editor.openDocument()));
        // 保存文件按钮
        toolBar.add(createButton("src/icons/save.png", "保存文件", e -> editor.saveDocument()));
        // 另存为按钮
        toolBar.add(createButton("src/icons/save_as.png", "另存为", e -> editor.saveAsDocument()));
        // 关闭所有文档按钮
        toolBar.add(createButton("src/icons/close_all.png", "关闭所有文档", e -> editor.closeAllDocuments()));
    }

    // 添加编辑操作按钮
    private void addEditOperationButtons(JToolBar toolBar) {
        // 复制按钮
        toolBar.add(createButton("src/icons/copy.png", "复制", e -> getSelectedFrame().copy()));
        // 粘贴按钮
        toolBar.add(createButton("src/icons/paste.png", "粘贴", e -> getSelectedFrame().paste()));
        // 剪切按钮
        toolBar.add(createButton("src/icons/cut.png", "剪切", e -> getSelectedFrame().cut()));
        // 查找按钮
        toolBar.add(createButton("src/icons/find.png", "查找", e -> editor.showFindDialog()));
        // 撤销按钮
        toolBar.add(createButton("src/icons/undo.png", "撤销", e -> getSelectedFrame().undo()));
        // 重做按钮
        toolBar.add(createButton("src/icons/redo.png", "重做", e -> getSelectedFrame().redo()));
    }

    // 添加格式化按钮
    private void addFormatButtons(JToolBar toolBar) {
        // 粗体按钮
        boldButton = createToggleButton("src/icons/bold.png", "粗体", e -> getSelectedFrame().setBold(boldButton.isSelected()));
        // 斜体按钮
        italicButton = createToggleButton("src/icons/italic.png", "斜体", e -> getSelectedFrame().setItalic(italicButton.isSelected()));
        // 下划线按钮
        underlineButton = createToggleButton("src/icons/underline.png", "下划线", e -> getSelectedFrame().setUnderline(underlineButton.isSelected()));

        // 左对齐按钮
        JToggleButton leftAlignButton = createToggleButton("src/icons/align_left.png", "左对齐", e -> getSelectedFrame().setAlignment(StyleConstants.ALIGN_LEFT));
        // 居中对齐按钮
        JToggleButton centerAlignButton = createToggleButton("src/icons/align_center.png", "居中对齐", e -> getSelectedFrame().setAlignment(StyleConstants.ALIGN_CENTER));
        // 右对齐按钮
        JToggleButton rightAlignButton = createToggleButton("src/icons/align_right.png", "右对", e -> getSelectedFrame().setAlignment(StyleConstants.ALIGN_RIGHT));

        toolBar.add(boldButton);
        toolBar.add(italicButton);
        toolBar.add(underlineButton);
        toolBar.add(leftAlignButton);
        toolBar.add(centerAlignButton);
        toolBar.add(rightAlignButton);
    }

    // 添加字体控制组件
    private void addFontControls(JToolBar toolBar) {
        // 字体选择框
        fontComboBox = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontComboBox.setMaximumSize(new Dimension(150, 25));
        fontComboBox.addActionListener(e -> getSelectedFrame().setFontFamily((String) fontComboBox.getSelectedItem()));
        toolBar.add(fontComboBox);

        // 字号选择框
        String[] fontSizes = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};
        fontSizeComboBox = new JComboBox<>(fontSizes);
        fontSizeComboBox.setMaximumSize(new Dimension(50, 25));
        fontSizeComboBox.addActionListener(e -> {
            try {
                int fontSize = Integer.parseInt((String) fontSizeComboBox.getSelectedItem());
                getSelectedFrame().setFontSize(fontSize);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(editor, "请输入有效的字体大小！");
            }
        });
        toolBar.add(fontSizeComboBox);
    }

    // 创建普通按钮
    private JButton createButton(String iconPath, String toolTipText, ActionListener action) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image scaledImage = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(scaledImage));
        button.setToolTipText(toolTipText);
        button.addActionListener(action);
        return button;
    }

    // 创建切换按钮
    private JToggleButton createToggleButton(String iconPath, String toolTipText, ActionListener action) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image scaledImage = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        JToggleButton button = new JToggleButton(new ImageIcon(scaledImage));
        button.setToolTipText(toolTipText);
        button.addActionListener(action);
        button.setPreferredSize(new Dimension(24, 24));
        return button;
    }

    // 获取当前选中的文档窗口
    private DocumentFrame getSelectedFrame() {
        return (DocumentFrame) editor.getDesktopPane().getSelectedFrame();
    }

    private void addAIButton(JToolBar toolBar) {
        JButton aiButton = new JButton("AI");
        aiButton.setToolTipText("AI助手");
        aiButton.addActionListener(e -> editor.showAIAssistantDialog());
        aiButton.setFont(new Font("Arial", Font.BOLD, 14));
        aiButton.setForeground(Color.BLUE);
        aiButton.setPreferredSize(new Dimension(40, 40));
        toolBar.add(aiButton);
    }
}
