import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.border.Border;


public class TextEditor extends JFrame {
    private JDesktopPane desktopPane;
    private ArrayList<TextDocument> documents;
    private JComboBox<String> fontComboBox;
    private JComboBox<String> fontSizeComboBox;
    private JToggleButton boldButton, italicButton, underlineButton;
    private JToggleButton leftAlignButton, centerAlignButton, rightAlignButton;

    private enum WindowArrangement {
        CASCADE,
        TILE_HORIZONTAL,
        TILE_VERTICAL
    }

    private WindowArrangement currentArrangement = WindowArrangement.CASCADE;

    public TextEditor() {
        setTitle("多文档富文本编辑器");
        setSize(1080, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 设置应用程序图标
        ImageIcon icon = new ImageIcon("src/icons/app_icon.png");
        setIconImage(icon.getImage());

        desktopPane = new JDesktopPane();
        // 设置背景图片
        ImageIcon imageIcon = new ImageIcon("src/images/background_2.png");
        if (imageIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            System.out.println("背景图片加载成功");
            Image image = imageIcon.getImage();
            float alpha = 0.5f; // 设置透明度为50%
            double scaleFactor = 0.8; // 设置图片大小
            desktopPane.setBorder(new BackgroundImageBorder(image, alpha, scaleFactor));
        } else {
            System.out.println("背景图片加载失败");
        }
        
        documents = new ArrayList<>();

        createMenuBar();
        createToolBar();

        add(desktopPane, BorderLayout.CENTER);

        newDocument();

        setupShortcuts();
    }

    private static class BackgroundImageBorder implements Border {
        private final Image image;
        private final float alpha;
        private final double scaleFactor; 
    
        public BackgroundImageBorder(Image image, float alpha, double scaleFactor) {
            this.image = image;
            this.alpha = alpha;
            this.scaleFactor = scaleFactor;
        }
    
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (image != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                int imageWidth = (int)(image.getWidth(null) * scaleFactor);
                int imageHeight = (int)(image.getHeight(null) * scaleFactor);
                int offsetX = (width - imageWidth) / 2;
                int offsetY = (height - imageHeight) / 2;
                g2d.drawImage(image, offsetX, offsetY, imageWidth, imageHeight, null);
                g2d.dispose();
            }
        }
    
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }
    
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.add(createMenuItem("新建文档", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, e -> newDocument()));
        fileMenu.add(createMenuItem("打开文档", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, e -> openDocument()));
        fileMenu.add(createMenuItem("保存文档", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, e -> saveDocument()));
        fileMenu.add(createMenuItem("另存为", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, e -> saveAsDocument()));
        fileMenu.add(createMenuItem("关闭文档", KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK, e -> closeDocument()));
        fileMenu.add(createMenuItem("关闭所有文档", KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, e -> closeAllDocuments()));
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
        formatMenu.add(createMenuItem("设置字体", e -> setSelectedFont()));
        formatMenu.add(createMenuItem("左对齐", e -> setAlignment(StyleConstants.ALIGN_LEFT)));
        formatMenu.add(createMenuItem("居中对齐", e -> setAlignment(StyleConstants.ALIGN_CENTER)));
        formatMenu.add(createMenuItem("右对齐", e -> setAlignment(StyleConstants.ALIGN_RIGHT)));
        menuBar.add(formatMenu);

        // 窗口菜单
        JMenu windowMenu = new JMenu("窗口");
        windowMenu.add(createMenuItem("窗口层叠", e -> cascadeWindows()));
        windowMenu.add(createMenuItem("水平平铺", e -> tileWindowsHorizontally()));
        windowMenu.add(createMenuItem("垂直平铺", e -> tileWindowsVertically()));
        menuBar.add(windowMenu);

        setJMenuBar(menuBar);
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

