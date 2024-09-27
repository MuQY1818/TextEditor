import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.io.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

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
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            textPane.read(br, null);
        }
        this.file = file;  // 设置文件
        setModified(false); // 文件加载后，将修改状态设为false
    }

    /**
     * 保存文本编辑区域的内容到文件
     *
     * @param file File 要保存的文件
     * @throws IOException 如果写入文件发生错误
     */
    public void saveFile(File file) throws IOException {
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            textPane.write(bw);
        }
        this.file = file;  // 设置文件
        setModified(false); // 文件保存后，将修改状态设为false
    }
}
