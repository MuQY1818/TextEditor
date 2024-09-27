import javax.swing.*;
import java.awt.event.*;

/**
 * 管理文本编辑器键盘快捷键的类
 */
public class KeyboardShortcutManager {
    private final TextEditor editor;

    /**
     * 构造函数，初始化键盘快捷键管理器所需的文本编辑器实例
     *
     * @param editor 文本编辑器实例
     */
    public KeyboardShortcutManager(TextEditor editor) {
        this.editor = editor;
    }

    /**
     * 设置键盘快捷键
     */
    public void setupShortcuts() {
        JRootPane rootPane = editor.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        setupFileShortcuts(inputMap, actionMap);
        setupEditShortcuts(inputMap, actionMap);
    }

    /**
     * 设置文件操作相关的键盘快捷键
     *
     * @param inputMap  输入映射，用于将按键事件映射到动作
     * @param actionMap 动作映射，用于存储动作
     */
    private void setupFileShortcuts(InputMap inputMap, ActionMap actionMap) {
        setupShortcut(inputMap, actionMap, "newDocument", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK,
            e -> editor.newDocument());
        setupShortcut(inputMap, actionMap, "openDocument", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK,
            e -> editor.openDocument());
        setupShortcut(inputMap, actionMap, "saveDocument", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK,
            e -> editor.saveDocument());
        setupShortcut(inputMap, actionMap, "saveAsDocument", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
            e -> editor.saveAsDocument());
        setupShortcut(inputMap, actionMap, "closeDocument", KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK,
            e -> editor.closeDocument());
        setupShortcut(inputMap, actionMap, "closeAllDocuments", KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
            e -> editor.closeAllDocuments());
    }

    /**
     * 设置编辑操作相关的键盘快捷键
     *
     * @param inputMap  输入映射，用于将按键事件映射到动作
     * @param actionMap 动作映射，用于存储动作
     */
    private void setupEditShortcuts(InputMap inputMap, ActionMap actionMap) {
        setupShortcut(inputMap, actionMap, "undo", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK,
            e -> getSelectedFrame().undo());
        setupShortcut(inputMap, actionMap, "redo", KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK,
            e -> getSelectedFrame().redo());
        setupShortcut(inputMap, actionMap, "copy", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK,
            e -> getSelectedFrame().copy());
        setupShortcut(inputMap, actionMap, "paste", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK,
            e -> getSelectedFrame().paste());
        setupShortcut(inputMap, actionMap, "cut", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK,
            e -> getSelectedFrame().cut());
        setupShortcut(inputMap, actionMap, "find", KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK,
            e -> editor.showFindDialog());
    }

    /**
     * 设置一个键盘快捷键的映射和动作
     *
     * @param inputMap  输入映射，用于将按键事件映射到动作
     * @param actionMap 动作映射，用于存储动作
     * @param actionName 动作名称
     * @param keyCode   键盘按键代码
     * @param modifiers 键盘修饰符
     * @param action    键盘动作的监听器
     */
    private void setupShortcut(InputMap inputMap, ActionMap actionMap, String actionName, int keyCode, int modifiers, ActionListener action) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers), actionName);
        actionMap.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(e);
            }
        });
    }

    /**
     * 获取当前选中的文档框架
     *
     * @return 当前选中的文档框架，如果没有选中则返回null
     */
    private DocumentFrame getSelectedFrame() {
        return (DocumentFrame) editor.getDesktopPane().getSelectedFrame();
    }
}
