package calculator.data;

import java.io.Serial;

/**
 * 表示计算表达式或格式化结果时出现的业务异常。
 */
public class CalculationException extends RuntimeException {
    /**
     * 运行时异常的序列化版本号。
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 使用错误消息创建异常。
     *
     * @param message 面向用户的错误描述
     */
    public CalculationException(String message) {
        super(message);
    }
}
