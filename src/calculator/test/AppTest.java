package calculator.test;

import calculator.data.CalculationRecord;
import calculator.data.Computer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 基于断言的轻量级测试入口。
 */
public final class AppTest {
    /**
     * 执行全部测试。
     *
     * @param args 启动参数，当前未使用
     * @throws IOException 当临时文件创建失败时抛出
     */
    public static void main(String[] args) throws IOException {
        Path stateFile = Files.createTempFile("calculator-state", ".properties");
        try {
            runExpressionTests(stateFile);
            runMemoryTests(stateFile);
            runPersistenceTests(stateFile);
            System.out.println("所有计算器测试均已通过。");
        } finally {
            Files.deleteIfExists(stateFile);
        }
    }

    /**
     * 验证表达式优先级、括号、一元运算和科学函数。
     *
     * @param stateFile 隔离使用的状态文件
     */
    private static void runExpressionTests(Path stateFile) {
        Computer computer = new Computer(stateFile);
        computer.clearHistory();
        computer.clearAll();

        computer.appendToken("2+3*4");
        assertTrue(computer.evaluate(), "2+3*4 应当可以成功计算。");
        assertEquals("14", computer.getResultText(), "必须正确处理四则运算优先级。");

        computer.appendToken("(1+2)^3");
        assertTrue(computer.evaluate(), "(1+2)^3 应当可以成功计算。");
        assertEquals("27", computer.getResultText(), "必须正确处理括号与乘方。");

        computer.appendToken("sqrt(81)+recip(4)");
        assertTrue(computer.evaluate(), "sqrt 与 recip 应当可以成功计算。");
        assertEquals("9.25", computer.getResultText(), "sqrt(81)+recip(4) 应等于 9.25。");

        computer.appendToken("sin(pi/2)+log(100)+ln(e)");
        assertTrue(computer.evaluate(), "科学函数应当可以成功计算。");
        assertEquals("4", computer.getResultText(), "sin(pi/2)+log(100)+ln(e) 应等于 4。");

        computer.appendToken("50%");
        assertTrue(computer.evaluate(), "百分比表达式应当可以成功计算。");
        assertEquals("0.5", computer.getResultText(), "50% 应等于 0.5。");

        computer.appendToken("sqrt(-1)");
        assertTrue(!computer.evaluate(), "sqrt(-1) 应当计算失败。");
        assertContains(computer.getErrorMessage());
    }

    /**
     * 验证记忆寄存器相关功能。
     *
     * @param stateFile 隔离使用的状态文件
     */
    private static void runMemoryTests(Path stateFile) {
        Computer computer = new Computer(stateFile);
        computer.clearAll();
        computer.clearHistory();
        computer.memoryClear();

        computer.appendToken("12.5");
        assertTrue(computer.memoryStore(), "MS 应当能够写入当前值。");
        assertEquals("12.5", computer.getMemoryValueText(), "记忆值应为 12.5。");

        computer.clearEntry();
        computer.memoryRecall();
        assertEquals("12.5", computer.getExpressionText(), "MR 应当回填记忆值。");

        computer.clearEntry();
        computer.appendToken("7.5");
        assertTrue(computer.memoryAdd(), "M+ 应当能够累加当前值。");
        assertEquals("20", computer.getMemoryValueText(), "累加后记忆值应为 20。");

        computer.clearEntry();
        computer.appendToken("5");
        assertTrue(computer.memorySubtract(), "M- 应当能够减去当前值。");
        assertEquals("15", computer.getMemoryValueText(), "减法后记忆值应为 15。");

        computer.memoryClear();
        assertEquals("0", computer.getMemoryValueText(), "MC 应当清空记忆值。");
    }

    /**
     * 验证历史记录和记忆值的持久化恢复。
     *
     * @param stateFile 隔离使用的状态文件
     */
    private static void runPersistenceTests(Path stateFile) {
        Computer firstRun = new Computer(stateFile);
        firstRun.clearHistory();
        firstRun.clearAll();
        firstRun.memoryClear();

        firstRun.appendToken("3+4");
        assertTrue(firstRun.evaluate(), "3+4 应当可以成功计算。");
        assertTrue(firstRun.memoryStore(), "结果应当可以写入记忆。");

        Computer secondRun = new Computer(stateFile);
        assertEquals("7", secondRun.getResultText(), "必须恢复最近结果。");
        assertEquals("7", secondRun.getMemoryValueText(), "必须恢复记忆值。");
        assertTrue(secondRun.getHistorySize() >= 1, "必须至少恢复一条历史记录。");

        CalculationRecord latestRecord = secondRun.getHistoryEntries()
                .getLast();
        assertEquals("3+4", latestRecord.expression(), "历史记录中必须保留表达式。");
        assertEquals("7", latestRecord.result(), "历史记录中必须保留结果。");
    }

    /**
     * 断言条件为真。
     *
     * @param condition 条件
     * @param message 失败时的错误消息
     */
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * 断言两个字符串完全相等。
     *
     * @param expected 期望值
     * @param actual 实际值
     * @param message 失败时的错误消息
     */
    private static void assertEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException(message + " 期望值: " + expected + "，实际值: " + actual);
        }
    }

    /**
     * 断言字符串包含指定子串。
     *
     * @param actual 实际文本
     */
    private static void assertContains(String actual) {
        if (actual == null || !actual.contains("负数不能开平方")) {
            throw new IllegalStateException("负数开平方必须给出明确错误。" + " 实际消息: " + actual);
        }
    }
}
