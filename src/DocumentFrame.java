import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class DocumentFrame extends JInternalFrame {
    private static int newDocumentCounter = 1;
    private TextDocument textDocument;
    private JTextPane textPane;
    private UndoManager undoManager;
    private TextEditor parentEditor;

    public static DocumentFrame createNewDocument(TextEditor parent) {
        String title = "新文档 " + newDocumentCounter++;
        return new DocumentFrame(parent, title);
    }

    public DocumentFrame(TextEditor parent, String title) {
        super(title, true, true, true, true);
        this.parentEditor = parent;
        
        initComponents();
        
        setSize(400, 300);
        setVisible(true);
    }

    public DocumentFrame(TextEditor parent, File file) {
        super(file.getName(), true, true, true, true);
        this.parentEditor = parent;
        
        initComponents();
        loadFile(file);
        
        setSize(400, 300);
        setVisible(true);
    }

    private void initComponents() {
        textDocument = new TextDocument();
        textPane = textDocument.getTextPane();
        undoManager = new UndoManager();

        textPane.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane);

        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                closeDocument();
            }
        });
    }

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

    public JTextPane getTextPane() {
        return textPane;
    }

    public void loadFile(File file) {
        try {
            textDocument.loadFile(file);
            setTitle(file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "无法打开文件：" + e.getMessage());
        }
    }

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

    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    public void copy() {
        textPane.copy();
    }

    public void paste() {
        textPane.paste();
    }

    public void cut() {
        textPane.cut();
    }

    public void setFontFamily(String fontName) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, fontName);
        setCharacterAttributes(attrs, false);
    }

    public void setFontSize(int fontSize) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, fontSize);
        setCharacterAttributes(attrs, false);
    }

    public void setBold(boolean bold) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBold(attrs, bold);
        setCharacterAttributes(attrs, false);
    }

    public void setItalic(boolean italic) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setItalic(attrs, italic);
        setCharacterAttributes(attrs, false);
    }

    public void setUnderline(boolean underline) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setUnderline(attrs, underline);
        setCharacterAttributes(attrs, false);
    }

    public void setAlignment(int alignment) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, alignment);
        setParagraphAttributes(attrs, false);
    }

    private void setCharacterAttributes(AttributeSet attrs, boolean replace) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, replace);
        } else {
            MutableAttributeSet inputAttributes = textPane.getInputAttributes();
            inputAttributes.addAttributes(attrs);
        }
    }

    private void setParagraphAttributes(AttributeSet attrs, boolean replace) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();
        doc.setParagraphAttributes(start, end - start, attrs, replace);
    }
}