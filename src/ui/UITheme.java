package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Modern, clean UI theme constants and component styling helpers
 * suitable for a professional academic desktop application.
 */
public class UITheme {
    // Color Palette
    public static final Color PRIMARY = new Color(30, 41, 59);        // Slate 800
    public static final Color PRIMARY_DARK = new Color(15, 23, 42);   // Slate 900
    public static final Color ACCENT = new Color(37, 99, 235);        // Blue 600
    public static final Color ACCENT_HOVER = new Color(29, 78, 216);  // Blue 700
    public static final Color BG_MAIN = new Color(248, 250, 252);     // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate 200
    public static final Color TEXT_DARK = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    
    public static final Color SUCCESS = new Color(22, 163, 74);       // Green 600
    public static final Color SUCCESS_BG = new Color(240, 253, 244);  // Green 50
    public static final Color WARNING = new Color(217, 119, 6);       // Amber 600
    public static final Color WARNING_BG = new Color(254, 243, 199);  // Amber 50
    public static final Color DANGER = new Color(220, 38, 38);        // Red 600
    public static final Color DANGER_BG = new Color(254, 242, 242);   // Red 50

    // Typography
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_SUBHEADER = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_METRIC = new Font("SansSerif", Font.BOLD, 26);

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, ACCENT, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = createButton(text, Color.WHITE, TEXT_DARK);
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(FONT_REGULAR);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(219, 234, 254)); // Light blue highlight
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249)); // Slate 100
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);

        // Center render column text where appropriate
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Custom Status badge renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    String val = (value != null) ? value.toString() : "";
                    
                    if (val.equalsIgnoreCase("Available") || val.equalsIgnoreCase("Completed") || val.equalsIgnoreCase("Returned")) {
                        c.setForeground(SUCCESS);
                    } else if (val.toLowerCase().contains("overdue") || val.equalsIgnoreCase("Under Maintenance")) {
                        c.setForeground(DANGER);
                    } else if (val.equalsIgnoreCase("Scheduled") || val.equalsIgnoreCase("In Progress") || val.toLowerCase().contains("partially")) {
                        c.setForeground(WARNING);
                    } else {
                        c.setForeground(TEXT_DARK);
                    }
                }
                return c;
            }
        });
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }
}
