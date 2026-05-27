package calculator.data;

/**
 * 负责解析并计算数学表达式。
 */
public class ExpressionEvaluator {
    /**
     * 计算一段表达式文本。
     *
     * @param expression 表达式文本
     * @return 计算结果
     * @throws CalculationException 当表达式非法时抛出
     */
    public double evaluate(String expression) {
        return new Parser(expression).parse();
    }

    /**
     * 递归下降解析器。
     */
    private static final class Parser {
        /**
         * 原始表达式文本。
         */
        private final String text;

        /**
         * 当前解析游标。
         */
        private int index;

        /**
         * 创建解析器。
         *
         * @param text 原始表达式
         */
        private Parser(String text) {
            this.text = text;
        }

        /**
         * 解析完整表达式。
         *
         * @return 计算结果
         */
        private double parse() {
            double value = parseExpression();
            skipWhitespace();
            if (index != text.length()) {
                throw new CalculationException("表达式中存在无法识别的内容。");
            }
            return value;
        }

        /**
         * 解析加减法。
         *
         * @return 本层表达式结果
         */
        private double parseExpression() {
            double value = parseTerm();
            while (true) {
                skipWhitespace();
                if (match('+')) {
                    value += parseTerm();
                } else if (match('-')) {
                    value -= parseTerm();
                } else {
                    return value;
                }
            }
        }

        /**
         * 解析乘除法。
         *
         * @return 项结果
         */
        private double parseTerm() {
            double value = parsePower();
            while (true) {
                skipWhitespace();
                if (match('*')) {
                    value *= parsePower();
                } else if (match('/')) {
                    double divisor = parsePower();
                    if (Math.abs(divisor) < 1.0E-12) {
                        throw new CalculationException("除数不能为 0。");
                    }
                    value /= divisor;
                } else {
                    return value;
                }
            }
        }

        /**
         * 解析乘方运算。
         *
         * @return 幂运算结果
         */
        private double parsePower() {
            double base = parseUnary();
            skipWhitespace();
            if (match('^')) {
                return Math.pow(base, parsePower());
            }
            return base;
        }

        /**
         * 解析一元正负号。
         *
         * @return 一元表达式结果
         */
        private double parseUnary() {
            skipWhitespace();
            if (match('+')) {
                return parseUnary();
            }
            if (match('-')) {
                return -parseUnary();
            }
            return parsePostfix();
        }

        /**
         * 解析后缀百分号。
         *
         * @return 后缀表达式结果
         */
        private double parsePostfix() {
            double value = parsePrimary();
            while (true) {
                skipWhitespace();
                if (match('%')) {
                    value /= 100.0;
                } else {
                    return value;
                }
            }
        }

        /**
         * 解析基本单元，包括括号、数字、常量与函数。
         *
         * @return 基本单元结果
         */
        private double parsePrimary() {
            skipWhitespace();
            if (match('(')) {
                double value = parseExpression();
                skipWhitespace();
                require(')', "缺少右括号。");
                return value;
            }
            if (isLetter(peek())) {
                String identifier = parseIdentifier();
                if ("pi".equals(identifier)) {
                    return Math.PI;
                }
                if ("e".equals(identifier)) {
                    return Math.E;
                }
                skipWhitespace();
                require('(', "函数调用缺少左括号。");
                double argument = parseExpression();
                skipWhitespace();
                require(')', "函数调用缺少右括号。");
                return applyFunction(identifier, argument);
            }
            return parseNumber();
        }

        /**
         * 解析数值字面量。
         *
         * @return 数值结果
         */
        private double parseNumber() {
            skipWhitespace();
            int start = index;
            boolean hasDot = false;
            while (index < text.length()) {
                char current = text.charAt(index);
                if (Character.isDigit(current)) {
                    index++;
                } else if (current == '.' && !hasDot) {
                    hasDot = true;
                    index++;
                } else {
                    break;
                }
            }
            if (start == index) {
                throw new CalculationException("表达式不完整或数字格式错误。");
            }
            try {
                return Double.parseDouble(text.substring(start, index));
            } catch (NumberFormatException exception) {
                throw new CalculationException("数字格式无效。");
            }
        }

        /**
         * 解析标识符。
         *
         * @return 小写标识符
         */
        private String parseIdentifier() {
            int start = index;
            while (index < text.length() && isLetter(text.charAt(index))) {
                index++;
            }
            return text.substring(start, index).toLowerCase();
        }

        /**
         * 根据函数名执行一元数学函数。
         *
         * @param identifier 函数名
         * @param argument 参数
         * @return 函数结果
         */
        private double applyFunction(String identifier, double argument) {
            return switch (identifier) {
                case "sqrt" -> {
                    if (argument < 0) {
                        throw new CalculationException("负数不能开平方。");
                    }
                    yield Math.sqrt(argument);
                }
                case "sin" -> Math.sin(argument);
                case "cos" -> Math.cos(argument);
                case "tan" -> Math.tan(argument);
                case "log" -> {
                    if (argument <= 0) {
                        throw new CalculationException("log 的参数必须大于 0。");
                    }
                    yield Math.log10(argument);
                }
                case "ln" -> {
                    if (argument <= 0) {
                        throw new CalculationException("ln 的参数必须大于 0。");
                    }
                    yield Math.log(argument);
                }
                case "square" -> argument * argument;
                case "recip" -> {
                    if (Math.abs(argument) < 1.0E-12) {
                        throw new CalculationException("0 没有倒数。");
                    }
                    yield 1.0 / argument;
                }
                default -> throw new CalculationException("不支持的函数: " + identifier);
            };
        }

        /**
         * 如果当前位置字符匹配指定字符，则推进游标。
         *
         * @param expected 期望字符
         * @return 是否匹配成功
         */
        private boolean match(char expected) {
            if (peek() == expected) {
                index++;
                return true;
            }
            return false;
        }

        /**
         * 要求当前位置必须是指定字符。
         *
         * @param expected 期望字符
         * @param message 不匹配时的错误消息
         */
        private void require(char expected, String message) {
            if (!match(expected)) {
                throw new CalculationException(message);
            }
        }

        /**
         * 查看当前位置字符但不推进游标。
         *
         * @return 当前字符；如果已到末尾则返回空字符
         */
        private char peek() {
            if (index >= text.length()) {
                return '\0';
            }
            return text.charAt(index);
        }

        /**
         * 跳过空白字符。
         */
        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        /**
         * 判断字符是否为字母。
         *
         * @param candidate 待判断字符
         * @return 是否为字母
         */
        private boolean isLetter(char candidate) {
            return Character.isLetter(candidate);
        }
    }
}
