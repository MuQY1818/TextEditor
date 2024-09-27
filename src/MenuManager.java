import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.StyleConstants;

public class MenuManager {
    private TextEditor editor;

    public MenuManager(TextEditor editor) {
        this.editor = editor;
    }

    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createFormatMenu());
        menuBar.add(createWindowMenu());
        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("文件");
        fileMenu.add(createMenuItem("新建文档", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, e -> editor.newDocument()));
        fileMenu.add(createMenuItem("打开文档", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, e -> editor.openDocument()));
        fileMenu.add(createMenuItem("保存文档", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, e -> editor.saveDocument()));
        fileMenu.add(createMenuItem("另存为", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, e -> editor.saveAsDocument()));
        fileMenu.add(createMenuItem("关闭文档", KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK, e -> editor.closeDocument()));
        fileMenu.add(createMenuItem("关闭所有文档", KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, e -> editor.closeAllDocuments()));
        return fileMenu;
    }

    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("编辑");
        editMenu.add(createMenuItem("撤销", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK, e -> getSelectedFrame().undo()));
        editMenu.add(createMenuItem("重做", KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK, e -> getSelectedFrame().redo()));
        editMenu.add(createMenuItem("复制", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, e -> getSelectedFrame().copy()));
        editMenu.add(createMenuItem("粘贴", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, e -> getSelectedFrame().paste()));
        editMenu.add(createMenuItem("剪切", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK, e -> getSelectedFrame().cut()));
        editMenu.add(createMenuItem("查找", KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK, e -> editor.showFindDialog()));
        return editMenu;
    }

    private JMenu createFormatMenu() {
        JMenu formatMenu = new JMenu("格式");
        formatMenu.add(createMenuItem("设置粗体", e -> getSelectedFrame().setBold(true)));
        formatMenu.add(createMenuItem("设置斜体", e -> getSelectedFrame().setItalic(true)));
        formatMenu.add(createMenuItem("设置下划线", e -> getSelectedFrame().setUnderline(true)));
        formatMenu.add(createMenuItem("左对齐", e -> getSelectedFrame().setAlignment(StyleConstants.ALIGN_LEFT)));
        formatMenu.add(createMenuItem("居中对齐", e -> getSelectedFrame().setAlignment(StyleConstants.ALIGN_CENTER)));
        formatMenu.add(createMenuItem("右对齐", e -> getSelectedFrame().setAlignment(StyleConstants.ALIGN_RIGHT)));
        return formatMenu;
    }

    private JMenu createWindowMenu() {
        JMenu windowMenu = new JMenu("窗口");
        windowMenu.add(createMenuItem("窗口层叠", e -> editor.getWindowManager().cascadeWindows()));
        windowMenu.add(createMenuItem("水平平铺", e -> editor.getWindowManager().tileWindowsHorizontally()));
        windowMenu.add(createMenuItem("垂直平铺", e -> editor.getWindowManager().tileWindowsVertically()));
        return windowMenu;
    }

    private JMenuItem createMenuItem(String title, int keyCode, int modifiers, ActionListener action) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        menuItem.addActionListener(action);
        return menuItem;
    }

    private JMenuItem createMenuItem(String title, ActionListener action) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(action);
        return menuItem;
    }

    private DocumentFrame getSelectedFrame() {
        return (DocumentFrame) editor.getDesktopPane().getSelectedFrame();
    }
}