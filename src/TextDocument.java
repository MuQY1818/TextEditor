import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.io.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

public class TextDocument {
    private JTextPane textPane;
    private UndoManager undoManager;
    private boolean modified;
    private File file;  // 新增成员变量

    public TextDocument() {
        textPane = new JTextPane();
        undoManager = new UndoManager();
        modified = false;
        
        textPane.getDocument().addUndoableEditListener(e -> {
            undoManager.addEdit(e.getEdit());
            setModified(true);
        });

        // 添加文档监听器
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

    public JTextPane getTextPane() {
        return textPane;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void loadFile(File file) throws IOException {
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            textPane.read(br, null);
        }
        this.file = file;  // 设置文件
        setModified(false);
    }

    public void saveFile(File file) throws IOException {
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            textPane.write(bw);
        }
        this.file = file;  // 设置文件
        setModified(false);
    }
}