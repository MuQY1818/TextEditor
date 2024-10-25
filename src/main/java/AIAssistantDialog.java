import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

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
import java.awt.geom.RoundRectangle2D;

public class AIAssistantDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantDialog.class);
    private static final int MAX_BUBBLE_WIDTH = 600; // 保持这个最大宽度
    private static final int MIN_BUBBLE_WIDTH = 600; // 添加最小宽度
    private static final double MAX_BUBBLE_WIDTH_RATIO = 0.7; // 气泡最大宽度占聊天面板宽度的比例

    // 在这里添加新的常量定义
    private static final int BUBBLE_WIDTH = 600; // 固定气泡宽度

    private JTextArea inputArea;
    private JButton sendButton;
    private JButton insertButton;
    private final DocumentFrame currentDocument;
    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private List<Message> chatHistory;
    private JTextArea currentAIMessageArea = null;

    public AIAssistantDialog(TextEditor parent) {
        super(parent, "AI助手", false);
        this.currentDocument = (DocumentFrame) parent.getDesktopPane().getSelectedFrame();
        this.chatHistory = new ArrayList<>();
        this.currentAIMessageArea = null; // 初始化为null
        initComponents();
        setSize(900, 600); // 设置一个更大的初始尺寸
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 聊天面板
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        // 添加这行来设置组件之间的垂直间距
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(chatScrollPane, BorderLayout.CENTER);

        // 输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputArea = new JTextArea(3, 40);
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sendButton = new JButton("发送");
        insertButton = new JButton("插入对话到文档");
        buttonPanel.add(sendButton);
        buttonPanel.add(insertButton);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.SOUTH);

        // 添加事件监听器
        sendButton.addActionListener(e -> sendMessage());
        insertButton.addActionListener(e -> insertChatHistory());
    }

    private void addMessageBubble(String sender, String message, boolean isAI) {
        System.out.println("添加消息气泡 - isAI: " + isAI + ", 发送者: " + sender + ", 消息: " + message);

        JPanel bubblePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int arc = 15;
                
                Color bubbleColor = isAI ? new Color(173, 216, 230) : new Color(220, 255, 220);
                g2d.setColor(bubbleColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, arc, arc));
                
                g2d.dispose();
            }
        };
        bubblePanel.setLayout(new BorderLayout());
        bubblePanel.setOpaque(false);
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel senderLabel = new JLabel(sender + ":");
        senderLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setBorder(null);

        bubblePanel.add(senderLabel, BorderLayout.NORTH);
        bubblePanel.add(messageArea, BorderLayout.CENTER);

        // 设置首选大小，宽度固定，高度根据内容自动调整
        int preferredHeight = messageArea.getPreferredSize().height + senderLabel.getPreferredSize().height + 40; // 40是上下边距之和
        bubblePanel.setPreferredSize(new Dimension(BUBBLE_WIDTH, preferredHeight));

        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(bubblePanel);

        chatPanel.add(wrapperPanel);
        chatPanel.add(Box.createVerticalStrut(10)); // 在气泡之间添加垂直间距，减少到10像素
        
        chatPanel.revalidate();
        chatPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });

        if (isAI) {
            currentAIMessageArea = messageArea;
        }
    }

    private void addAIMessageBubble(String message) {
        if (currentAIMessageArea == null) {
            // 如果是新的AI回复，创建新的气泡
            addMessageBubble("AI", message, true);
            Component[] components = chatPanel.getComponents();
            JPanel lastBubble = (JPanel) components[components.length - 1];
            JPanel bubblePanel = (JPanel) lastBubble.getComponent(0);
            Component[] bubbleComponents = bubblePanel.getComponents();
            for (Component bc : bubbleComponents) {
                if (bc instanceof JTextArea) {
                    currentAIMessageArea = (JTextArea) bc;
                    break;
                }
            }
        } else {
            // 更新现有的AI回复气泡
            currentAIMessageArea.setText(message);
            
            // 重新计算气泡的高度
            JPanel bubblePanel = (JPanel) currentAIMessageArea.getParent();
            JLabel senderLabel = (JLabel) bubblePanel.getComponent(0);
            int preferredHeight = currentAIMessageArea.getPreferredSize().height + senderLabel.getPreferredSize().height + 40;
            
            bubblePanel.setPreferredSize(new Dimension(BUBBLE_WIDTH, preferredHeight));
            
            chatPanel.revalidate();
            chatPanel.repaint();
        }

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void sendMessage() {
        String userInput = inputArea.getText().trim();
        if (userInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入内容");
            return;
        }

        addMessageBubble("用户", userInput, false);
        inputArea.setText("");

        Message userMsg = Message.builder().role(Role.USER.getValue()).content(userInput).build();
        chatHistory.add(userMsg);

        sendButton.setEnabled(false);
        currentAIMessageArea = null; // 重置currentAIMessageArea，为新的AI回复做准备

        try {
            Generation gen = new Generation();
            GenerationParam param = GenerationParam.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .model("qwen-plus")
                .messages(chatHistory)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true)
                .build();
            
            Flowable<GenerationResult> result = gen.streamCall(param);
            
            StringBuilder aiResponse = new StringBuilder();
            result.subscribe(
                message -> {
                    String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                    aiResponse.append(content);
                    SwingUtilities.invokeLater(() -> addAIMessageBubble(aiResponse.toString()));
                },
                error -> {
                    logger.error("生成AI响应时发生错误: {}", error.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "生成AI响应时发生错误: " + error.getMessage());
                        sendButton.setEnabled(true);
                    });
                },
                () -> {
                    Message aiMsg = Message.builder().role(Role.ASSISTANT.getValue()).content(aiResponse.toString()).build();
                    chatHistory.add(aiMsg);
                    SwingUtilities.invokeLater(() -> {
                        sendButton.setEnabled(true);
                        currentAIMessageArea = null; // AI回复结束，重置currentAIMessageArea
                    });
                }
            );
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            logger.error("初始化AI响应生成时发生错误: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "初始化AI响应生成时发生错误: " + e.getMessage());
            sendButton.setEnabled(true);
        }
    }

    private void insertChatHistory() {
        if (chatHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可插入的对话内容");
            return;
        }

        if (currentDocument != null) {
            StringBuilder chatText = new StringBuilder();
            for (Message msg : chatHistory) {
                chatText.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n\n");
            }
            currentDocument.insertText(chatText.toString());
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "无法找到当前文档");
        }
    }

    private int getMaxBubbleWidth() {
        return (int)(chatScrollPane.getViewport().getWidth() * MAX_BUBBLE_WIDTH_RATIO);
    }
}
