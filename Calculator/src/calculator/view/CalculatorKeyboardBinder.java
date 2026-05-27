package calculator.view;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 负责为窗口安装键盘快捷键。
 */
public class CalculatorKeyboardBinder {
    /**
     * 根组件，用于注册按键映射。
     */
    private final JComponent target;

    /**
     * 按键触发后调用的动作处理器。
     */
    private final CalculatorActionHandler actionHandler;

    /**
     * 创建按键绑定器。
     *
     * @param target 注册目标
     * @param actionHandler 动作处理器
     */
    public CalculatorKeyboardBinder(JComponent target, CalculatorActionHandler actionHandler) {
        this.target = target;
        this.actionHandler = actionHandler;
    }

    /**
     * 安装全部快捷键。
     */
    public void install() {
        InputMap inputMap = target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = target.getActionMap();
        Map<String, String> inputBindings = createInputBindings();
        for (Map.Entry<String, String> entry : inputBindings.entrySet()) {
            bindTypedKey(inputMap, actionMap, entry.getKey(), () -> actionHandler.handleInput(entry.getValue()));
        }

        bindTypedKey(inputMap, actionMap, "%", () -> actionHandler.handleSpecial("percent"));
        bindTypedKey(inputMap, actionMap, "Q", () -> actionHandler.handleSpecial("sqrt"));
        bindTypedKey(inputMap, actionMap, "q", () -> actionHandler.handleSpecial("sqrt"));
        bindTypedKey(inputMap, actionMap, "R", () -> actionHandler.handleSpecial("recip"));
        bindTypedKey(inputMap, actionMap, "r", () -> actionHandler.handleSpecial("recip"));
        bindTypedKey(inputMap, actionMap, "X", () -> actionHandler.handleSpecial("square"));
        bindTypedKey(inputMap, actionMap, "x", () -> actionHandler.handleSpecial("square"));

        bindPressedKey(inputMap, actionMap, "ENTER", "evaluate", actionHandler::handleEvaluate);
        bindPressedKey(inputMap, actionMap, "BACK_SPACE", "backspace",
                () -> actionHandler.handleSpecial("backspace"));
        bindPressedKey(inputMap, actionMap, "ESCAPE", "clearAll",
                () -> actionHandler.handleSpecial("clearAll"));
    }

    /**
     * 创建文本输入型快捷键映射。
     *
     * @return 快捷键映射
     */
    private Map<String, String> createInputBindings() {
        Map<String, String> bindings = new LinkedHashMap<>();
        bindings.put("1", "1");
        bindings.put("2", "2");
        bindings.put("3", "3");
        bindings.put("4", "4");
        bindings.put("5", "5");
        bindings.put("6", "6");
        bindings.put("7", "7");
        bindings.put("8", "8");
        bindings.put("9", "9");
        bindings.put("0", "0");
        bindings.put(".", ".");
        bindings.put("+", "+");
        bindings.put("-", "-");
        bindings.put("*", "*");
        bindings.put("/", "/");
        bindings.put("^", "^");
        bindings.put("(", "(");
        bindings.put(")", ")");
        bindings.put("p", "pi");
        bindings.put("P", "pi");
        bindings.put("e", "e");
        bindings.put("E", "e");
        bindings.put("s", "sin(");
        bindings.put("S", "sin(");
        bindings.put("c", "cos(");
        bindings.put("C", "cos(");
        bindings.put("t", "tan(");
        bindings.put("T", "tan(");
        bindings.put("l", "log(");
        bindings.put("L", "log(");
        bindings.put("n", "ln(");
        bindings.put("N", "ln(");
        return bindings;
    }

    /**
     * 绑定字符输入型快捷键。
     *
     * @param inputMap 输入映射
     * @param actionMap 动作映射
     * @param key 键字符
     * @param runnable 执行动作
     */
    private void bindTypedKey(InputMap inputMap, ActionMap actionMap, String key, Runnable runnable) {
        String actionKey = "typed:" + key;
        inputMap.put(KeyStroke.getKeyStroke("typed " + key), actionKey);
        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                runnable.run();
            }
        });
    }

    /**
     * 绑定按键事件型快捷键。
     *
     * @param inputMap 输入映射
     * @param actionMap 动作映射
     * @param keyStroke 按键描述
     * @param actionKey 动作键
     * @param runnable 执行动作
     */
    private void bindPressedKey(
            InputMap inputMap,
            ActionMap actionMap,
            String keyStroke,
            String actionKey,
            Runnable runnable) {
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), actionKey);
        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                runnable.run();
            }
        });
    }
}