    private JButton createButton(String iconPath, String toolTipText) {
        ImageIcon originalIcon = new ImageIcon(iconPath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        JButton button = new JButton(scaledIcon);
        button.setToolTipText(toolTipText);
        button.setPreferredSize(new Dimension(25, 25));
        return button;
    }

    private void createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
         // 新建文件按钮
        JButton newButton = createButton("src/icons/new.png", "新建文件");
        newButton.addActionListener(e -> newDocument());
        toolBar.add(newButton);

        // 打开文件按钮
        JButton openButton = createButton("src/icons/open.png", "打开文件");
        openButton.addActionListener(e -> openDocument());
        toolBar.add(openButton);

        // 保存文件按钮
        JButton saveButton = createButton("src/icons/save.png", "保存文件");
        saveButton.addActionListener(e -> saveDocument());
        toolBar.add(saveButton);

        // 另存为按钮
        JButton saveAsButton = createButton("src/icons/save_as.png", "另存为");
        saveAsButton.addActionListener(e -> saveAsDocument());
        toolBar.add(saveAsButton);
     
        // 关闭所有文档按钮
        JButton closeAllButton = createButton("src/icons/close_all.png", "关闭所有文档");
        closeAllButton.addActionListener(e -> closeAllDocuments());
        toolBar.add(closeAllButton);
        toolBar.addSeparator();

        // 复制按钮
        JButton copyButton = createButton("src/icons/copy.png", "复制");
        copyButton.addActionListener(e -> copy());
        toolBar.add(copyButton);

        // 粘贴按钮
        JButton pasteButton = createButton("src/icons/paste.png", "粘贴");
        pasteButton.addActionListener(e -> paste());
        toolBar.add(pasteButton);

        // 剪切按钮
        JButton cutButton = createButton("src/icons/cut.png", "剪切");
        cutButton.addActionListener(e -> cut());
        toolBar.add(cutButton);
        toolBar.addSeparator();
        
        // 查找按钮
        JButton findButton = createButton("src/icons/find.png", "查找");
        findButton.addActionListener(e -> findText());
        toolBar.add(findButton);

        // 替换按钮
        JButton replaceButton = createButton("src/icons/replace.png", "替换");
        replaceButton.addActionListener(e -> replaceText());
        toolBar.add(replaceButton);
        
        toolBar.addSeparator();
        // 撤销按钮
        JButton undoButton = createButton("src/icons/undo.png", "撤销");
        undoButton.addActionListener(e -> undo());
        toolBar.add(undoButton);

        // 重做按钮
        JButton redoButton = createButton("src/icons/redo.png", "重做");
        redoButton.addActionListener(e -> redo());
        toolBar.add(redoButton);
        toolBar.addSeparator();
        // 字体选择
        fontComboBox = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontComboBox.setMaximumSize(new Dimension(150, 25));
        fontComboBox.addActionListener(e -> setSelectedFont());
        toolBar.add(fontComboBox);

        // 字号选择
        String[] fontSizes = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};
        fontSizeComboBox = new JComboBox<>(fontSizes);
        fontSizeComboBox.setMaximumSize(new Dimension(50, 25));
        fontSizeComboBox.addActionListener(e -> setSelectedFontSize());
        toolBar.add(fontSizeComboBox);
        toolBar.addSeparator();

        // 粗体按钮
        boldButton = createToggleButton("src/icons/bold.png", "粗体");
        boldButton.addActionListener(e -> setBold());
        toolBar.add(boldButton);

        // 斜体按钮
        italicButton = createToggleButton("src/icons/italic.png", "斜体");
        italicButton.addActionListener(e -> setItalic());
        toolBar.add(italicButton);

        // 下划线按钮
        underlineButton = createToggleButton("src/icons/underline.png", "下划线");
        underlineButton.addActionListener(e -> setUnderline());
        toolBar.add(underlineButton);
        
        toolBar.addSeparator();
        // 左对齐按钮
        leftAlignButton = createToggleButton("src/icons/align_left.png", "左对齐");
        leftAlignButton.addActionListener(e -> setAlignment(StyleConstants.ALIGN_LEFT));
        toolBar.add(leftAlignButton);

        // 居中对齐按钮
        centerAlignButton = createToggleButton("src/icons/align_center.png", "居中对齐");
        centerAlignButton.addActionListener(e -> setAlignment(StyleConstants.ALIGN_CENTER));
        toolBar.add(centerAlignButton);

        // 右对齐按钮
        rightAlignButton = createToggleButton("src/icons/align_right.png", "右对齐");
        rightAlignButton.addActionListener(e -> setAlignment(StyleConstants.ALIGN_RIGHT));
        toolBar.add(rightAlignButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private JToggleButton createToggleButton(String iconPath, String toolTipText) {
        ImageIcon originalIcon = new ImageIcon(iconPath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        JToggleButton button = new JToggleButton(scaledIcon);
        button.setToolTipText(toolTipText);
        button.setPreferredSize(new Dimension(25, 25));
        return button;
    }

    private void newDocument() {
        TextDocument doc = new TextDocument();
        documents.add(doc);
        JInternalFrame iframe = new JInternalFrame("新文档 " + documents.size(), true, true, true, true);
        iframe.add(new JScrollPane(doc.getTextPane()));
        iframe.setSize(400, 300);
        iframe.setVisible(true);
        
        // 添加窗口监听器
        iframe.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                closeDocument(iframe);
            }
        });
        
        desktopPane.add(iframe);
        iframe.toFront();
        try {
            iframe.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
        }
        
