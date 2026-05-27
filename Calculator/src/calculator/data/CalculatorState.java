package calculator.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 封装计算器运行时状态。
 */
public class CalculatorState {
    /**
     * 历史记录最多保留的条数。
     */
    public static final int MAX_HISTORY_SIZE = 50;

    /**
     * 当前正在编辑的表达式缓冲区。
     */
    private final StringBuilder expressionBuffer;

    /**
     * 当前会话中的历史记录。
     */
    private final List<CalculationRecord> historyEntries;

    /**
     * 最近一次计算结果。
     */
    private String resultText;

    /**
     * 最近一次计算结果对应的原始数值。
     */
    private double resultValue;

    /**
     * 记忆寄存器中的数值。
     */
    private double memoryValue;

    /**
     * 面向用户的错误消息。
     */
    private String errorMessage;

    /**
     * 表示下一次输入是否应替换当前表达式。
     */
    private boolean replaceExpressionOnNextInput;

    /**
     * 创建默认状态对象。
     */
    public CalculatorState() {
        this.expressionBuffer = new StringBuilder();
        this.historyEntries = new ArrayList<>();
        this.resultText = "0";
        this.resultValue = 0;
        this.errorMessage = "";
    }

    /**
     * 返回当前表达式。
     *
     * @return 表达式文本
     */
    public String getExpressionText() {
        return expressionBuffer.toString();
    }

    /**
     * 使用新的表达式完全替换当前内容。
     *
     * @param expression 新表达式
     */
    public void setExpressionText(String expression) {
        expressionBuffer.setLength(0);
        expressionBuffer.append(expression);
    }

    /**
     * 追加表达式片段。
     *
     * @param token 要追加的文本
     */
    public void appendExpression(String token) {
        expressionBuffer.append(token);
    }

    /**
     * 清空表达式。
     */
    public void clearExpression() {
        expressionBuffer.setLength(0);
    }

    /**
     * 删除表达式最后一个字符。
     */
    public void deleteLastExpressionChar() {
        if (!expressionBuffer.isEmpty()) {
            expressionBuffer.deleteCharAt(expressionBuffer.length() - 1);
        }
    }

    /**
     * 返回结果文本。
     *
     * @return 结果文本
     */
    public String getResultText() {
        return resultText;
    }

    /**
     * 设置结果文本。
     *
     * @param resultText 结果文本
     */
    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    /**
     * 返回最近一次结果对应的数值。
     *
     * @return 最近一次结果数值
     */
    public double getResultValue() {
        return resultValue;
    }

    /**
     * 设置最近一次结果对应的数值。
     *
     * @param resultValue 最近一次结果数值
     */
    public void setResultValue(double resultValue) {
        this.resultValue = resultValue;
    }

    /**
     * 返回记忆值。
     *
     * @return 记忆值
     */
    public double getMemoryValue() {
        return memoryValue;
    }

    /**
     * 设置记忆值。
     *
     * @param memoryValue 记忆值
     */
    public void setMemoryValue(double memoryValue) {
        this.memoryValue = memoryValue;
    }

    /**
     * 返回错误消息。
     *
     * @return 错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 设置错误消息。
     *
     * @param errorMessage 错误消息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 清空错误消息。
     */
    public void clearError() {
        this.errorMessage = "";
    }

    /**
     * 返回是否应在下一次输入时替换表达式。
     *
     * @return 是否替换表达式
     */
    public boolean isReplaceExpressionOnNextInput() {
        return replaceExpressionOnNextInput;
    }

    /**
     * 设置下一次输入是否替换表达式。
     *
     * @param replaceExpressionOnNextInput 是否替换
     */
    public void setReplaceExpressionOnNextInput(boolean replaceExpressionOnNextInput) {
        this.replaceExpressionOnNextInput = replaceExpressionOnNextInput;
    }

    /**
     * 追加一条历史记录，并在超限时移除最旧记录。
     *
     * @param record 历史记录
     */
    public void addHistoryRecord(CalculationRecord record) {
        historyEntries.add(record);
        while (historyEntries.size() > MAX_HISTORY_SIZE) {
            historyEntries.removeFirst();
        }
    }

    /**
     * 清空历史记录。
     */
    public void clearHistory() {
        historyEntries.clear();
    }

    /**
     * 追加一组已恢复的历史记录。
     *
     * @param records 历史记录列表
     */
    public void replaceHistory(List<CalculationRecord> records) {
        historyEntries.clear();
        historyEntries.addAll(records);
    }

    /**
     * 返回只读历史记录列表。
     *
     * @return 只读历史记录列表
     */
    public List<CalculationRecord> getHistoryEntries() {
        return Collections.unmodifiableList(historyEntries);
    }
}
