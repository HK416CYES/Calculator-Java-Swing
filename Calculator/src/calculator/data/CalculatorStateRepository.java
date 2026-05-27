package calculator.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/**
 * 负责将计算器状态持久化到本地文件，并在启动时恢复状态。
 *
 * @param storagePath 状态文件路径。
 */
public record CalculatorStateRepository(Path storagePath) {
    /**
     * 最近结果对应的属性键。
     */
    private static final String LAST_RESULT_KEY = "last.result";

    /**
     * 当前表达式对应的属性键。
     */
    private static final String LAST_EXPRESSION_KEY = "last.expression";

    /**
     * 记忆值对应的属性键。
     */
    private static final String MEMORY_KEY = "memory.value";

    /**
     * 历史记录数量对应的属性键。
     */
    private static final String HISTORY_COUNT_KEY = "history.count";

    /**
     * 使用指定路径创建仓储对象。
     *
     * @param storagePath 状态文件路径
     */
    public CalculatorStateRepository {
    }

    /**
     * 读取并恢复状态；若文件不存在，则返回默认状态。
     *
     * @return 恢复后的状态对象
     */
    public CalculatorState load() {
        CalculatorState state = new CalculatorState();
        if (!Files.exists(storagePath)) {
            return state;
        }

        Properties properties = new Properties();
        try {
            properties.load(Files.newBufferedReader(storagePath, StandardCharsets.UTF_8));
            state.setResultText(properties.getProperty(LAST_RESULT_KEY, "0"));
            state.setResultValue(parseDoubleOrZero(state.getResultText()));
            state.setExpressionText(properties.getProperty(LAST_EXPRESSION_KEY, ""));
            state.setMemoryValue(parseDoubleOrZero(properties.getProperty(MEMORY_KEY, "0")));
            state.replaceHistory(readHistory(properties));
        } catch (IOException exception) {
            state.clearExpression();
            state.setResultText("0");
            state.setResultValue(0);
            state.setMemoryValue(0);
            state.clearHistory();
            state.setErrorMessage("状态文件读取失败，已使用默认状态。");
        }
        return state;
    }

    /**
     * 保存当前状态。
     *
     * @param state 要保存的状态
     * @throws IOException 当写入失败时抛出
     */
    public void save(CalculatorState state) throws IOException {
        if (storagePath.getParent() != null) {
            Files.createDirectories(storagePath.getParent());
        }
        Properties properties = new Properties();
        properties.setProperty(LAST_RESULT_KEY, state.getResultText());
        properties.setProperty(LAST_EXPRESSION_KEY, state.getExpressionText());
        properties.setProperty(MEMORY_KEY, Double.toString(state.getMemoryValue()));
        properties.setProperty(HISTORY_COUNT_KEY, Integer.toString(state.getHistoryEntries().size()));
        writeHistory(properties, state.getHistoryEntries());

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(storagePath), StandardCharsets.UTF_8))) {
            properties.store(writer, "Calculator persistent state");
        }
    }

    /**
     * 返回仓储所使用的状态文件路径。
     *
     * @return 状态文件路径
     */
    @Override
    public Path storagePath() {
        return storagePath;
    }

    /**
     * 从属性对象中读取历史记录。
     *
     * @param properties 属性对象
     * @return 历史记录列表
     */
    private List<CalculationRecord> readHistory(Properties properties) {
        int count = (int) parseDoubleOrZero(properties.getProperty(HISTORY_COUNT_KEY, "0"));
        List<CalculationRecord> records = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String expression = decode(properties.getProperty("history." + index + ".expression"));
            String result = decode(properties.getProperty("history." + index + ".result"));
            if (expression.isEmpty() || result.isEmpty()) {
                continue;
            }
            String createdAtText = properties.getProperty("history." + index + ".createdAt");
            records.add(new CalculationRecord(expression, result, parseDateOrNow(createdAtText)));
        }
        return records;
    }

    /**
     * 将历史记录写入属性对象。
     *
     * @param properties 属性对象
     * @param records    历史记录列表
     */
    private void writeHistory(Properties properties, List<CalculationRecord> records) {
        for (int index = 0; index < records.size(); index++) {
            CalculationRecord record = records.get(index);
            properties.setProperty("history." + index + ".expression", encode(record.expression()));
            properties.setProperty("history." + index + ".result", encode(record.result()));
            properties.setProperty("history." + index + ".createdAt", record.createdAt().toString());
        }
    }

    /**
     * 解析双精度文本，失败时返回 0。
     *
     * @param value 文本值
     * @return 解析结果
     */
    private double parseDoubleOrZero(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    /**
     * 解析时间文本；失败时返回当前时间。
     *
     * @param value 时间文本
     * @return 解析结果
     */
    private LocalDateTime parseDateOrNow(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            return LocalDateTime.now();
        }
    }

    /**
     * 对文本进行 Base64 编码，以保证属性文件存储稳定。
     *
     * @param value 原始文本
     * @return 编码后的文本
     */
    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对 Base64 文本进行解码。
     *
     * @param value 编码文本
     * @return 解码后的文本
     */
    private String decode(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