        // 应用当前的窗口排列
        applyCurrentArrangement();
    }
    
    private void closeDocument() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            closeDocument(currentFrame);
        }
        applyCurrentArrangement();
    }
    
    private void saveAsDocument() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        currentDoc.saveFile(file);
                        currentFrame.setTitle(file.getName());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this, "无法保存文件：" + e.getMessage());
                    }
                }
            }
        }
    }

    private void closeDocument(JInternalFrame frame) {
        if (frame != null) {
            TextDocument currentDoc = getDocumentFromFrame(frame);
            if (currentDoc != null) {
                if (currentDoc.isModified()) {
                    int choice = JOptionPane.showConfirmDialog(
                        this,
                        "文档已被修改，是否保存更改？",
                        "关闭文档",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        saveDocument(frame);
                        if (currentDoc.isModified()) {
                            // 如果用户在保存对话框中取消了操作，不关闭文档
                            return;
                        }
                    } else if (choice == JOptionPane.CANCEL_OPTION) {
                        // 用户选择取消，不关闭文档
                        return;
                    }
                }
                
                frame.dispose();
                documents.remove(currentDoc);
            } else {
                System.out.println("无法获取当前文档");
            }
        } else {
            System.out.println("没有选中的文档");
        }
        applyCurrentArrangement();
    }
    
    private void openDocument() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
    
            TextDocument doc = new TextDocument();
            try {
                doc.loadFile(file);
                documents.add(doc);
                JInternalFrame iframe = new JInternalFrame(file.getName(), true, true, true, true);
                iframe.add(new JScrollPane(doc.getTextPane()));
                iframe.setSize(400, 300);
                iframe.setVisible(true);
                
                // 添加窗口监听器
                iframe.addInternalFrameListener(new InternalFrameAdapter() {
                    @Override
                    public void internalFrameClosing(InternalFrameEvent e) {
                        closeDocument(iframe);
                    }
                });
                
                desktopPane.add(iframe);
                iframe.toFront();
                try {
                    iframe.setSelected(true);
                } catch (java.beans.PropertyVetoException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "无法打开文件：" + e.getMessage());
            }
        }
        applyCurrentArrangement();
    }

    private void saveDocument(JInternalFrame frame) {
        if (frame != null) {
            TextDocument currentDoc = getDocumentFromFrame(frame);
            if (currentDoc != null) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        currentDoc.saveFile(file);
                        frame.setTitle(file.getName());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this, "无法保存文件：" + e.getMessage());
                    }
                }
            }
        }
    }

    private void closeAllDocuments() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "确定要关闭所有文档吗？",
            "关闭所有文档",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            JInternalFrame[] frames = desktopPane.getAllFrames();
            for (JInternalFrame frame : frames) {
                closeDocument(frame);
            }
        }
    }

    private void saveDocument() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                if (currentDoc.getFile() != null) {
                    // 文件已经存在，直接保存
                    try {
                        currentDoc.saveFile(currentDoc.getFile());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this, "无法保存文件：" + e.getMessage());
                    }
                } else {
                    // 新文件，调用另存为方法
                    saveAsDocument();
                }
            }
        }
    }

    private TextDocument getDocumentFromFrame(JInternalFrame frame) {
        Component comp = frame.getContentPane().getComponent(0);
        if (comp instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) comp;
            Component view = scrollPane.getViewport().getView();
            if (view instanceof JTextPane) {
                for (TextDocument doc : documents) {
                    if (doc.getTextPane() == view) {
                        return doc;
                    }
                }
            }
        }
        System.out.println("无法找到与框架关联的文档");
        return null;
    }

    private void undo() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null && currentDoc.getUndoManager().canUndo()) {
                currentDoc.getUndoManager().undo();
            }
        }
    }

    private void redo() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null && currentDoc.getUndoManager().canRedo()) {
                currentDoc.getUndoManager().redo();
            }
        }
    }

    private void copy() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                currentDoc.getTextPane().copy();
            }
        }
    }

    private void paste() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                currentDoc.getTextPane().paste();
            }
        }
    }

    private void cut() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                currentDoc.getTextPane().cut();
            }
        }
    }

    private void findText() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                String searchText = JOptionPane.showInputDialog(this, "请输入要查找的文本：");
                if (searchText != null && !searchText.isEmpty()) {
                    JTextPane textPane = currentDoc.getTextPane();
                    String content = textPane.getText();
                    int index = content.indexOf(searchText, textPane.getCaretPosition());
                    if (index != -1) {
                        textPane.setCaretPosition(index);
                        textPane.select(index, index + searchText.length());
                    } else {
                        JOptionPane.showMessageDialog(this, "未找到指定文本。");
                    }
                }
            }
        }
    }
    
    private void replaceText() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                String searchText = JOptionPane.showInputDialog(this, "请输入要替换的文本：");
                if (searchText != null && !searchText.isEmpty()) {
                    String replaceText = JOptionPane.showInputDialog(this, "请输入替换后的文本：");
                    if (replaceText != null) {
                        JTextPane textPane = currentDoc.getTextPane();
                        String content = textPane.getText();
                        content = content.replace(searchText, replaceText);
                        textPane.setText(content);
                    }
                }
            }
        }
    }

    private void setBold() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                JTextPane currentTextPane = currentDoc.getTextPane();
                MutableAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setBold(attrs, boldButton.isSelected());
                setCharacterAttributes(currentTextPane, attrs, false);
            }
        }
    }

    private void setItalic() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                JTextPane currentTextPane = currentDoc.getTextPane();
                MutableAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setItalic(attrs, italicButton.isSelected());
                setCharacterAttributes(currentTextPane, attrs, false);
            }
        }
    }

    private void setUnderline() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                JTextPane currentTextPane = currentDoc.getTextPane();
                MutableAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setUnderline(attrs, underlineButton.isSelected());
                setCharacterAttributes(currentTextPane, attrs, false);
            }
        }
    }

    private void setAlignment(int alignment) {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                JTextPane currentTextPane = currentDoc.getTextPane();
                StyledDocument doc = currentTextPane.getStyledDocument();
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setAlignment(attrs, alignment);
                doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
            }
        }
    }

    private void setSelectedFont() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                JTextPane currentTextPane = currentDoc.getTextPane();
                String fontName = (String) fontComboBox.getSelectedItem();
                setFontFamily(currentTextPane, fontName);
            }
        }
    }

    private void setSelectedFontSize() {
        JInternalFrame currentFrame = desktopPane.getSelectedFrame();
        if (currentFrame != null) {
            TextDocument currentDoc = getDocumentFromFrame(currentFrame);
            if (currentDoc != null) {
                JTextPane currentTextPane = currentDoc.getTextPane();
                try {
                    int fontSize = Integer.parseInt((String) fontSizeComboBox.getSelectedItem());
                    setFontSize(currentTextPane, fontSize);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "请输入有效的字体大小！");
                }
            }
        }
    }

    private void setFontFamily(JTextPane textPane, String fontName) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, fontName);
        setCharacterAttributes(textPane, attrs, false);
    }

    private void setFontSize(JTextPane textPane, int fontSize) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, fontSize);
        setCharacterAttributes(textPane, attrs, false);
    }

    private void setCharacterAttributes(JTextPane textPane, AttributeSet attrs, boolean replace) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            textPane.getStyledDocument().setCharacterAttributes(start, end - start, attrs, replace);
        } else {
            MutableAttributeSet attribute = new SimpleAttributeSet(attrs);
            textPane.setCharacterAttributes(attribute, replace);
        }
    }

    private void cascadeWindows() {
        currentArrangement = WindowArrangement.CASCADE;
        int x = 0, y = 0;
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame.isIcon()) continue;
            try {
                frame.setMaximum(false);
                frame.reshape(x, y, 400, 300);
                x += 30;
                y += 30;
                if (x + 400 > getWidth()) x = 0;
                if (y + 300 > getHeight()) y = 0;
                frame.toFront();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void tileWindowsHorizontally() {
        currentArrangement = WindowArrangement.TILE_HORIZONTAL;
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int count = frames.length;
        if (count == 0) return;

        int rows = (int) Math.sqrt(count);
        int cols = count / rows;
        if (count % rows != 0) cols++;

        int width = getWidth() / cols;
        int height = getHeight() / rows;
        int x = 0, y = 0;

        for (JInternalFrame frame : frames) {
            if (frame.isIcon()) continue;
            try {
                frame.setMaximum(false);
                frame.reshape(x, y, width, height);
                x += width;
                if (x >= getWidth()) {
                    x = 0;
                    y += height;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void tileWindowsVertically() {
        currentArrangement = WindowArrangement.TILE_VERTICAL;
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int count = frames.length;
        if (count == 0) return;

        int height = getHeight() / count;
        int y = 0;

        for (JInternalFrame frame : frames) {
            if (frame.isIcon()) continue;
            try {
                frame.setMaximum(false);
                frame.reshape(0, y, getWidth(), height);
                y += height;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void applyCurrentArrangement() {
        switch (currentArrangement) {
            case CASCADE:
                cascadeWindows();
                break;
            case TILE_HORIZONTAL:
                tileWindowsHorizontally();
                break;
            case TILE_VERTICAL:
                tileWindowsVertically();
                break;
        }
    }

    private void setupShortcuts() {
        JRootPane rootPane = getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

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

        // 另存为 - Ctrl+Shift+S
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "saveAsDocument");
        actionMap.put("saveAsDocument", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAsDocument();
            }
        });

        // 关闭当前文档 - Ctrl+W
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "closeDocument");
        actionMap.put("closeDocument", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeDocument();
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

        // 关闭所有文档 - Ctrl+Shift+Q
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "closeAllDocuments");
        actionMap.put("closeAllDocuments", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeAllDocuments();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextEditor().setVisible(true));
    }
}