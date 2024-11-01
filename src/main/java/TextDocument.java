import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.io.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.text.BadLocationException;

/**
 * 文本文档类，用于处理文本的编辑、撤销、保存和加载
 */
public class TextDocument {
    private final JTextPane textPane; // 文本编辑区域
    private final UndoManager undoManager; // 撤销管理器
    private boolean modified; // 文档是否被修改的标志
    private File file; // 当前加载的文件

    /**
     * 构造函数，初始化文本文档
     */
    public TextDocument() {
        textPane = new JTextPane();
        undoManager = new UndoManager();
        modified = false;

        // 监听撤销编辑事件，以便更新修改状态
        textPane.getDocument().addUndoableEditListener(e -> {
            undoManager.addEdit(e.getEdit());
            setModified(true);
        });

        // 添加文档监听器，用于在文档内容变化时更新修改状态
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
            }
        });
    }

    /**
     * 获取文本编辑区域
     *
     * @return JTextPane 文本编辑区域
     */
    public JTextPane getTextPane() {
        return textPane;
    }

    /**
     * 判断文档是否被修改
     *
     * @return boolean 文档是否被修改
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * 设置文档的修改状态
     *
     * @param modified boolean 文档的修改状态
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * 获取当前加载的文件
     *
     * @return File 当前加载的文件
     */
    public File getFile() {
        return file;
    }

    /**
     * 加载文件到文本编辑区域
     *
     * @param file File 要加载的文件
     * @throws IOException 如果读取文件发生错误
     */
    public void loadFile(File file) throws IOException {
        // 检查文件扩展名
        if (file.getName().toLowerCase().endsWith(".rtf")) {
            // 加载RTF文件
            try (FileInputStream fis = new FileInputStream(file)) {
                RTFEditorKit rtfKit = new RTFEditorKit();
                textPane.setEditorKit(rtfKit);
                textPane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
                try {
                    rtfKit.read(fis, textPane.getDocument(), 0);
                } catch (BadLocationException e) {
                    throw new IOException("加载RTF文件时发生错误: " + e.getMessage());
                }
            }
        } else {
            // 普通文本文件按原方式加载
            try (FileReader fr = new FileReader(file);
                 BufferedReader br = new BufferedReader(fr)) {
                textPane.read(br, null);
            }
        }
        this.file = file;
        setModified(false);
    }

    /**
     * 保存文本编辑区域的内容到文件
     *
     * @param file File 要保存的文件
     * @throws IOException 如果写入文件发生错误
     */
    public void saveFile(File file) throws IOException {
        // 如果文件名不以.rtf结尾，自动添加.rtf扩展名
        if (!file.getName().toLowerCase().endsWith(".rtf")) {
            file = new File(file.getAbsolutePath() + ".rtf");
        }
        
        // 保存为RTF格式
        try (FileOutputStream fos = new FileOutputStream(file)) {
            RTFEditorKit rtfKit = new RTFEditorKit();
            try {
                rtfKit.write(fos, textPane.getDocument(), 0, textPane.getDocument().getLength());
            } catch (BadLocationException e) {
                throw new IOException("保存RTF文件时发生错误: " + e.getMessage());
            }
        }
        
        this.file = file;
        setModified(false);
    }
}
