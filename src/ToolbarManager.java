import javax.swing.*;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class ToolbarManager {
    private TextEditor editor;
    private JComboBox<String> fontComboBox;
    private JComboBox<String> fontSizeComboBox;
    private JToggleButton boldButton, italicButton, underlineButton;
    private JToggleButton leftAlignButton, centerAlignButton, rightAlignButton;

    public ToolbarManager(TextEditor editor) {
        this.editor = editor;
    }

    public JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        addFileOperationButtons(toolBar);
        toolBar.addSeparator();
        addEditOperationButtons(toolBar);
        toolBar.addSeparator();
        addFormatButtons(toolBar);
        toolBar.addSeparator();
        addFontControls(toolBar);

        return toolBar;
    }

    private void addFileOperationButtons(JToolBar toolBar) {
        toolBar.add(createButton("src/icons/new.png", "新建文件", e -> editor.newDocument()));
        toolBar.add(createButton("src/icons/open.png", "打开文件", e -> editor.openDocument()));
        toolBar.add(createButton("src/icons/save.png", "保存文件", e -> editor.saveDocument()));
        toolBar.add(createButton("src/icons/save_as.png", "另存为", e -> editor.saveAsDocument()));
        toolBar.add(createButton("src/icons/close_all.png", "关闭所有文档", e -> editor.closeAllDocuments()));
    }

    private void addEditOperationButtons(JToolBar toolBar) {
        toolBar.add(createButton("src/icons/copy.png", "复制", e -> getSelectedFrame().copy()));
        toolBar.add(createButton("src/icons/paste.png", "粘贴", e -> getSelectedFrame().paste()));
        toolBar.add(createButton("src/icons/cut.png", "剪切", e -> getSelectedFrame().cut()));
        toolBar.add(createButton("src/icons/find.png", "查找", e -> editor.showFindDialog()));
        toolBar.add(createButton("src/icons/undo.png", "撤销", e -> getSelectedFrame().undo()));
        toolBar.add(createButton("src/icons/redo.png", "重做", e -> getSelectedFrame().redo()));
    }

    private void addFormatButtons(JToolBar toolBar) {
        boldButton = createToggleButton("src/icons/bold.png", "粗体", e -> getSelectedFrame().setBold(boldButton.isSelected()));
        italicButton = createToggleButton("src/icons/italic.png", "斜体", e -> getSelectedFrame().setItalic(italicButton.isSelected()));
        underlineButton = createToggleButton("src/icons/underline.png", "下划线", e -> getSelectedFrame().setUnderline(underlineButton.isSelected()));
        
        leftAlignButton = createToggleButton("src/icons/align_left.png", "左对齐", e -> getSelectedFrame().setAlignment(StyleConstants.ALIGN_LEFT));
        centerAlignButton = createToggleButton("src/icons/align_center.png", "居中对齐", e -> getSelectedFrame().setAlignment(StyleConstants.ALIGN_CENTER));
        rightAlignButton = createToggleButton("src/icons/align_right.png", "右对齐", e -> getSelectedFrame().setAlignment(StyleConstants.ALIGN_RIGHT));

        toolBar.add(boldButton);
        toolBar.add(italicButton);
        toolBar.add(underlineButton);
        toolBar.add(leftAlignButton);
        toolBar.add(centerAlignButton);
        toolBar.add(rightAlignButton);
    }

    private void addFontControls(JToolBar toolBar) {
        fontComboBox = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontComboBox.setMaximumSize(new Dimension(150, 25));
        fontComboBox.addActionListener(e -> getSelectedFrame().setFontFamily((String) fontComboBox.getSelectedItem()));
        toolBar.add(fontComboBox);

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

    private JButton createButton(String iconPath, String toolTipText, ActionListener action) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image scaledImage = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(scaledImage));
        button.setToolTipText(toolTipText);
        button.addActionListener(action);
        button.setPreferredSize(new Dimension(24, 24));
        return button;
    }

    private JToggleButton createToggleButton(String iconPath, String toolTipText, ActionListener action) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image scaledImage = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        JToggleButton button = new JToggleButton(new ImageIcon(scaledImage));
        button.setToolTipText(toolTipText);
        button.addActionListener(action);
        button.setPreferredSize(new Dimension(24, 24));
        return button;
    }

    private DocumentFrame getSelectedFrame() {
        return (DocumentFrame) editor.getDesktopPane().getSelectedFrame();
    }
}