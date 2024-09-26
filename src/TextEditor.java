import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.undo.UndoManager;
import javax.swing.border.EmptyBorder;

/**
 * 多文档富文本编辑器主类
 */
public class TextEditor extends JFrame {
    private JTabbedPane tabbedPane;  // 用于管理多个文档的标签页面板
    private ArrayList<TextDocument> documents;  // 存储所有打开的文档
    private JComboBox<String> fontComboBox;  // 字体选择下拉框
    private JComboBox<String> fontSizeComboBox;  // 字体大小选择下拉框
    private JToggleButton boldButton, italicButton, underlineButton;  // 粗体、斜体、下划线按钮
    private JToggleButton leftAlignButton, centerAlignButton, rightAlignButton;  // 对齐方式按钮

    /**
     * 构造函数，初始化编辑器界面
     */
    public TextEditor() {
        setTitle("多文档富文本编辑器");
        setSize(1080, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 设置应用程序图标
        ImageIcon icon = new ImageIcon("src/icons/app_icon.png");
        setIconImage(icon.getImage());

        documents = new ArrayList<>();
        tabbedPane = new JTabbedPane();
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {  // 鼠标中键点击关闭标签页
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex != -1) {
                        closeDocument(tabIndex);
                    }
                }
            }
        });

        createMenuBar();  // 创建菜单栏
        createToolBar();  // 创建工具栏

        add(tabbedPane, BorderLayout.CENTER);

        // 创建一个新文档作为初始标签页
        newDocument();

        setupShortcuts();  // 设置快捷键
    }

    /**
     * 创建菜单栏
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.add(createMenuItem("新建文档", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, e -> newDocument()));
        fileMenu.add(createMenuItem("打开文档", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, e -> openDocument()));
        fileMenu.add(createMenuItem("保存文档", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, e -> saveDocument()));
        fileMenu.add(createMenuItem("关闭文档", KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK, e -> closeDocument(tabbedPane.getSelectedIndex())));
        menuBar.add(fileMenu);

        // 编辑菜单
        JMenu editMenu = new JMenu("编辑");
        editMenu.add(createMenuItem("撤销", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK, e -> undo()));
        editMenu.add(createMenuItem("重做", KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK, e -> redo()));
        editMenu.add(createMenuItem("复制", e -> copy()));
        editMenu.add(createMenuItem("粘贴", e -> paste()));
        editMenu.add(createMenuItem("剪切", e -> cut()));
        editMenu.add(createMenuItem("查找", e -> findText()));
        editMenu.add(createMenuItem("替换", e -> replaceText()));
        menuBar.add(editMenu);

        // 格式菜单
        JMenu formatMenu = new JMenu("格式");
        formatMenu.add(createMenuItem("设置粗体", e -> setBold()));
        formatMenu.add(createMenuItem("设置斜体", e -> setItalic()));
        formatMenu.add(createMenuItem("设置下划线", e -> setUnderline()));
        formatMenu.add(createMenuItem("设置字体", e -> setFont()));
        formatMenu.add(createMenuItem("左对齐", e -> setAlignment(StyleConstants.ALIGN_LEFT)));
        formatMenu.add(createMenuItem("居中对齐", e -> setAlignment(StyleConstants.ALIGN_CENTER)));
        formatMenu.add(createMenuItem("右对齐", e -> setAlignment(StyleConstants.ALIGN_RIGHT)));
        menuBar.add(formatMenu);

        setJMenuBar(menuBar);
    }

    /**
     * 创建带快捷键的菜单项
     */
    private JMenuItem createMenuItem(String title, int keyCode, int modifiers, ActionListener action) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        menuItem.addActionListener(action);
        return menuItem;
    }

    /**
     * 创建普通菜单项
     */
    private JMenuItem createMenuItem(String title, ActionListener action) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(action);
        return menuItem;
    }

    /**
     * 创建新文档
     */
    private void newDocument() {
        TextDocument doc = new TextDocument();
        documents.add(doc);
        JScrollPane scrollPane = new JScrollPane(doc.getTextPane());
        tabbedPane.addTab("新文档 " + documents.size(), scrollPane);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setSelectedIndex(index);
        tabbedPane.setTabComponentAt(index, new TabComponent(tabbedPane));
    }

    /**
     * 关闭指定索引的文档
     */
    private void closeDocument(int index) {
        if (index != -1) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "是否保存更改？",
                "关闭文档",
                JOptionPane.YES_NO_CANCEL_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                saveDocument();
            }
            if (choice != JOptionPane.CANCEL_OPTION) {
                tabbedPane.remove(index);
                documents.remove(index);
                if (tabbedPane.getTabCount() == 0) {
                    newDocument();
                }
            }
        }
    }

    /**
     * 打开文档
     */
    private void openDocument() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            TextDocument doc = new TextDocument();
            try {
                doc.loadFile(file);
                documents.add(doc);
                tabbedPane.addTab(file.getName(), new JScrollPane(doc.getTextPane()));
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "无法打开文件：" + e.getMessage());
            }
        }
    }

    /**
     * 保存当前文档
     */
    private void saveDocument() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            TextDocument currentDoc = documents.get(currentIndex);
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    currentDoc.saveFile(file);
                    tabbedPane.setTitleAt(currentIndex, file.getName());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "无法保存文件：" + e.getMessage());
                }
            }
        }
    }

    /**
     * 撤销操作
     */
    private void undo() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            TextDocument currentDoc = documents.get(currentIndex);
            if (currentDoc.getUndoManager().canUndo()) {
                currentDoc.getUndoManager().undo();
            }
        }
    }

    /**
     * 重做操作
     */
    private void redo() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            TextDocument currentDoc = documents.get(currentIndex);
            if (currentDoc.getUndoManager().canRedo()) {
                currentDoc.getUndoManager().redo();
            }
        }
    }

    /**
     * 复制操作
     */
    private void copy() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            TextDocument currentDoc = documents.get(currentIndex);
            currentDoc.getTextPane().copy();
        }
    }

    /**
     * 粘贴操作
     */
    private void paste() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            TextDocument currentDoc = documents.get(currentIndex);
            currentDoc.getTextPane().paste();
        }
    }

    /**
     * 剪切操作
     */
    private void cut() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            TextDocument currentDoc = documents.get(currentIndex);
            currentDoc.getTextPane().cut();
        }
    }

    /**
     * 设置粗体
     */
    private void setBold() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            JTextPane currentTextPane = documents.get(currentIndex).getTextPane();
            MutableAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setBold(attrs, boldButton.isSelected());
            setCharacterAttributes(currentTextPane, attrs, false);
        }
    }

    /**
     * 设置斜体
     */
    private void setItalic() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            JTextPane currentTextPane = documents.get(currentIndex).getTextPane();
            MutableAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setItalic(attrs, italicButton.isSelected());
            setCharacterAttributes(currentTextPane, attrs, false);
        }
    }

    /**
     * 设置下划线
     */
    private void setUnderline() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            JTextPane currentTextPane = documents.get(currentIndex).getTextPane();
            MutableAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setUnderline(attrs, underlineButton.isSelected());
            setCharacterAttributes(currentTextPane, attrs, false);
        }
    }

    /**
     * 设置字体
     */
    private void setFont() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            JTextPane currentTextPane = documents.get(currentIndex).getTextPane();
            String fontName = JOptionPane.showInputDialog("输入字体名称：");
            String fontSizeStr = JOptionPane.showInputDialog("输入字体大小：");
            if (fontName != null && fontSizeStr != null) {
                try {
                    int fontSize = Integer.parseInt(fontSizeStr);
                    Style style = currentTextPane.addStyle("FontStyle", null);
                    StyleConstants.setFontFamily(style, fontName);
                    StyleConstants.setFontSize(style, fontSize);
                    int start = currentTextPane.getSelectionStart();
                    int end = currentTextPane.getSelectionEnd();
                    if (start != end) {
                        currentTextPane.setCharacterAttributes(style, false);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "请输入有效的字体大小！");
                }
            }
        }
    }

    /**
     * 设置对齐方式
     */
    private void setAlignment(int alignment) {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            JTextPane currentTextPane = documents.get(currentIndex).getTextPane();
            StyledDocument doc = currentTextPane.getStyledDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setAlignment(attrs, alignment);
            doc.setParagraphAttributes(currentTextPane.getSelectionStart(), currentTextPane.getSelectionEnd() - currentTextPane.getSelectionStart(), attrs, false);
        }
    }

    /**
     * 查找文本
     */
    private void findText() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            JTextPane currentTextPane = documents.get(currentIndex).getTextPane();
            String searchText = JOptionPane.showInputDialog("输入查找的文本：");
            if (searchText != null) {
                String content = currentTextPane.getText();
                int index = content.indexOf(searchText);
                if (index >= 0) {
                    currentTextPane.select(index, index + searchText.length());
                } else {
                    JOptionPane.showMessageDialog(this, "未找到文本！");
                }
            }
        }
    }

    /**
     * 替换文本
     */
    private void replaceText() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            JTextPane currentTextPane = documents.get(currentIndex).getTextPane();
            String searchText = JOptionPane.showInputDialog("输入查找的文本：");
            String replaceText = JOptionPane.showInputDialog("输入替换的文本：");
            if (searchText != null && replaceText != null) {
                String content = currentTextPane.getText();
                content = content.replace(searchText, replaceText);
                currentTextPane.setText(content);
            }
        }
    }

    /**
     * 设置字符属性
     */
    private void setCharacterAttributes(JTextPane pane, AttributeSet attrs, boolean replace) {
        int start = pane.getSelectionStart();
        int end = pane.getSelectionEnd();
        if (start != end) {
            StyledDocument doc = pane.getStyledDocument();
            doc.setCharacterAttributes(start, end - start, attrs, replace);
        }
    }

    /**
     * 主方法，启动应用程序
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TextEditor editor = new TextEditor();
            editor.setVisible(true);
        });
    }

    /**
     * 内部类：表示单个文本文档
     */
    private class TextDocument {
        private JTextPane textPane;
        private UndoManager undoManager;

        public TextDocument() {
            textPane = new JTextPane();
            undoManager = new UndoManager();
            textPane.getDocument().addUndoableEditListener(undoManager);
        }

        public JTextPane getTextPane() {
            return textPane;
        }

        public UndoManager getUndoManager() {
            return undoManager;
        }

        /**
         * 从文件加载内容
         */
        public void loadFile(File file) throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                textPane.setText("");
                String line;
                while ((line = br.readLine()) != null) {
                    textPane.getDocument().insertString(textPane.getDocument().getLength(), line + "\n", null);
                }
            } catch (BadLocationException e) {
                throw new IOException("文件读取错误", e);
            }
        }

        /**
         * 保存内容到文件
         */
        public void saveFile(File file) throws IOException {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(textPane.getText());
            }
        }
    }

    /**
     * 创建工具栏
     */
    private void createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        // 新建、打开、保存按钮
        toolBar.add(createToolBarButton("src/icons/new.png", e -> newDocument(), "新建文档"));
        toolBar.add(createToolBarButton("src/icons/open.png", e -> openDocument(), "打开文档"));
        toolBar.add(createToolBarButton("src/icons/save.png", e -> saveDocument(), "保存文档"));

        toolBar.addSeparator();

        // 复制、剪切、粘贴按钮
        toolBar.add(createToolBarButton("src/icons/copy.png", e -> copy(), "复制"));
        toolBar.add(createToolBarButton("src/icons/cut.png", e -> cut(), "剪切"));
        toolBar.add(createToolBarButton("src/icons/paste.png", e -> paste(), "粘贴"));

        toolBar.addSeparator();

        // 撤销、重做按钮
        toolBar.add(createToolBarButton("src/icons/undo.png", e -> undo(), "撤销"));
        toolBar.add(createToolBarButton("src/icons/redo.png", e -> redo(), "重做"));

        toolBar.addSeparator();

        // 查找、替换按钮
        toolBar.add(createToolBarButton("src/icons/find.png", e -> findText(), "查找"));
        toolBar.add(createToolBarButton("src/icons/replace.png", e -> replaceText(), "替换"));

        toolBar.addSeparator();

        // 字体选择
        toolBar.add(new JLabel(createScaledImageIcon("src/icons/font.png", 24, 24)));
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontComboBox = new JComboBox<>(fonts);
        fontComboBox.setEditable(true);
        fontComboBox.setMaximumSize(new Dimension(150, 30));
        fontComboBox.addActionListener(e -> setSelectedFont());
        toolBar.add(fontComboBox);

        // 字体大小选择
        toolBar.add(new JLabel(createScaledImageIcon("src/icons/font_size.png", 24, 24)));
        String[] sizes = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};
        fontSizeComboBox = new JComboBox<>(sizes);
        fontSizeComboBox.setEditable(true);
        fontSizeComboBox.setMaximumSize(new Dimension(80, 30));
        fontSizeComboBox.addActionListener(e -> setSelectedFontSize());
        toolBar.add(fontSizeComboBox);

        toolBar.addSeparator();

        // 加粗、斜体、下划线按钮
        boldButton = createToolBarToggleButton("src/icons/bold.png", e -> setBold(), "粗体");
        toolBar.add(boldButton);
        italicButton = createToolBarToggleButton("src/icons/italic.png", e -> setItalic(), "斜体");
        toolBar.add(italicButton);
        underlineButton = createToolBarToggleButton("src/icons/underline.png", e -> setUnderline(), "下划线");
        toolBar.add(underlineButton);

        toolBar.addSeparator();

        // 对齐方式按钮
        leftAlignButton = createToolBarToggleButton("src/icons/align_left.png", e -> setAlignment(StyleConstants.ALIGN_LEFT), "左对齐");
        toolBar.add(leftAlignButton);
        centerAlignButton = createToolBarToggleButton("src/icons/align_center.png", e -> setAlignment(StyleConstants.ALIGN_CENTER), "居中对齐");
        toolBar.add(centerAlignButton);
        rightAlignButton = createToolBarToggleButton("src/icons/align_right.png", e -> setAlignment(StyleConstants.ALIGN_RIGHT), "右对齐");
        toolBar.add(rightAlignButton);

        // 创建一个按钮组，确保只有一个对齐按钮被选中
        ButtonGroup alignmentGroup = new ButtonGroup();
        alignmentGroup.add(leftAlignButton);
        alignmentGroup.add(centerAlignButton);
        alignmentGroup.add(rightAlignButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private JButton createToolBarButton(String iconPath, ActionListener action, String toolTipText) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(img));
        button.setFocusPainted(false);
        button.addActionListener(action);
        button.setToolTipText(toolTipText);
        return button;
    }

    private JToggleButton createToolBarToggleButton(String iconPath, ActionListener action, String toolTipText) {
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        JToggleButton button = new JToggleButton(new ImageIcon(img));
        button.setFocusPainted(false);
        button.addActionListener(action);
        button.setToolTipText(toolTipText);
        return button;
    }

    private ImageIcon createScaledImageIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    /**
     * 设置选中的字体
     */
    private void setSelectedFont() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            JTextPane currentTextPane = documents.get(currentIndex).getTextPane();
            String fontName = (String) fontComboBox.getSelectedItem();
            setFontFamily(currentTextPane, fontName);
        }
    }

    /**
     * 设置选中的字体大小
     */
    private void setSelectedFontSize() {
        int currentIndex = tabbedPane.getSelectedIndex();
        if (currentIndex != -1) {
            JTextPane currentTextPane = documents.get(currentIndex).getTextPane();
            try {
                int fontSize = Integer.parseInt((String) fontSizeComboBox.getSelectedItem());
                setFontSize(currentTextPane, fontSize);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "请输入有效的字体大小！");
            }
        }
    }

    /**
     * 获取当前字体名称
     */
    private String getCurrentFontFamily(JTextPane textPane) {
        Font font = textPane.getFont();
        return font != null ? font.getFamily() : Font.SANS_SERIF;
    }

    /**
     * 获取当前字体大小
     */
    private int getCurrentFontSize(JTextPane textPane) {
        Font font = textPane.getFont();
        return font != null ? font.getSize() : 12;
    }

    /**
     * 设置字体名称
     */
    private void setFontFamily(JTextPane textPane, String fontName) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, fontName);
        setCharacterAttributes(textPane, attrs, false);
    }

    /**
     * 设置字体大小
     */
    private void setFontSize(JTextPane textPane, int fontSize) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, fontSize);
        setCharacterAttributes(textPane, attrs, false);
    }

    /**
     * 内部类：自定义标签页组件
     */
    private class TabComponent extends JPanel {
        private final JLabel label;
        private final CloseButton closeButton;

        public TabComponent(final JTabbedPane pane) {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(false);
            
            label = new JLabel() {
                public String getText() {
                    int i = pane.indexOfTabComponent(TabComponent.this);
                    if (i != -1) {
                        return pane.getTitleAt(i);
                    }
                    return null;
                }
            };
            add(label);
            
            closeButton = new CloseButton();
            add(closeButton);
        }

        /**
         * 内部类：关闭按钮
         */
        private class CloseButton extends JButton implements MouseListener {
            private boolean mouseOver = false;
            private boolean mousePressed = false;

            public CloseButton() {
                setPreferredSize(new Dimension(16, 16));
                setToolTipText("关闭此标签页");
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusable(false);
                setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
                addMouseListener(this);
                setRolloverEnabled(true);
            }

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (mousePressed && mouseOver) {
                    g2.setColor(Color.RED.darker());
                } else if (mouseOver) {
                    g2.setColor(Color.RED);
                } else {
                    g2.setColor(Color.GRAY);
                }
                
                int size = Math.min(getWidth(), getHeight()) - 6;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                if (mouseOver) {
                    // 绘制小叉叉
                    g2.setStroke(new BasicStroke(1.5f));
                    int margin = size / 4;
                    g2.drawLine(x + margin, y + margin, x + size - margin, y + size - margin);
                    g2.drawLine(x + size - margin, y + margin, x + margin, y + size - margin);
                } else {
                    // 绘制小圆圈
                    g2.drawOval(x, y, size, size);
                }
                g2.dispose();
            }

            public void mouseClicked(MouseEvent e) {
                int i = tabbedPane.indexOfTabComponent(TabComponent.this);
                if (i != -1) {
                    closeDocument(i);
                }
            }

            public void mousePressed(MouseEvent e) {
                mousePressed = true;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                mousePressed = false;
                repaint();
            }

            public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                repaint();
            }

            public void mouseExited(MouseEvent e) {
                mouseOver = false;
                repaint();
            }
        }
    }

    /**
     * 设置快捷键
     */
    private void setupShortcuts() {
        // 创建动作映射
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        // 新建文档 - Ctrl+N
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "newDocument");
        actionMap.put("newDocument", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newDocument();
            }
        });

        // 打开文档 - Ctrl+O
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "openDocument");
        actionMap.put("openDocument", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDocument();
            }
        });

        // 保存文档 - Ctrl+S
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "saveDocument");
        actionMap.put("saveDocument", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDocument();
            }
        });

        // 关闭当前文档 - Ctrl+W
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "closeDocument");
        actionMap.put("closeDocument", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeDocument(tabbedPane.getSelectedIndex());
            }
        });

        // 撤销 - Ctrl+Z
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });

        // 重做 - Ctrl+Y
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });

        // 切换标签页 - Ctrl+Tab
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK), "nextTab");
        actionMap.put("nextTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = tabbedPane.getSelectedIndex();
                if (selectedIndex < tabbedPane.getTabCount() - 1) {
                    tabbedPane.setSelectedIndex(selectedIndex + 1);
                } else {
                    tabbedPane.setSelectedIndex(0);
                }
            }
        });
    }
}
