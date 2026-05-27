package calculator.view;

import calculator.data.CalculationRecord;
import calculator.data.Computer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * 计算器主窗口，只负责界面结构组织与界面刷新。
 */
public class CalculatorWindow extends JFrame {
    /**
     * Swing 可序列化类型约定的版本号。
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 面板之间的统一间距。
     */
    private static final int PANEL_GAP = 18;

    /**
     * 数字按钮的推荐尺寸。
     */
    private static final Dimension LARGE_BUTTON_SIZE = new Dimension(108, 88);

    /**
     * 科学功能按钮的推荐尺寸。
     */
    private static final Dimension MEDIUM_BUTTON_SIZE = new Dimension(92, 64);

    /**
     * 控制区按钮的推荐尺寸。
     */
    private static final Dimension CONTROL_BUTTON_SIZE = new Dimension(120, 54);

    /**
     * 运算符按钮的推荐尺寸。
     */
    private static final Dimension OPERATOR_BUTTON_SIZE = new Dimension(102, 88);

    /**
     * 计算器业务对象。
     */
    private final transient Computer computer;

    /**
     * 动作处理器。
     */
    private final transient CalculatorActionHandler actionHandler;

    /**
     * 表达式显示区。
     */
    private final JTextArea expressionDisplay;

    /**
     * 结果显示标签。
     */
    private final JLabel resultLabel;

    /**
     * 记忆状态标签。
     */
    private final JLabel memoryLabel;

    /**
     * 状态栏标签。
     */
    private final JLabel statusLabel;

    /**
     * 历史记录列表模型。
     */
    private final DefaultListModel<CalculationRecord> historyListModel;

    /**
     * 历史记录列表组件。
     */
    private final JList<CalculationRecord> historyList;

    /**
     * 创建主窗口。
     */
    public CalculatorWindow() {
        this.computer = new Computer();
        this.actionHandler = new CalculatorActionHandler(computer);
        this.expressionDisplay = new JTextArea(3, 20);
        this.resultLabel = new JLabel("0", SwingConstants.RIGHT);
        this.memoryLabel = new JLabel("Memory: 0");
        this.statusLabel = new JLabel("就绪");
        this.historyListModel = new DefaultListModel<>();
        this.historyList = new JList<>(historyListModel);
    }

    /**
     * 在窗口完成构造后初始化界面结构。
     */
    public void initializeWindow() {
        configureLookAndFeel();
        configureWindow();
        buildLayout();
        refreshAll("已恢复历史记录和记忆值。");
    }

    /**
     * 在窗口完成构造后初始化交互逻辑，避免构造期间暴露未完成初始化的对象。
     */
    public void initializeInteraction() {
        actionHandler.bindWindow(this);
        installKeyboardBindings();
    }

    /**
     * 返回当前选中的历史记录。
     *
     * @return 当前选中的历史记录；若未选中则返回 {@code null}
     */
    public CalculationRecord getSelectedHistoryRecord() {
        return historyList.getSelectedValue();
    }

    /**
     * 根据模型状态刷新全部界面区域。
     *
     * @param fallbackStatus 当没有错误消息时展示的默认状态文本
     */
    public void refreshAll(String fallbackStatus) {
        expressionDisplay.setText(formatExpressionForDisplay(computer.getExpressionText()));
        resultLabel.setText("= " + computer.getResultText());
        memoryLabel.setText("Memory: " + computer.getMemoryValueText()
                + (computer.hasMemoryValue() ? "  [M]" : ""));

        historyListModel.clear();
        for (CalculationRecord record : computer.getHistoryEntries()) {
            historyListModel.addElement(record);
        }
        if (!computer.getHistoryEntries().isEmpty()) {
            historyList.ensureIndexIsVisible(historyListModel.size() - 1);
        }

        String status = computer.getErrorMessage().isEmpty() ? fallbackStatus : computer.getErrorMessage();
        statusLabel.setText(status);
        statusLabel.setForeground(computer.getErrorMessage().isEmpty()
                ? new Color(97, 109, 128)
                : new Color(180, 50, 62));
    }

    /**
     * 配置系统外观。
     */
    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    /**
     * 配置窗口基础属性。
     */
    private void configureWindow() {
        setTitle("科学计算器");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setSize(1280, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CalculatorTheme.APP_BACKGROUND);
    }

