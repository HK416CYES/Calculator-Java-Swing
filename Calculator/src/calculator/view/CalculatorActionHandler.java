package calculator.view;

import calculator.data.CalculationRecord;
import calculator.data.Computer;

import javax.swing.JFileChooser;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Path;

/**
 * 集中处理窗口中的用户动作，避免窗口类承担过多业务逻辑。
 */
public class CalculatorActionHandler {
    /**
     * 关联的窗口对象。
     */
    private CalculatorWindow window;

    /**
     * 关联的计算器业务对象。
     */
    private final Computer computer;

    /**
     * 创建动作处理器。
     *
     * @param computer 计算器对象
     */
    public CalculatorActionHandler(Computer computer) {
        this.computer = computer;
    }

    /**
     * 绑定当前处理器所服务的窗口。
     *
     * @param window 窗口对象
     */
    public void bindWindow(CalculatorWindow window) {
        this.window = window;
    }

    /**
     * 处理普通文本输入。
     *
     * @param token 输入片段
     */
    public void handleInput(String token) {
        computer.appendToken(token);
        window.refreshAll("已更新表达式。");
    }

    /**
     * 处理求值操作。
     */
    public void handleEvaluate() {
        if (computer.evaluate()) {
            window.refreshAll("计算完成。");
        } else {
            window.refreshAll(computer.getErrorMessage());
        }
    }

    /**
     * 处理记忆相关命令。
     *
     * @param command 命令标识
     */
    public void handleMemory(String command) {
        switch (command) {
            case "mc" -> {
                computer.memoryClear();
                window.refreshAll("记忆值已清空。");
            }
            case "mr" -> {
                computer.memoryRecall();
                window.refreshAll("已回填记忆值。");
            }
            case "mplus" -> window.refreshAll(computer.memoryAdd() ? "已累加到记忆值。" : computer.getErrorMessage());
            case "mminus" -> window.refreshAll(computer.memorySubtract() ? "已从记忆值中减去当前值。" : computer.getErrorMessage());
            case "ms" -> window.refreshAll(computer.memoryStore() ? "当前值已写入记忆。" : computer.getErrorMessage());
            default -> window.refreshAll("未识别的记忆命令。");
        }
    }

    /**
     * 处理特殊按钮命令。
     *
     * @param command 命令标识
     */
    public void handleSpecial(String command) {
        switch (command) {
            case "percent" -> {
                computer.appendPercent();
                window.refreshAll("已插入百分比。");
            }
            case "square" -> {
                computer.wrapExpression("square");
                window.refreshAll("已对当前表达式应用平方。");
            }
            case "recip" -> {
                computer.wrapExpression("recip");
                window.refreshAll("已对当前表达式应用倒数。");
            }
            case "sqrt" -> {
                computer.wrapExpression("sqrt");
                window.refreshAll("已对当前表达式应用平方根。");
            }
            case "toggleSign" -> {
                computer.toggleSign();
                window.refreshAll("已切换表达式正负号。");
            }
            case "backspace" -> {
                computer.backspace();
                window.refreshAll("已删除最后一个字符。");
            }
            case "clearEntry" -> {
                computer.clearEntry();
                window.refreshAll("已清空当前输入。");
            }
            case "clearAll" -> {
                computer.clearAll();
                window.refreshAll("已清空表达式和结果。");
            }
            case "ans" -> {
                computer.appendLastResult();
                window.refreshAll("已插入最近结果。");
            }
            case "equals" -> handleEvaluate();
            case "replayHistory" -> replaySelectedHistory();
            case "clearHistory" -> {
                computer.clearHistory();
                window.refreshAll("历史记录已清空。");
            }
            case "copyExpression" -> copyToClipboard(computer.getExpressionText(), "表达式已复制到剪贴板。");
            case "copyResult" -> copyToClipboard(computer.getResultText(), "结果已复制到剪贴板。");
            case "exportHistory" -> exportHistory();
            default -> window.refreshAll("未识别的操作命令。");
        }
    }

    /**
     * 回放当前选中的历史记录。
     */
    public void replaySelectedHistory() {
        CalculationRecord record = window.getSelectedHistoryRecord();
        if (record == null) {
            window.refreshAll("请先在右侧历史列表中选择一条记录。");
            return;
        }
        computer.loadRecord(record);
        window.refreshAll("已回放所选历史记录。");
    }

    /**
     * 打开保存对话框并导出历史记录。
     */
    public void exportHistory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导出计算历史");
        if (chooser.showSaveDialog(window) != JFileChooser.APPROVE_OPTION) {
            window.refreshAll("已取消导出。");
            return;
        }
        Path target = chooser.getSelectedFile().toPath();
        try {
            computer.exportHistory(target);
            window.refreshAll("历史记录已导出到: " + target);
        } catch (IOException exception) {
            window.refreshAll("历史导出失败: " + exception.getMessage());
        }
    }

    /**
     * 将文本复制到系统剪贴板。
     *
     * @param text 要复制的文本
     * @param status 复制成功后的提示
     */
    private void copyToClipboard(String text, String status) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        window.refreshAll(status);
    }
}
