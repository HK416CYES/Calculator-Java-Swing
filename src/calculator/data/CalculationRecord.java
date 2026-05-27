package calculator.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 表示一条不可变的计算历史记录。
 *
 * @param expression 用户输入的原始表达式。
 * @param result     该表达式对应的格式化结果。
 * @param createdAt  记录创建时间。
 */
public record CalculationRecord(String expression, String result, LocalDateTime createdAt) {
    /**
     * 历史记录统一使用的时间格式。
     */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 返回表达式文本。
     *
     * @return 表达式文本
     */
    @Override
    public String expression() {
        return expression;
    }

    /**
     * 返回结果文本。
     *
     * @return 结果文本
     */
    @Override
    public String result() {
        return result;
    }

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    @Override
    public LocalDateTime createdAt() {
        return createdAt;
    }

    /**
     * 生成便于界面显示的历史文本。
     *
     * @return 可显示文本
     */
    public String toDisplayText() {
        return FORMATTER.format(createdAt) + " | " + expression + " = " + result;
    }

    /**
     * 返回对象的字符串表示。
     *
     * @return 历史文本
     */
    @Override
    public String toString() {
        return toDisplayText();
    }
}
