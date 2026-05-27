package calculator.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 负责统一格式化计算器中的数值文本。
 */
public final class CalculatorFormatter {
    /**
     * 小数保留位数上限，用于抑制双精度尾差。
     */
    private static final int SCALE = 12;

    /**
     * 将双精度数值格式化为适合界面显示的十进制文本。
     *
     * @param value 原始数值
     * @return 格式化文本
     * @throws CalculationException 当数值不是有限值时抛出
     */
    public static String format(double value) {
        if (!Double.isFinite(value)) {
            throw new CalculationException("结果超出可显示范围。");
        }
        BigDecimal decimal = BigDecimal.valueOf(value)
                .setScale(SCALE, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return decimal.toPlainString();
    }
}
