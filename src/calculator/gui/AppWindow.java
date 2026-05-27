package calculator.gui;

import calculator.view.CalculatorWindow;

import javax.swing.SwingUtilities;

/**
 * 应用程序入口，仅负责在事件派发线程中启动窗口。
 */
public final class AppWindow {
    /**
     * 程序启动入口。
     *
     * @param args 启动参数，当前未使用
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorWindow window = new CalculatorWindow();
            window.initializeWindow();
            window.initializeInteraction();
            window.setVisible(true);
        });
    }
}