    /**
     * 构建主界面布局。
     */
    private void buildLayout() {
        JPanel content = new JPanel(new BorderLayout(PANEL_GAP, PANEL_GAP));
        content.setBorder(BorderFactory.createEmptyBorder(PANEL_GAP, PANEL_GAP, PANEL_GAP, PANEL_GAP));
        content.setBackground(CalculatorTheme.APP_BACKGROUND);

        content.add(buildDisplayPanel(), BorderLayout.NORTH);
        content.add(buildKeypadPanel(), BorderLayout.CENTER);
        content.add(buildHistoryPanel(), BorderLayout.EAST);
        setContentPane(content);
    }

    /**
     * 构建顶部显示区。
     *
     * @return 显示区组件
     */
    private JComponent buildDisplayPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(0, 14));

        JLabel title = new JLabel("科学计算器");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(18, 33, 58));

        expressionDisplay.setEditable(false);
        expressionDisplay.setLineWrap(true);
        expressionDisplay.setWrapStyleWord(true);
        expressionDisplay.setFont(new Font("Monospaced", Font.PLAIN, 20));
        expressionDisplay.setForeground(new Color(67, 84, 108));
        expressionDisplay.setBackground(CalculatorTheme.PANEL_BACKGROUND);
        expressionDisplay.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        resultLabel.setForeground(new Color(17, 38, 74));

        memoryLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        memoryLabel.setForeground(new Color(72, 86, 107));

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        statusLabel.setForeground(new Color(97, 109, 128));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(title, BorderLayout.WEST);
        north.add(memoryLabel, BorderLayout.EAST);

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);
        south.add(resultLabel, BorderLayout.NORTH);
        south.add(statusLabel, BorderLayout.SOUTH);

        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(expressionDisplay), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * 构建按键面板。
     *
     * @return 按键区组件
     */
    private JComponent buildKeypadPanel() {
        JPanel wrapper = createCardPanel();
        wrapper.setLayout(new BorderLayout(0, 16));
        wrapper.add(buildControlPanel(), BorderLayout.NORTH);
        wrapper.add(buildMainKeypad(), BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * 构建历史记录面板。
     *
     * @return 历史区组件
     */
    private JComponent buildHistoryPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setPreferredSize(new Dimension(360, 0));

        JLabel title = new JLabel("历史记录");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(18, 33, 58));

        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        historyList.setVisibleRowCount(18);
        historyList.setCellRenderer(new HistoryListCellRenderer());
        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    actionHandler.replaySelectedHistory();
                }
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new GridLayout(1, 3, 10, 10));
        actions.setOpaque(false);
        actions.add(createSpecialButton("回放", "replayHistory", CalculatorTheme.SCIENCE_BUTTON));
        actions.add(createSpecialButton("导出", "exportHistory", CalculatorTheme.SCIENCE_BUTTON));
        actions.add(createSpecialButton("清空历史", "clearHistory", CalculatorTheme.UTILITY_BUTTON));

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyList), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * 构建记忆键行。
     *
     * @return 记忆键面板
     */
    private JPanel buildControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        controlPanel.setOpaque(false);
        controlPanel.add(createMemoryButton("MC", "mc"));
        controlPanel.add(createMemoryButton("MR", "mr"));
        controlPanel.add(createMemoryButton("M+", "mplus"));
        controlPanel.add(createMemoryButton("M-", "mminus"));
        controlPanel.add(createMemoryButton("MS", "ms"));
        controlPanel.add(createSpecialButton("CE", "clearEntry", CalculatorTheme.UTILITY_BUTTON, CONTROL_BUTTON_SIZE));
        controlPanel.add(createSpecialButton("C", "clearAll", CalculatorTheme.UTILITY_BUTTON, CONTROL_BUTTON_SIZE));
        controlPanel.add(createSpecialButton("⌫", "backspace", CalculatorTheme.UTILITY_BUTTON, CONTROL_BUTTON_SIZE));
        controlPanel.add(createSpecialButton("复制表达式", "copyExpression", CalculatorTheme.UTILITY_BUTTON, CONTROL_BUTTON_SIZE));
        controlPanel.add(createSpecialButton("复制结果", "copyResult", CalculatorTheme.UTILITY_BUTTON, CONTROL_BUTTON_SIZE));
        return controlPanel;
    }

    /**
     * 构建主按键矩阵。
     *
     * @return 主按键面板
     */
    private JPanel buildMainKeypad() {
        JPanel keypad = new JPanel(new BorderLayout(14, 0));
        keypad.setOpaque(false);
        keypad.add(buildScientificPanel(), BorderLayout.WEST);
        keypad.add(buildArithmeticPanel(), BorderLayout.CENTER);
        return keypad;
    }

    /**
     * 构建科学功能按键区。
     *
     * @return 科学功能面板
     */
    private JPanel buildScientificPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 3, 10, 10));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.add(createInputButton("sin", "sin(", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createInputButton("cos", "cos(", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createInputButton("tan", "tan(", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createInputButton("log", "log(", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createInputButton("ln", "ln(", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createSpecialButton("sqrt", "sqrt", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createInputButton("(", "(", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createInputButton(")", ")", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createInputButton("^", "^", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createInputButton("π", "pi", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createInputButton("e", "e", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createSpecialButton("%", "percent", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createSpecialButton("x²", "square", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createSpecialButton("1/x", "recip", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        panel.add(createSpecialButton("Ans", "ans", CalculatorTheme.SCIENCE_BUTTON, MEDIUM_BUTTON_SIZE));
        return panel;
    }

    /**
     * 构建数值与运算按键区。
     *
     * @return 数值运算面板
     */
    private JPanel buildArithmeticPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setOpaque(false);
        panel.add(buildDigitPanel(), BorderLayout.CENTER);
        panel.add(buildOperatorPanel(), BorderLayout.EAST);
        return panel;
    }

    /**
     * 构建数字输入区。
     *
     * @return 数字输入面板
     */
    private JPanel buildDigitPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 10));
        panel.setOpaque(false);
        panel.add(buildDigitRow("7", "8", "9"));
        panel.add(buildDigitRow("4", "5", "6"));
        panel.add(buildDigitRow("1", "2", "3"));
        panel.add(buildDigitBottomRow());
        return panel;
    }

    /**
     * 构建普通数字行。
     *
     * @param first 第一个按钮文本
     * @param second 第二个按钮文本
     * @param third 第三个按钮文本
     * @return 数字行面板
     */
    private JPanel buildDigitRow(String first, String second, String third) {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 10));
        row.setOpaque(false);
        row.add(createInputButton(first, first, Color.WHITE, LARGE_BUTTON_SIZE));
        row.add(createInputButton(second, second, Color.WHITE, LARGE_BUTTON_SIZE));
        row.add(createInputButton(third, third, Color.WHITE, LARGE_BUTTON_SIZE));
        return row;
    }

    /**
     * 构建数字区底部按钮行。
     *
     * @return 底部按钮行面板
     */
    private JPanel buildDigitBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 10));
        row.setOpaque(false);
        row.add(createSpecialButton("+/-", "toggleSign", CalculatorTheme.SCIENCE_BUTTON, LARGE_BUTTON_SIZE));
        row.add(createInputButton("0", "0", Color.WHITE, LARGE_BUTTON_SIZE));
        row.add(createInputButton(".", ".", Color.WHITE, LARGE_BUTTON_SIZE));
        return row;
    }

    /**
     * 构建运算符区。
     *
     * @return 运算符面板
     */
    private JPanel buildOperatorPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(116, 0));
        panel.add(createInputButton("÷", "/", CalculatorTheme.SCIENCE_BUTTON, OPERATOR_BUTTON_SIZE));
        panel.add(createInputButton("×", "*", CalculatorTheme.SCIENCE_BUTTON, OPERATOR_BUTTON_SIZE));
        panel.add(createInputButton("-", "-", CalculatorTheme.SCIENCE_BUTTON, OPERATOR_BUTTON_SIZE));
        panel.add(createInputButton("+", "+", CalculatorTheme.SCIENCE_BUTTON, OPERATOR_BUTTON_SIZE));
        panel.add(createEqualsButton());
        return panel;
    }

    /**
     * 将表达式中的内部运算符转换为更适合界面展示的符号。
     *
     * @param expression 原始表达式
     * @return 格式化后的表达式
     */
    private String formatExpressionForDisplay(String expression) {
        return expression.replace("*", "×");
    }

    /**
     * 安装键盘快捷键。
     */
    private void installKeyboardBindings() {
        new CalculatorKeyboardBinder(getRootPane(), actionHandler).install();
    }

    /**
     * 创建通用卡片面板。
     *
     * @return 卡片面板
     */
    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CalculatorTheme.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 235)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return panel;
    }

    /**
     * 创建带尺寸的普通输入按钮。
     *
     * @param label 按钮文本
     * @param token 输入片段
     * @param background 背景色
     * @param size 推荐尺寸
     * @return 按钮组件
     */
    private JButton createInputButton(String label, String token, Color background, Dimension size) {
        JButton button = createBaseButton(label, background, Color.BLACK, size, 24);
        button.addActionListener(event -> actionHandler.handleInput(token));
        return button;
    }

    /**
     * 创建记忆按钮。
     *
     * @param label   按钮文本
     * @param command 命令标识
     * @return 按钮组件
     */
    private JButton createMemoryButton(String label, String command) {
        JButton button = createBaseButton(label, CalculatorTheme.MEMORY_BUTTON, new Color(18, 77, 55), CalculatorWindow.CONTROL_BUTTON_SIZE, 18);
        button.addActionListener(event -> actionHandler.handleMemory(command));
        return button;
    }

    /**
     * 创建特殊动作按钮。
     *
     * @param label 按钮文本
     * @param command 命令标识
     * @param background 背景色
     * @return 按钮组件
     */
    private JButton createSpecialButton(String label, String command, Color background) {
        Color foreground = CalculatorTheme.ACCENT.equals(background) ? Color.WHITE : new Color(24, 36, 56);
        JButton button = createBaseButton(label, background, foreground, MEDIUM_BUTTON_SIZE, 18);
        button.addActionListener(event -> actionHandler.handleSpecial(command));
        return button;
    }

    /**
     * 创建带尺寸的特殊动作按钮。
     *
     * @param label 按钮文本
     * @param command 命令标识
     * @param background 背景色
     * @param size 推荐尺寸
     * @return 按钮组件
     */
    private JButton createSpecialButton(String label, String command, Color background, Dimension size) {
        Color foreground = CalculatorTheme.ACCENT.equals(background) ? Color.WHITE : new Color(24, 36, 56);
        JButton button = createBaseButton(label, background, foreground, size, 22);
        button.addActionListener(event -> actionHandler.handleSpecial(command));
        return button;
    }

    /**
     * 创建专用的求值按钮，确保等于号文本始终明确可见。
     *
     * @return 求值按钮
     */
    private JButton createEqualsButton() {
        JButton button = new EqualsButton(OPERATOR_BUTTON_SIZE);
        button.addActionListener(event -> actionHandler.handleSpecial("equals"));
        return button;
    }

    /**
     * 创建基础样式按钮。
     *
     * @param label 按钮文本
     * @param background 背景色
     * @param foreground 前景色
     * @return 按钮组件
     */
    private JButton createBaseButton(
            String label,
            Color background,
            Color foreground,
            Dimension preferredSize,
            int fontSize) {
        JButton button = new JButton(label);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setPreferredSize(preferredSize);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 219, 229)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return button;
    }

    /**
     * 专门用于求值操作的自绘按钮，避免系统外观忽略按钮文本颜色或背景色。
     */
    private static final class EqualsButton extends JButton {
        /**
         * Swing 可序列化类型约定的版本号。
         */
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 按钮上绘制的等号文本。
         */
        private static final String EQUALS_TEXT = "=";

        /**
         * 创建求值按钮。
         *
         * @param preferredSize 推荐尺寸
         */
        private EqualsButton(Dimension preferredSize) {
            super(EQUALS_TEXT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Dialog", Font.BOLD, 40));
            setPreferredSize(preferredSize);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(23, 84, 205)),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        }

        /**
         * 绘制按钮背景和居中的等号文本。
         *
         * @param graphics 绘图上下文
         */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(getModel().isPressed() ? new Color(21, 82, 205) : CalculatorTheme.ACCENT);
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            graphics2D.setColor(Color.WHITE);
            graphics2D.setFont(getFont());

            FontMetrics metrics = graphics2D.getFontMetrics();
            int textX = (getWidth() - metrics.stringWidth(EQUALS_TEXT)) / 2;
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            graphics2D.drawString(EQUALS_TEXT, textX, textY);
            graphics2D.dispose();
        }
    }
}
