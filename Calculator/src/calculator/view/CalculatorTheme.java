package calculator.view;

import java.awt.Color;

/**
 * 统一定义计算器界面的配色常量。
 */
public final class CalculatorTheme {
    /**
     * 应用整体背景色。
     */
    public static final Color APP_BACKGROUND = new Color(239, 243, 247);

    /**
     * 卡片面板背景色。
     */
    public static final Color PANEL_BACKGROUND = new Color(250, 252, 255);

    /**
     * 主要强调色。
     */
    public static final Color ACCENT = new Color(30, 102, 245);

    /**
     * 科学计算按钮背景色。
     */
    public static final Color SCIENCE_BUTTON = new Color(225, 235, 255);

    /**
     * 工具按钮背景色。
     */
    public static final Color UTILITY_BUTTON = new Color(236, 240, 245);

    /**
     * 记忆按钮背景色。
     */
    public static final Color MEMORY_BUTTON = new Color(216, 245, 234);

    /**
     * 工具类不允许实例化。
     */
    private CalculatorTheme() {
    }
}
