import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class AdvancedFindDialog extends JDialog {
    private JComboBox<String> documentSelector;
    private JTextField searchField;
    private JTextField replaceField;
    private JCheckBox regexCheckBox;
    private JButton findNextButton;
    private JButton findPreviousButton;
    private JButton findAllButton;
    private JButton replaceButton;
    private JButton replaceAllButton;
    private JButton clearHighlightButton;
    private List<DocumentFrame> documentFrames;
    private ArrayList<Integer> allMatchPositions;
    private int currentMatchIndex;

    public AdvancedFindDialog(TextEditor parent, List<DocumentFrame> documentFrames) {
        super(parent, "高级查找和替换", false);
        this.documentFrames = documentFrames;
        initComponents();
        setPreferredSize(new Dimension(600, 300));
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        List<String> documentTitles = new ArrayList<>();
        for (DocumentFrame frame : documentFrames) {
            documentTitles.add(frame.getTitle());
        }
        documentSelector = new JComboBox<>(documentTitles.toArray(new String[0]));
        searchField = new JTextField(30);
        replaceField = new JTextField(30);
        regexCheckBox = new JCheckBox("使用正则表达式");

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

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(110, 30)); // 设置按钮大小
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12)); // 设置字体
        return button;
    }

    private void setupListeners() {
        findNextButton.addActionListener(e -> findNext());
        findPreviousButton.addActionListener(e -> findPrevious());
        findAllButton.addActionListener(e -> findAll());
        replaceButton.addActionListener(e -> replace());
        replaceAllButton.addActionListener(e -> replaceAll());
        clearHighlightButton.addActionListener(e -> clearHighlight());
    }

    private JTextPane getCurrentTextPane() {
        int selectedIndex = documentSelector.getSelectedIndex();
        return documentFrames.get(selectedIndex).getTextPane();
    }

    private void findNext() {
        find(true);
    }

    private void findPrevious() {
        find(false);
    }

    private void find(boolean forward) {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) return;

        String content = getCurrentTextPane().getText();
        int caretPosition = getCurrentTextPane().getCaretPosition();
        int foundIndex = -1;

        if (regexCheckBox.isSelected()) {
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
            getCurrentTextPane().setCaretPosition(foundIndex);
            getCurrentTextPane().select(foundIndex, foundIndex + searchText.length());
            getCurrentTextPane().getCaret().setSelectionVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "未找到匹配项。");
        }
    }

    private void findAll() {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) return;

        String content = getCurrentTextPane().getText();
        allMatchPositions = new ArrayList<>();
        currentMatchIndex = -1;

        if (regexCheckBox.isSelected()) {
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

    private void replaceAll() {
        String searchText = searchField.getText();
        String replaceText = replaceField.getText();
        if (searchText.isEmpty()) return;

        String content = getCurrentTextPane().getText();
        String newContent;

        if (regexCheckBox.isSelected()) {
            try {
                Pattern pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
                newContent = pattern.matcher(content).replaceAll(replaceText);
            } catch (PatternSyntaxException ex) {
                JOptionPane.showMessageDialog(this, "无效的正则表达式: " + ex.getMessage());
                return;
            }
        } else {
            newContent = content.replaceAll("(?i)" + Pattern.quote(searchText), replaceText);
        }

        getCurrentTextPane().setText(newContent);
        JOptionPane.showMessageDialog(this, "替换完成。");
    }

    private void clearHighlight() {
        Highlighter highlighter = getCurrentTextPane().getHighlighter();
        highlighter.removeAllHighlights();
    }
}