import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.io.*;

/**
 * DocumentFrame 类表示文本编辑器中的一个文档窗口。
 */
public class DocumentFrame extends JInternalFrame {
    // 静态计数器，用于生成新文档的标题编号
    private static int newDocumentCounter = 1;
    // 文档对象
    private TextDocument textDocument;
    // 文本窗格
    private JTextPane textPane;
    // 撤销管理器
    private UndoManager undoManager;
    // 父编辑器对象
    private final TextEditor parentEditor;

    /**
     * 创建一个新的文档窗口。
     *
     * @param parent 父编辑器
     * @return 新创建的文档窗口
     */
    public static DocumentFrame createNewDocument(TextEditor parent) {
        String title = "新文档 " + newDocumentCounter++;
        return new DocumentFrame(parent, title);
    }

    /**
     * 构造函数，用于创建一个新的文档窗口。
     *
     * @param parent 父编辑器
     * @param title  文档标题
     */
    public DocumentFrame(TextEditor parent, String title) {
        super(title, true, true, true, true);
        this.parentEditor = parent;

        initComponents();

        setSize(400, 300);
        setVisible(true);
    }

    /**
     * 构造函数，用于从文件加载文档。
     *
     * @param parent 父编辑器
     * @param file   要加载的文件
     */
    public DocumentFrame(TextEditor parent, File file) {
        super(file.getName(), true, true, true, true);
        this.parentEditor = parent;

        initComponents();
        loadFile(file);

        setSize(400, 300);
        setVisible(true);
    }

    /**
     * 初始化组件。
     */
    private void initComponents() {
        textDocument = new TextDocument();
        textPane = textDocument.getTextPane();
        undoManager = new UndoManager();

        // 添加撤销监听器
        textPane.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        // 创建滚动面板并添加文本窗格
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane);

        // 添加内部框架关闭监听器
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                closeDocument();
            }
        });
    }

    /**
     * 关闭文档窗口。
     */
    public void closeDocument() {
        if (textDocument.isModified()) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "文档已被修改，是否保存更改？",
                "关闭文档",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                parentEditor.saveDocument();
                if (textDocument.isModified()) {
                    return;
                }
            } else if (choice == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        parentEditor.removeDocumentFrame(this);
        dispose();
    }

    /**
     * 获取文本窗格。
     *
     * @return 文本窗格
     */
    public JTextPane getTextPane() {
        return textPane;
    }

    /**
     * 从文件加载文档。
     *
     * @param file 要加载的文件
     */
    public void loadFile(File file) {
        try {
            textDocument.loadFile(file);
            setTitle(file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "无法打开文件：" + e.getMessage());
        }
    }

    /**
     * 保存文档。
     */
    public void saveDocument() {
        if (textDocument.getFile() != null) {
            try {
                textDocument.saveFile(textDocument.getFile());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "无法保存文件：" + e.getMessage());
            }
        } else {
            saveAsDocument();
        }
    }

    /**
     * 另存为文档。
     */
    public void saveAsDocument() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                textDocument.saveFile(file);
                setTitle(file.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "无法保存文件：" + e.getMessage());
            }
        }
    }

    /**
     * 撤销操作。
     */
    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    /**
     * 重做操作。
     */
    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    /**
     * 复制文本。
     */
    public void copy() {
        textPane.copy();
    }

    /**
     * 粘贴文本。
     */
    public void paste() {
        textPane.paste();
    }

    /**
     * 剪切文本。
     */
    public void cut() {
        textPane.cut();
    }

    /**
     * 设置字体名称。
     *
     * @param fontName 字体名称
     */
    public void setFontFamily(String fontName) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, fontName);
        setCharacterAttributes(attrs);
    }

    /**
     * 设置字体大小。
     *
     * @param fontSize 字体大小
     */
    public void setFontSize(int fontSize) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, fontSize);
        setCharacterAttributes(attrs);
    }

    /**
     * 设置加粗样式。
     *
     * @param bold 是否加粗
     */
    public void setBold(boolean bold) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBold(attrs, bold);
        setCharacterAttributes(attrs);
    }

    /**
     * 设置斜体样式。
     *
     * @param italic 是否斜体
     */
    public void setItalic(boolean italic) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setItalic(attrs, italic);
        setCharacterAttributes(attrs);
    }

    /**
     * 设置下划线样式。
     *
     * @param underline 是否下划线
     */
    public void setUnderline(boolean underline) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setUnderline(attrs, underline);
        setCharacterAttributes(attrs);
    }

    /**
     * 设置对齐方式。
     *
     * @param alignment 对齐方式
     */
    public void setAlignment(int alignment) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, alignment);
        setParagraphAttributes(attrs);
    }

    /**
     * 设置字符属性。
     *
     * @param attrs 属性集
     */
    private void setCharacterAttributes(AttributeSet attrs) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, false);
        } else {
            MutableAttributeSet inputAttributes = textPane.getInputAttributes();
            inputAttributes.addAttributes(attrs);
        }
    }

    /**
     * 设置段落属性。
     *
     * @param attrs 属性集
     */
    private void setParagraphAttributes(AttributeSet attrs) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();
        doc.setParagraphAttributes(start, end - start, attrs, false);
    }
}
