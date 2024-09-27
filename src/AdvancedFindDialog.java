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
     * 执行查找操作
     * @param forward 是否向前查找
     */
    private void find(boolean forward) {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) return;

        String content = getCurrentTextPane().getText();
        int caretPosition = getCurrentTextPane().getCaretPosition();
        int foundIndex = -1;

        if (regexCheckBox.isSelected()) {
            // 使用正则表达式查找
            try {
                Pattern pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(content);
                if (forward) {
                    if (matcher.find(caretPosition)) {
                        foundIndex = matcher.start();
                    } else if (matcher.find(0)) {
                        foundIndex = matcher.start();
                    }
                } else {
                    int lastMatchEnd = -1;
                    while (matcher.find() && matcher.start() < caretPosition) {
                        lastMatchEnd = matcher.start();
                    }
                    foundIndex = lastMatchEnd;
                }
            } catch (PatternSyntaxException ex) {
                JOptionPane.showMessageDialog(this, "无效的正则表达式: " + ex.getMessage());
                return;
            }
        } else {
            // 普通文本查找
            if (forward) {
                foundIndex = content.toLowerCase().indexOf(searchText.toLowerCase(), caretPosition);
                if (foundIndex == -1) {
                    foundIndex = content.toLowerCase().indexOf(searchText.toLowerCase());
                }
            } else {
                foundIndex = content.toLowerCase().lastIndexOf(searchText.toLowerCase(), caretPosition - 1);
            }
        }

        if (foundIndex != -1) {
            // 找到匹配项，设置选中
            getCurrentTextPane().setCaretPosition(foundIndex);
            getCurrentTextPane().select(foundIndex, foundIndex + searchText.length());
            getCurrentTextPane().getCaret().setSelectionVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "未找到匹配项。");
        }
    }

    /**
     * 查找所有匹配项
     */
    private void findAll() {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) return;

        String content = getCurrentTextPane().getText();
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
            int index = 0;
            while ((index = content.toLowerCase().indexOf(searchText.toLowerCase(), index)) != -1) {
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