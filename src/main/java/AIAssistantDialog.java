import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;

import io.reactivex.Flowable;
import net.miginfocom.swing.MigLayout;

public class AIAssistantDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantDialog.class);
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton generateButton;
    private JButton insertButton;
    private final DocumentFrame currentDocument;
    private Timer typewriterTimer;
    private StringBuilder responseBuffer;

    public AIAssistantDialog(TextEditor parent) {
        super(parent, "AI助手", false);
        this.currentDocument = (DocumentFrame) parent.getDesktopPane().getSelectedFrame();
        initComponents();
        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][][grow 40][grow 60][]"));

        // 输入区域
        add(createStyledLabel("输入:"), "wrap");
        inputArea = new JTextArea(8, 40);
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(inputScrollPane, "grow, wrap, gapbottom 10");

        // 输出区域
        add(createStyledLabel("AI生成内容:"), "wrap");
        outputArea = new JTextArea(12, 40);
        outputArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(BorderFactory.createEmptyBorder());
        outputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(outputScrollPane, "grow, wrap");

        // 按钮面板
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]"));
        generateButton = createStyledButton("生成");
        insertButton = createStyledButton("插入到文档");
        buttonPanel.add(generateButton, "growx");
        buttonPanel.add(insertButton, "growx");
        add(buttonPanel, "growx");

        // 添加事件监听器
        generateButton.addActionListener(e -> generateAIResponse());
        insertButton.addActionListener(e -> insertGeneratedText());
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private void generateAIResponse() {
        String userInput = inputArea.getText();
        if (userInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入内容");
            return;
        }

        generateButton.setEnabled(false);
        outputArea.setText("");

        try {
            Generation gen = new Generation();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content(userInput).build();
            GenerationParam param = buildGenerationParam(userMsg);
            
            Flowable<GenerationResult> result = gen.streamCall(param);
            
            result.subscribe(
                message -> {
                    String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                    SwingUtilities.invokeLater(() -> {
                        outputArea.append(content);
                        outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        
                        // 可选：打印完整的 JSON 响应到控制台
                        System.out.println(JsonUtils.toJson(message));
                    });
                },
                error -> {
                    logger.error("生成AI响应时发生错误: {}", error.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "生成AI响应时发生错误: " + error.getMessage());
                        generateButton.setEnabled(true);
                    });
                },
                () -> SwingUtilities.invokeLater(() -> generateButton.setEnabled(true))
            );
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            logger.error("初始化AI响应生成时发生错误: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "初始化AI响应生成时发生错误: " + e.getMessage());
            generateButton.setEnabled(true);
        }
    }

    private void startTypewriterEffect() {
        if (typewriterTimer != null && typewriterTimer.isRunning()) {
            typewriterTimer.stop();
        }

        typewriterTimer = new Timer(50, new ActionListener() {
            private int currentIndex = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex < responseBuffer.length()) {
                    outputArea.append(String.valueOf(responseBuffer.charAt(currentIndex)));
                    currentIndex++;
                } else {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        typewriterTimer.start();
    }

    private GenerationParam buildGenerationParam(Message userMsg) {
        return GenerationParam.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .model("qwen-plus")
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true)
                .build();
    }

    private void handleGenerationResult(GenerationResult message) {
        SwingUtilities.invokeLater(() -> {
            String content = message.getOutput().getChoices().get(0).getMessage().getContent();
            outputArea.append(content);
            
            // 可选：打印完整的 JSON 响应到控制台
            System.out.println(JsonUtils.toJson(message));
        });
    }

    private void insertGeneratedText() {
        String generatedText = outputArea.getText();
        if (generatedText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可插入的生成内容");
            return;
        }

        if (currentDocument != null) {
            currentDocument.insertText(generatedText);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "无法找到当前文档");
        }
    }
}
