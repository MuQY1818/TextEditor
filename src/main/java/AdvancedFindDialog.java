import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

/**
 * 高级查找和替换对话框类
 * 提供查找、替换、正则表达式匹配等功能
 */
public class AdvancedFindDialog extends JDialog {
    // UI 组件
    private JComboBox<String> documentSelector; // 文档选择下拉框
    private JTextField searchField; // 搜索文本框
    private JTextField replaceField; // 替换文本框
    private JCheckBox regexCheckBox; // 正则表达式复选框
    private JButton findNextButton; // 查找下一个按钮
    private JButton findPreviousButton; // 查找上一个按钮
    private JButton findAllButton; // 查找全部按钮
    private JButton replaceButton; // 替换按钮
    private JButton replaceAllButton; // 全部替换按钮
    private JButton clearHighlightButton; // 清除高亮按钮

    private final List<DocumentFrame> documentFrames; // 文档框架列表
    private ArrayList<Integer> allMatchPositions; // 所有匹配位置
    private int currentMatchIndex; // 当前匹配索引

    /**
     * 构造函数
     * @param parent 父窗口
     * @param documentFrames 文档框架列表
     */
    public AdvancedFindDialog(TextEditor parent, List<DocumentFrame> documentFrames) {
        super(parent, "高级查找和替换", false);
        this.documentFrames = documentFrames;
        initComponents();
        setPreferredSize(new Dimension(600, 300));
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * 初始化UI组件
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建输入面板，使用GridBagLayout进行布局
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 获取所有文档标题
        List<String> documentTitles = new ArrayList<>();
        for (DocumentFrame frame : documentFrames) {
            documentTitles.add(frame.getTitle());
        }
        documentSelector = new JComboBox<>(documentTitles.toArray(new String[0]));
        searchField = new JTextField(30);
        replaceField = new JTextField(30);
        regexCheckBox = new JCheckBox("使用正则表达式");

        // 添加组件到输入面板
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("选择文档："), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        inputPanel.add(documentSelector, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("查找："), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        inputPanel.add(searchField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("替换为："), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        inputPanel.add(replaceField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        inputPanel.add(regexCheckBox, gbc);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        findPreviousButton = createStyledButton("查找上一个");
        findNextButton = createStyledButton("查找下一个");
        findAllButton = createStyledButton("查找全部");
        replaceButton = createStyledButton("替换");
        replaceAllButton = createStyledButton("全部替换");
        clearHighlightButton = createStyledButton("清除高亮");

        buttonPanel.add(findPreviousButton);
        buttonPanel.add(findNextButton);
        buttonPanel.add(findAllButton);
        buttonPanel.add(replaceButton);
        buttonPanel.add(replaceAllButton);
        buttonPanel.add(clearHighlightButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        setupListeners();
    }

    /**
     * 创建样式化按钮
     * @param text 按钮文本
     * @return 样式化的JButton
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(110, 30)); // 设置按钮大小
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12)); // 设置字体
        return button;
    }

    /**
     * 设置按钮监听器
     */
    private void setupListeners() {
        findNextButton.addActionListener(e -> findNext());
        findPreviousButton.addActionListener(e -> findPrevious());
        findAllButton.addActionListener(e -> findAll());
        replaceButton.addActionListener(e -> replace());
        replaceAllButton.addActionListener(e -> replaceAll());
        clearHighlightButton.addActionListener(e -> clearHighlight());
    }

    /**
     * 获取当前选中的文本面板
     * @return 当前选中的JTextPane
     */
    private JTextPane getCurrentTextPane() {
        int selectedIndex = documentSelector.getSelectedIndex();
        return documentFrames.get(selectedIndex).getTextPane();
    }

    /**
     * 查找下一个匹配项
     */
    private void findNext() {
        find(true);
    }

    /**
     * 查找上一个匹配项
     */
    private void findPrevious() {
        find(false);
    }

    /**
     * 获取文档内容
     * @param textPane 文本面板
     * @return 文档内容
     */
    private String getDocumentText(JTextPane textPane) {
        try {
            Document doc = textPane.getDocument();
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 执行查找操作
     * @param forward 是否向前查找
     */
    private void find(boolean forward) {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) return;

        JTextPane textPane = getCurrentTextPane();
        String content = getDocumentText(textPane);
        int caretPosition = textPane.getCaretPosition();
        int foundIndex = -1;

        // 如果有选中的文本，调整搜索起始位置
        if (forward) {
            caretPosition = textPane.getSelectionEnd();
        } else {
            caretPosition = textPane.getSelectionStart();
        }

        if (regexCheckBox.isSelected()) {
            // 使用正则表达式查找
            try {
                Pattern pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(content);
                if (forward) {
                    // 向前查找
                    if (matcher.find(caretPosition)) {
                        foundIndex = matcher.start();
                    } else if (matcher.find(0)) { // 从头开始查找
                        foundIndex = matcher.start();
                    }
                } else {
                    // 向后查找
                    int lastMatch = -1;
                    int searchEndPos = caretPosition;
                    matcher = pattern.matcher(content.substring(0, searchEndPos));
                    while (matcher.find()) {
                        lastMatch = matcher.start();
                    }
                    if (lastMatch != -1) {
                        foundIndex = lastMatch;
                    } else {
                        // 如果在当前位置之前没找到，则从文档末尾开始搜索
                        matcher = pattern.matcher(content);
                        while (matcher.find()) {
                            lastMatch = matcher.start();
                        }
                        foundIndex = lastMatch;
                    }
                }
            } catch (PatternSyntaxException ex) {
                JOptionPane.showMessageDialog(this, "无效的正则表达式: " + ex.getMessage());
                return;
            }
        } else {
            // 普通文本查找
            String lowerContent = content.toLowerCase();
            String lowerSearchText = searchText.toLowerCase();
            
            if (forward) {
                // 向前查找
                foundIndex = lowerContent.indexOf(lowerSearchText, caretPosition);
                if (foundIndex == -1) { // 如果没找到，从头开始查找
                    foundIndex = lowerContent.indexOf(lowerSearchText);
                    if (foundIndex != -1) {
                        JOptionPane.showMessageDialog(this, "已到达文档末尾，从头继续查找。");
                    }
                }
            } else {
                // 向后查找
                foundIndex = lowerContent.lastIndexOf(lowerSearchText, Math.max(0, caretPosition - 1));
                if (foundIndex == -1) { // 如果没找到，从末尾开始查找
                    foundIndex = lowerContent.lastIndexOf(lowerSearchText);
                    if (foundIndex != -1) {
                        JOptionPane.showMessageDialog(this, "已到达文档开头，从末尾继续查找。");
                    }
                }
            }
        }

        if (foundIndex != -1) {
            // 找到匹配项，设置选中并滚动到可见区域
            try {
                // 使用 Document 的方法来设置选择
                textPane.setCaretPosition(foundIndex);
                textPane.moveCaretPosition(foundIndex + searchText.length());
                
                // 确保选中的文本可见
                Rectangle viewRect = textPane.modelToView(foundIndex);
                if (viewRect != null) {
                    textPane.scrollRectToVisible(viewRect);
                }
                textPane.requestFocusInWindow();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "未找到匹配项。");
            // 重置光标位置
            if (forward) {
                textPane.setCaretPosition(0);
            } else {
                try {
                    textPane.setCaretPosition(textPane.getDocument().getLength());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 查找所有匹配项
     */
    private void findAll() {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) return;

        JTextPane textPane = getCurrentTextPane();
        String content = getDocumentText(textPane);
        allMatchPositions = new ArrayList<>();
        currentMatchIndex = -1;

        if (regexCheckBox.isSelected()) {
            // 使用正则表达式查找所有匹配项
            try {
                Pattern pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    allMatchPositions.add(matcher.start());
                }
            } catch (PatternSyntaxException ex) {
                JOptionPane.showMessageDialog(this, "无效的正则表达式: " + ex.getMessage());
                return;
            }
        } else {
            // 普通文本查找所有匹配项
            String lowerContent = content.toLowerCase();
            String lowerSearchText = searchText.toLowerCase();
            int index = 0;
            while ((index = lowerContent.indexOf(lowerSearchText, index)) != -1) {
                allMatchPositions.add(index);
                index += searchText.length();
            }
        }

        if (!allMatchPositions.isEmpty()) {
            highlightAll(searchText);
            JOptionPane.showMessageDialog(this, "找到 " + allMatchPositions.size() + " 个匹配项。");
        } else {
            JOptionPane.showMessageDialog(this, "未找到匹配项。");
        }
    }

    /**
     * 高亮显示所有匹配项
     * @param searchText 搜索文本
     */
    private void highlightAll(String searchText) {
        Highlighter highlighter = getCurrentTextPane().getHighlighter();
        highlighter.removeAllHighlights();

        for (int position : allMatchPositions) {
            try {
                highlighter.addHighlight(position, position + searchText.length(),
                        new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行替换操作
     */
    private void replace() {
        String searchText = searchField.getText();
        String replaceText = replaceField.getText();
        if (searchText.isEmpty()) return;

        int selectionStart = getCurrentTextPane().getSelectionStart();
        int selectionEnd = getCurrentTextPane().getSelectionEnd();

        if (selectionStart != selectionEnd) {
            String selectedText = getCurrentTextPane().getSelectedText();
            if (selectedText.equals(searchText) || (regexCheckBox.isSelected() && selectedText.matches(searchText))) {
                getCurrentTextPane().replaceSelection(replaceText);
                findNext();
            } else {
                findNext();
            }
        } else {
            findNext();
        }
    }

    /**
     * 执行全部替换操作
     */
    private void replaceAll() {
        String searchText = searchField.getText();
        String replaceText = replaceField.getText();
        if (searchText.isEmpty()) return;

        String content = getCurrentTextPane().getText();
        String newContent;

        if (regexCheckBox.isSelected()) {
            // 使用正则表达式替换
            try {
                Pattern pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
                newContent = pattern.matcher(content).replaceAll(replaceText);
            } catch (PatternSyntaxException ex) {
                JOptionPane.showMessageDialog(this, "无效的正则表达式: " + ex.getMessage());
                return;
            }
        } else {
            // 普通文本替换
            newContent = content.replaceAll("(?i)" + Pattern.quote(searchText), replaceText);
        }

        getCurrentTextPane().setText(newContent);
        JOptionPane.showMessageDialog(this, "替换完成。");
    }

    /**
     * 清除所有高亮
     */
    private void clearHighlight() {
        Highlighter highlighter = getCurrentTextPane().getHighlighter();
        highlighter.removeAllHighlights();
    }
}