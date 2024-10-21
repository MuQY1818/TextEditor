import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * TextEditor 类是一个基于 Swing 的多文档界面 (MDI) 文本编辑器。
 * 它支持创建新文档、打开现有文件、保存以及关闭文档等功能。
 */
public class TextEditor extends JFrame {
    // JDesktopPane 用于管理多个子窗口
    private JDesktopPane desktopPane;
    // 存储所有打开的文档框架
    private List<DocumentFrame> documentFrames;
    // 窗口管理器
    private WindowManager windowManager;

    /**
     * 构造函数初始化编辑器的基本设置和组件。
     */
    public TextEditor() {
        setTitle("多文档富文本编辑器");
        setSize(1080, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initComponents(); // 初始化组件
        setupShortcuts(); // 设置快捷键
    }

    /**
     * 初始化编辑器的主要组件。
     */
    private void initComponents() {
        setIconImage(new ImageIcon("src/icons/app_icon.png").getImage()); // 设置应用程序图标

        desktopPane = new JDesktopPane();
        desktopPane.setBorder(new BackgroundImageBorder("src/images/background_2.png", 0.5f, 0.8)); // 设置背景图片

        documentFrames = new ArrayList<>(); // 创建文档列表

        // 工具栏管理器
        ToolbarManager toolbarManager = new ToolbarManager(this); // 创建工具栏管理器
        // 菜单管理器
        MenuManager menuManager = new MenuManager(this); // 创建菜单管理器
        windowManager = new WindowManager(this, desktopPane); // 创建窗口管理器

        setJMenuBar(menuManager.createMenuBar()); // 设置菜单栏
        add(toolbarManager.createToolBar(), BorderLayout.NORTH); // 添加工具栏到北边
        add(desktopPane, BorderLayout.CENTER); // 将桌面面板添加到中心区域

        newDocument(); // 创建一个新文档
    }

    /**
     * 创建一个新的文档并将其添加到桌面面板中。
     */
    public void newDocument() {
        DocumentFrame documentFrame = DocumentFrame.createNewDocument(this); // 创建新的文档框架
        documentFrames.add(documentFrame); // 将文档框架添加到列表中
        desktopPane.add(documentFrame); // 将文档框架添加到桌面面板中
        windowManager.positionNewWindow(documentFrame); // 定位新窗口
    }

    /**
     * 获取 JDesktopPane。
     * @return JDesktopPane 实例
     */
    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    /**
     * 获取 WindowManager。
     * @return WindowManager 实例
     */
    public WindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * 移除指定的文档框架。
     * @param frame 要移除的文档框架
     */
    public void removeDocumentFrame(DocumentFrame frame) {
        documentFrames.remove(frame); // 从列表中移除文档框架
    }

    /**
     * 打开一个文件。
     */
    public void openDocument() {
        JFileChooser fileChooser = new JFileChooser(); // 创建文件选择器
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { // 如果用户选择了文件
            File file = fileChooser.getSelectedFile(); // 获取选中的文件
            DocumentFrame documentFrame = new DocumentFrame(this, file); // 创建新的文档框架
            documentFrames.add(documentFrame); // 将文档框架添加到列表中
            desktopPane.add(documentFrame); // 将文档框架添加到桌面面板中
            windowManager.positionNewWindow(documentFrame); // 定位新窗口
        }
    }

    /**
     * 保存当前文档。
     */
    public void saveDocument() {
        DocumentFrame currentFrame = (DocumentFrame) desktopPane.getSelectedFrame(); // 获取当前选中的文档框架
        if (currentFrame != null) { // 如果有选中的文档
            currentFrame.saveDocument(); // 保存文档
        }
    }

    /**
     * 另存为当前文档。
     */
    public void saveAsDocument() {
        DocumentFrame currentFrame = (DocumentFrame) desktopPane.getSelectedFrame(); // 获取当前选中的文档框架
        if (currentFrame != null) { // 如果有选中的文档
            currentFrame.saveAsDocument(); // 另存为文档
        }
    }

    /**
     * 关闭当前文档。
     */
    public void closeDocument() {
        DocumentFrame currentFrame = (DocumentFrame) desktopPane.getSelectedFrame(); // 获取当前选中的文档框架
        if (currentFrame != null) { // 如果有选中的文档
            currentFrame.closeDocument(); // 关闭文档
            documentFrames.remove(currentFrame); // 从列表中移除文档框架
        }
    }

    /**
     * 关闭所有文档。
     */
    public void closeAllDocuments() {
        for (DocumentFrame frame : new ArrayList<>(documentFrames)) { // 遍历所有文档框架
            frame.closeDocument(); // 关闭文档
            documentFrames.remove(frame); // 从列表中移除文档框架
        }
    }

    /**
     * 显示查找对话框。
     */
    public void showFindDialog() {
        new AdvancedFindDialog(this, documentFrames).setVisible(true); // 创建并显示高级查找对话框
    }

    /**
     * 设置快捷键。
     */
    private void setupShortcuts() {
        KeyboardShortcutManager shortcutManager = new KeyboardShortcutManager(this); // 创建快捷键管理器
        shortcutManager.setupShortcuts(); // 设置快捷键
    }

    /**
     * 显示AI助手对话框。
     */
    public void showAIAssistantDialog() {
        new AIAssistantDialog(this).setVisible(true);
    }

    /**
     * 主方法启动应用程序。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextEditor().setVisible(true)); // 在事件调度线程中启动编辑器
    }
}
