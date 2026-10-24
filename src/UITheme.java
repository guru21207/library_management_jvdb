import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class UITheme {

    // Color Palette
    public static final Color COLOR_BG = new Color(15, 23, 42);           // #0F172A
    public static final Color COLOR_SIDEBAR = new Color(18, 26, 44);      // #121A2C
    public static final Color COLOR_CARD = new Color(30, 41, 59);         // #1E293B
    public static final Color COLOR_CARD_HOVER = new Color(38, 52, 75);
    public static final Color COLOR_BORDER = new Color(51, 65, 85);       // #334155
    public static final Color COLOR_PRIMARY = new Color(99, 102, 241);    // #6366F1
    public static final Color COLOR_PRIMARY_HOVER = new Color(79, 70, 229);// #4F46E5
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);    // #10B981
    public static final Color COLOR_WARNING = new Color(245, 158, 11);    // #F59E0B
    public static final Color COLOR_DANGER = new Color(239, 68, 68);      // #EF4444
    public static final Color COLOR_CYAN = new Color(6, 182, 212);        // #06B6D4
    public static final Color COLOR_TEXT = new Color(248, 250, 252);       // #F8FAFC
    public static final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);// #94A3B8
    public static final Color COLOR_INPUT_BG = new Color(15, 23, 42);
    public static final Color COLOR_INPUT_BORDER = new Color(71, 85, 105);
    public static final Color COLOR_INPUT_FOCUS = new Color(99, 102, 241);
    public static final Color COLOR_TABLE_ROW_ALT = new Color(20, 29, 44);
    public static final Color COLOR_SELECTION = new Color(79, 70, 229);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);

    // Rounded panel with optional custom background and border
    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private Color backgroundColor;
        private Color borderColor;
        private int borderWidth = 1;

        public RoundedPanel(int radius, Color bg) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bg;
            this.borderColor = COLOR_BORDER;
            setOpaque(false);
        }

        public RoundedPanel(int radius, Color bg, Color border) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bg;
            this.borderColor = border;
            setOpaque(false);
        }

        public void setBackgroundColor(Color bg) {
            this.backgroundColor = bg;
            repaint();
        }

        public void setBorderColor(Color border) {
            this.borderColor = border;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape shape = new RoundRectangle2D.Float(
                    0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius
            );

            if (backgroundColor != null) {
                g2.setColor(backgroundColor);
                g2.fill(shape);
            }

            if (borderColor != null && borderWidth > 0) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderWidth));
                g2.draw(shape);
            }

            g2.dispose();
        }
    }

    // Modern styled button
    public static class ModernButton extends JButton {
        private Color normalColor;
        private Color hoverColor;
        private Color pressedColor;
        private final int radius;
        private boolean isHovered = false;

        public ModernButton(String text, Color baseColor, Color hoverColor, int radius) {
            super(text);
            this.normalColor = baseColor;
            this.hoverColor = hoverColor;
            this.pressedColor = hoverColor.darker();
            this.radius = radius;

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setFont(FONT_BODY_BOLD);
            setForeground(COLOR_TEXT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 18, 10, 18));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public static ModernButton primary(String text) {
            return new ModernButton(text, COLOR_PRIMARY, COLOR_PRIMARY_HOVER, 10);
        }

        public static ModernButton success(String text) {
            return new ModernButton(text, COLOR_SUCCESS, new Color(5, 150, 105), 10);
        }

        public static ModernButton warning(String text) {
            return new ModernButton(text, COLOR_WARNING, new Color(217, 119, 6), 10);
        }

        public static ModernButton danger(String text) {
            return new ModernButton(text, COLOR_DANGER, new Color(220, 38, 38), 10);
        }

        public static ModernButton secondary(String text) {
            return new ModernButton(text, COLOR_CARD, COLOR_CARD_HOVER, 10);
        }

        public void setBaseColors(Color normal, Color hover) {
            this.normalColor = normal;
            this.hoverColor = hover;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color currentBg = normalColor;
            if (!isEnabled()) {
                currentBg = new Color(51, 65, 85, 160);
            } else if (getModel().isPressed()) {
                currentBg = pressedColor;
            } else if (isHovered) {
                currentBg = hoverColor;
            }

            g2.setColor(currentBg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();

            super.paintComponent(g);
        }
    }

    // Modern text field with placeholder and rounded border
    public static class ModernTextField extends JTextField {
        private String placeholder = "";
        private boolean isFocused = false;
        private final int radius = 10;

        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            init();
        }

        public ModernTextField() {
            init();
        }

        private void init() {
            setOpaque(false);
            setFont(FONT_BODY);
            setForeground(COLOR_TEXT);
            setCaretColor(COLOR_TEXT);
            setBackground(COLOR_INPUT_BG);
            setBorder(new EmptyBorder(10, 14, 10, 14));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fill background
            g2.setColor(COLOR_INPUT_BG);
            g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, radius, radius));

            // Draw border
            g2.setColor(isFocused ? COLOR_INPUT_FOCUS : COLOR_INPUT_BORDER);
            g2.setStroke(new BasicStroke(isFocused ? 1.8f : 1.0f));
            g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));

            g2.dispose();
            super.paintComponent(g);

            // Draw placeholder if empty
            if (getText().isEmpty() && !isFocused && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D gp = (Graphics2D) g.create();
                gp.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                gp.setFont(getFont());
                gp.setColor(COLOR_TEXT_MUTED);
                Insets insets = getInsets();
                FontMetrics fm = gp.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                gp.drawString(placeholder, insets.left, y);
                gp.dispose();
            }
        }
    }

    // Modern styled ComboBox
    public static <T> JComboBox<T> createModernComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(COLOR_INPUT_BG);
        combo.setForeground(COLOR_TEXT);
        combo.setFocusable(false);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(8, 10, 8, 10));
                l.setFont(FONT_BODY);
                if (isSelected) {
                    l.setBackground(COLOR_SELECTION);
                    l.setForeground(COLOR_TEXT);
                } else {
                    l.setBackground(COLOR_CARD);
                    l.setForeground(COLOR_TEXT);
                }
                return l;
            }
        });
        return combo;
    }

    // Style standard JTable with dark slate theme, pill badges, and padded rows
    public static void styleTable(JTable table) {
        table.setRowHeight(42);
        table.setFont(FONT_BODY);
        table.setForeground(COLOR_TEXT);
        table.setBackground(COLOR_BG);
        table.setGridColor(COLOR_BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(COLOR_TEXT);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(COLOR_CARD);
        header.setForeground(COLOR_TEXT);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                          boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                l.setBackground(COLOR_CARD);
                l.setForeground(new Color(203, 213, 225)); // slate-300
                l.setFont(FONT_BODY_BOLD);
                l.setBorder(new EmptyBorder(0, 14, 0, 14));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                return l;
            }
        });

        // Alternating row renderer with padding
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                          boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                l.setBorder(new EmptyBorder(0, 14, 0, 14));
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? COLOR_BG : COLOR_TABLE_ROW_ALT);
                    l.setForeground(COLOR_TEXT);
                } else {
                    l.setBackground(COLOR_SELECTION);
                    l.setForeground(Color.WHITE);
                }
                return l;
            }
        });
    }

    // Sleek Dark ScrollPane with custom scrollbar UI
    public static JScrollPane createModernScrollPane(JComponent component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scroll.getViewport().setBackground(COLOR_BG);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        return scroll;
    }

    // Minimalistic modern scrollbar
    public static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(71, 85, 105);
            this.trackColor = COLOR_BG;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1, thumbBounds.width - 2, thumbBounds.height - 2, 6, 6);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    // Dashboard Stat Card
    public static class StatCard extends RoundedPanel {
        private final JLabel valueLabel;
        private final JLabel subtitleLabel;

        public StatCard(String title, String initialValue, String subtitle, Color accentColor, String iconText) {
            super(14, COLOR_CARD, COLOR_BORDER);
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(18, 20, 18, 20));

            // Top Header: Title and Accent Icon
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(FONT_SUBTITLE);
            titleLabel.setForeground(COLOR_TEXT_MUTED);

            JLabel iconBadge = new JLabel(iconText, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            iconBadge.setPreferredSize(new Dimension(36, 36));
            iconBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            iconBadge.setForeground(accentColor);

            topPanel.add(titleLabel, BorderLayout.WEST);
            topPanel.add(iconBadge, BorderLayout.EAST);

            // Center: Big Value
            valueLabel = new JLabel(initialValue);
            valueLabel.setFont(FONT_STAT_NUM);
            valueLabel.setForeground(COLOR_TEXT);

            // Bottom: Subtitle
            subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(FONT_SMALL);
            subtitleLabel.setForeground(COLOR_TEXT_MUTED);

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);
            centerPanel.add(Box.createVerticalStrut(4));
            centerPanel.add(valueLabel);
            centerPanel.add(Box.createVerticalStrut(4));
            centerPanel.add(subtitleLabel);

            add(topPanel, BorderLayout.NORTH);
            add(centerPanel, BorderLayout.CENTER);
        }

        public void setValue(String val) {
            valueLabel.setText(val);
        }

        public void setSubtitle(String sub) {
            subtitleLabel.setText(sub);
        }
    }

    // Custom Styled Notification Modals
    public static void showSuccess(Component parent, String message, String title) {
        showModernDialog(parent, message, title, "✓", COLOR_SUCCESS);
    }

    public static void showError(Component parent, String message, String title) {
        showModernDialog(parent, message, title, "✕", COLOR_DANGER);
    }

    public static void showInfo(Component parent, String message, String title) {
        showModernDialog(parent, message, title, "ℹ", COLOR_PRIMARY);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        final boolean[] result = {false};
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setSize(420, 210);
        dialog.setLocationRelativeTo(parent);

        RoundedPanel panel = new RoundedPanel(16, COLOR_CARD, COLOR_BORDER);
        panel.setLayout(new BorderLayout(16, 16));
        panel.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel icon = new JLabel("?", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(COLOR_WARNING.getRed(), COLOR_WARNING.getGreen(), COLOR_WARNING.getBlue(), 40));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setPreferredSize(new Dimension(44, 44));
        icon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        icon.setForeground(COLOR_WARNING);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(icon, BorderLayout.WEST);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_TEXT);
        header.add(titleLabel, BorderLayout.CENTER);

        JLabel msgLabel = new JLabel("<html><body style='width: 300px;'>" + message + "</body></html>");
        msgLabel.setFont(FONT_BODY);
        msgLabel.setForeground(COLOR_TEXT_MUTED);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton cancelBtn = ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        ModernButton confirmBtn = ModernButton.danger("Confirm");
        confirmBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);

        panel.add(header, BorderLayout.NORTH);
        panel.add(msgLabel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);

        return result[0];
    }

    private static void showModernDialog(Component parent, String message, String title, String iconSymbol, Color iconColor) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setSize(440, 200);
        dialog.setLocationRelativeTo(parent);

        RoundedPanel panel = new RoundedPanel(16, COLOR_CARD, COLOR_BORDER);
        panel.setLayout(new BorderLayout(14, 14));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel icon = new JLabel(iconSymbol, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 40));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setPreferredSize(new Dimension(42, 42));
        icon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        icon.setForeground(iconColor);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(icon, BorderLayout.WEST);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_TEXT);
        header.add(titleLabel, BorderLayout.CENTER);

        JLabel msgLabel = new JLabel("<html><body style='width: 320px;'>" + message + "</body></html>");
        msgLabel.setFont(FONT_BODY);
        msgLabel.setForeground(COLOR_TEXT_MUTED);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);

        ModernButton okBtn = ModernButton.primary("OK");
        okBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(okBtn);

        panel.add(header, BorderLayout.NORTH);
        panel.add(msgLabel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
