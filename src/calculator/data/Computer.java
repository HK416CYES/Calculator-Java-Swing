package calculator.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 计算器领域服务，对外提供稳定的业务操作接口。
 */
public class Computer {
    /**
     * 表达式求值器。
     */
    private final ExpressionEvaluator evaluator;

    /**
     * 状态持久化仓储。
     */
    private final CalculatorStateRepository repository;

    /**
     * 当前运行时状态。
     */
    private final CalculatorState state;

    /**
     * 使用默认状态文件创建计算器。
     */
    public Computer() {
        this(Path.of(System.getProperty("user.home"), ".calculator-state.properties"));
    }

    /**
     * 使用指定状态文件创建计算器。
     *
     * @param storagePath 状态文件路径
     */
    public Computer(Path storagePath) {
        this.evaluator = new ExpressionEvaluator();
        this.repository = new CalculatorStateRepository(storagePath);
        this.state = repository.load();
    }

    /**
     * 向当前表达式追加一个输入片段。
     *
     * @param token 输入片段
     */
    public void appendToken(String token) {
        state.clearError();
        if (state.isReplaceExpressionOnNextInput() && shouldStartNewExpression(token)) {
            state.clearExpression();
        } else if (state.isReplaceExpressionOnNextInput() && isBinaryOperator(token)) {
            state.setExpressionText(state.getResultText());
        }
        state.setReplaceExpressionOnNextInput(false);
        state.appendExpression(token);
        persistQuietly();
    }

    /**
     * 追加百分号后缀。
     */
    public void appendPercent() {
        appendToken("%");
    }

    /**
     * 用函数包裹当前表达式。
     *
     * @param functionName 函数名
     */
    public void wrapExpression(String functionName) {
        state.clearError();
        String source = resolveExpressionOrResult();
        state.setExpressionText(functionName + "(" + source + ")");
        state.setReplaceExpressionOnNextInput(false);
        persistQuietly();
    }

    /**
     * 切换当前表达式的正负号。
     */
    public void toggleSign() {
        state.clearError();
        String source = resolveExpressionOrResult();
        if (source.startsWith("-(") && source.endsWith(")")) {
            state.setExpressionText(source.substring(2, source.length() - 1));
        } else {
            state.setExpressionText("-(" + source + ")");
        }
        state.setReplaceExpressionOnNextInput(false);
        persistQuietly();
    }

    /**
     * 删除表达式最后一个字符。
     */
    public void backspace() {
        state.clearError();
        if (state.isReplaceExpressionOnNextInput()) {
            state.setExpressionText(state.getResultText());
            state.setReplaceExpressionOnNextInput(false);
        }
        state.deleteLastExpressionChar();
        persistQuietly();
    }

    /**
     * 清空当前输入表达式。
     */
    public void clearEntry() {
        state.clearError();
        state.clearExpression();
        state.setReplaceExpressionOnNextInput(false);
        persistQuietly();
    }

    /**
     * 清空当前表达式和结果。
     */
    public void clearAll() {
        state.clearError();
        state.clearExpression();
        state.setResultText("0");
        state.setResultValue(0);
        state.setReplaceExpressionOnNextInput(false);
        persistQuietly();
    }

    /**
     * 计算当前表达式。
     *
     * @return 是否计算成功
     */
    public boolean evaluate() {
        state.clearError();
        String expression = state.getExpressionText().trim();
        if (expression.isEmpty()) {
            state.setErrorMessage("请输入表达式后再计算。");
            return false;
        }

        try {
            double value = evaluator.evaluate(expression);
            String resultText = CalculatorFormatter.format(value);
            state.setResultText(resultText);
            state.setResultValue(value);
            state.addHistoryRecord(new CalculationRecord(expression, resultText, LocalDateTime.now()));
            state.setReplaceExpressionOnNextInput(true);
            persistQuietly();
            return true;
        } catch (CalculationException exception) {
            state.setErrorMessage(exception.getMessage());
            return false;
        }
    }

    /**
     * 将当前值写入记忆。
     *
     * @return 是否写入成功
     */
    public boolean memoryStore() {
        Double value = resolveValueForMemory();
        if (value == null) {
            return false;
        }
        state.setMemoryValue(value);
        persistQuietly();
        return true;
    }

    /**
     * 将当前值累加到记忆。
     *
     * @return 是否累加成功
     */
    public boolean memoryAdd() {
        Double value = resolveValueForMemory();
        if (value == null) {
            return false;
        }
        state.setMemoryValue(state.getMemoryValue() + value);
        persistQuietly();
        return true;
    }

    /**
     * 将当前值从记忆中减去。
     *
     * @return 是否减法成功
     */
    public boolean memorySubtract() {
        Double value = resolveValueForMemory();
        if (value == null) {
            return false;
        }
        state.setMemoryValue(state.getMemoryValue() - value);
        persistQuietly();
        return true;
    }

