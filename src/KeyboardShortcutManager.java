import javax.swing.*;
import java.awt.event.*;

public class KeyboardShortcutManager {
    private TextEditor editor;

    public KeyboardShortcutManager(TextEditor editor) {
        this.editor = editor;
    }

    public void setupShortcuts() {
        JRootPane rootPane = editor.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        setupFileShortcuts(inputMap, actionMap);
        setupEditShortcuts(inputMap, actionMap);
    }

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

    private void setupShortcut(InputMap inputMap, ActionMap actionMap, String actionName, int keyCode, int modifiers, ActionListener action) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers), actionName);
        actionMap.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(e);
            }
        });
    }

    private DocumentFrame getSelectedFrame() {
        return (DocumentFrame) editor.getDesktopPane().getSelectedFrame();
    }
}