package calculator.view;

import calculator.data.CalculationRecord;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * 负责将历史记录渲染成双行卡片样式。
 */
public class HistoryListCellRenderer extends DefaultListCellRenderer {
    /**
     * Swing 渲染器的序列化版本号。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 渲染一条列表项。
     *
     * @param list 列表组件
     * @param value 当前值
     * @param index 索引
     * @param isSelected 是否选中
     * @param cellHasFocus 是否拥有焦点
     * @return 渲染后的组件
     */
    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        CalculationRecord record = (CalculationRecord) value;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        configureBackground(panel, isSelected);

        JLabel expressionLabel = new JLabel(record.expression());
        expressionLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        expressionLabel.setForeground(isSelected ? new Color(19, 54, 135) : new Color(55, 70, 92));

        JLabel resultLabel = new JLabel("= " + record.result() + "    " + record.createdAt());
        resultLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultLabel.setForeground(new Color(103, 117, 138));

        panel.add(expressionLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(resultLabel);
        return panel;
    }

    /**
     * 根据选中状态设置背景色。
     *
     * @param component 目标组件
     * @param isSelected 是否选中
     */
    private void configureBackground(JComponent component, boolean isSelected) {
        component.setBackground(isSelected ? new Color(220, 232, 255) : Color.WHITE);
    }
}