    /**
     * 清空记忆。
     */
    public void memoryClear() {
        state.clearError();
        state.setMemoryValue(0);
        persistQuietly();
    }

    /**
     * 将记忆值回填到表达式。
     */
    public void memoryRecall() {
        state.clearError();
        if (state.isReplaceExpressionOnNextInput()) {
            state.clearExpression();
            state.setReplaceExpressionOnNextInput(false);
        }
        state.appendExpression(CalculatorFormatter.format(state.getMemoryValue()));
        persistQuietly();
    }

    /**
     * 将最近一次结果回填到当前表达式中。
     */
    public void appendLastResult() {
        state.clearError();
        if (state.isReplaceExpressionOnNextInput()) {
            state.clearExpression();
            state.setReplaceExpressionOnNextInput(false);
        }
        state.appendExpression(state.getResultText());
        persistQuietly();
    }

    /**
     * 清空历史记录。
     */
    public void clearHistory() {
        state.clearHistory();
        persistQuietly();
    }

    /**
     * 将一条历史记录重新装载到当前界面状态。
     *
     * @param record 历史记录
     */
    public void loadRecord(CalculationRecord record) {
        state.clearError();
        state.setExpressionText(record.expression());
        state.setResultText(record.result());
        state.setResultValue(parseResultValue(record.result()));
        state.setReplaceExpressionOnNextInput(false);
        persistQuietly();
    }

    /**
     * 导出历史记录到文本文件。
     *
     * @param target 目标文件
     * @throws IOException 当写入失败时抛出
     */
    public void exportHistory(Path target) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            for (CalculationRecord record : state.getHistoryEntries()) {
                writer.write(record.toDisplayText());
                writer.newLine();
            }
        }
    }

    /**
     * 返回当前表达式。
     *
     * @return 表达式文本
     */
    public String getExpressionText() {
        return state.getExpressionText();
    }

    /**
     * 返回当前结果文本。
     *
     * @return 结果文本
     */
    public String getResultText() {
        return state.getResultText();
    }

    /**
     * 返回只读历史记录。
     *
     * @return 历史记录列表
     */
    public List<CalculationRecord> getHistoryEntries() {
        return state.getHistoryEntries();
    }

    /**
     * 返回格式化后的记忆值文本。
     *
     * @return 记忆值文本
     */
    public String getMemoryValueText() {
        return CalculatorFormatter.format(state.getMemoryValue());
    }

    /**
     * 返回当前错误消息。
     *
     * @return 错误消息
     */
    public String getErrorMessage() {
        return state.getErrorMessage();
    }

    /**
     * 判断记忆寄存器是否包含非零值。
     *
     * @return 是否存在记忆值
     */
    public boolean hasMemoryValue() {
        return Math.abs(state.getMemoryValue()) > 1.0E-12;
    }

    /**
     * 返回历史记录条数。
     *
     * @return 历史记录条数
     */
    public int getHistorySize() {
        return state.getHistoryEntries().size();
    }

    /**
     * 静默保存当前状态。
     */
    private void persistQuietly() {
        try {
            repository.save(state);
        } catch (IOException exception) {
            state.setErrorMessage("状态保存失败，但当前会话仍可继续使用。");
        }
    }

    /**
     * 返回“表达式优先，否则结果”的文本来源。
     *
     * @return 可用于继续编辑的文本
     */
    private String resolveExpressionOrResult() {
        String expression = state.getExpressionText().trim();
        return expression.isEmpty() ? state.getResultText() : expression;
    }

    /**
     * 解析当前内容用于记忆操作。
     *
     * @return 可写入记忆的数值；若失败则返回 {@code null}
     */
    private Double resolveValueForMemory() {
        state.clearError();
        try {
            if (!state.getExpressionText().trim().isEmpty() && !state.isReplaceExpressionOnNextInput()) {
                return evaluator.evaluate(state.getExpressionText().trim());
            }
            return state.getResultValue();
        } catch (CalculationException exception) {
            state.setErrorMessage("当前内容无法写入记忆。");
            return null;
        }
    }

    /**
     * 将结果文本恢复为数值形式，用于历史回放后的记忆操作。
     *
     * @param resultText 结果文本
     * @return 结果对应的数值；解析失败时返回 0
     */
    private double parseResultValue(String resultText) {
        try {
            return Double.parseDouble(resultText);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    /**
     * 判断某个输入是否应当开启全新表达式。
     *
     * @param token 输入片段
     * @return 是否应开启新表达式
     */
    private boolean shouldStartNewExpression(String token) {
        return !isBinaryOperator(token) && !"%".equals(token);
    }

    /**
     * 判断某个输入是否为二元运算符。
     *
     * @param token 输入片段
     * @return 是否为二元运算符
     */
    private boolean isBinaryOperator(String token) {
        return "+".equals(token) || "-".equals(token) || "*".equals(token)
                || "/".equals(token) || "^".equals(token);
    }
}
