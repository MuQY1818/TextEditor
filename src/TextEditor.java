import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextEditor extends JFrame {
    private JDesktopPane desktopPane;
    private List<DocumentFrame> documentFrames;
    private ToolbarManager toolbarManager;
    private MenuManager menuManager;
    private WindowManager windowManager;

    public TextEditor() {
        setTitle("多文档富文本编辑器");
        setSize(1080, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initComponents();
        setupShortcuts();
    }

    private void initComponents() {
        setIconImage(new ImageIcon("src/icons/app_icon.png").getImage());

        desktopPane = new JDesktopPane();
        desktopPane.setBorder(new BackgroundImageBorder("src/images/background_2.png", 0.5f, 0.8));

        documentFrames = new ArrayList<>();

        toolbarManager = new ToolbarManager(this);
        menuManager = new MenuManager(this);
        windowManager = new WindowManager(this, desktopPane);

        setJMenuBar(menuManager.createMenuBar());
        add(toolbarManager.createToolBar(), BorderLayout.NORTH);
        add(desktopPane, BorderLayout.CENTER);

        newDocument();
    }

    public void newDocument() {
        DocumentFrame documentFrame = DocumentFrame.createNewDocument(this);
        documentFrames.add(documentFrame);
        desktopPane.add(documentFrame);
        windowManager.positionNewWindow(documentFrame);
    }

    // 添加 getDesktopPane 方法
    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    // 添加 getWindowManager 方法（如果还没有的话）
    public WindowManager getWindowManager() {
        return windowManager;
    }

    public void removeDocumentFrame(DocumentFrame frame) {
        documentFrames.remove(frame);
    }

    public void openDocument() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            DocumentFrame documentFrame = new DocumentFrame(this, file);
            documentFrames.add(documentFrame);
            desktopPane.add(documentFrame);
            windowManager.positionNewWindow(documentFrame);
        }
    }

    public void saveDocument() {
        DocumentFrame currentFrame = (DocumentFrame) desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            currentFrame.saveDocument();
        }
    }

    public void saveAsDocument() {
        DocumentFrame currentFrame = (DocumentFrame) desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            currentFrame.saveAsDocument();
        }
    }

    public void closeDocument() {
        DocumentFrame currentFrame = (DocumentFrame) desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            currentFrame.closeDocument();
            documentFrames.remove(currentFrame);
        }
    }

    public void closeAllDocuments() {
        for (DocumentFrame frame : new ArrayList<>(documentFrames)) {
            frame.closeDocument();
            documentFrames.remove(frame);
        }
    }

    public void showFindDialog() {
        new AdvancedFindDialog(this, documentFrames).setVisible(true);
    }

    private void setupShortcuts() {
        KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this);
        shortcutManager.setupShortcuts();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextEditor().setVisible(true));
    }
}