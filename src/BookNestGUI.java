import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BookNestGUI extends JFrame {

    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final LibraryService libraryService = new LibraryService();

    // Navigation Cards
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);

    // Sidebar navigation buttons
    private final JButton[] navButtons = new JButton[6];
    private String currentView = "DASHBOARD";

    // Top Header Labels
    private JLabel viewTitleLabel;
    private JLabel viewSubtitleLabel;

    // Dashboard widgets
    private UITheme.StatCard statTotalBooks;
    private UITheme.StatCard statTotalCopies;
    private UITheme.StatCard statTotalMembers;
    private UITheme.StatCard statActiveLoans;
    private DefaultTableModel dashboardActiveLoansModel;

    // Books view
    private DefaultTableModel booksTableModel;
    private JTable booksTable;
    private UITheme.ModernTextField booksSearchField;

    // Members view
    private DefaultTableModel membersTableModel;
    private JTable membersTable;
    private UITheme.ModernTextField membersSearchField;

    // Issue Book view
    private JComboBox<String> issueMemberCombo;
    private JComboBox<String> issueBookCombo;
    private JLabel issueMemberStatusLabel;
    private JLabel issueBookStatusLabel;
    private DefaultTableModel issueMemberLoansModel;

    // Return Book view
    private DefaultTableModel returnLoansModel;
    private JTable returnLoansTable;
    private JLabel returnSelectedLoanLabel;
    private JLabel returnDaysLabel;
    private JLabel returnFineLabel;
    private UITheme.ModernButton confirmReturnBtn;
    private BorrowRecordDetail selectedReturnRecord = null;

    // Borrow History view
    private DefaultTableModel historyTableModel;
    private JTable historyTable;
    private JComboBox<String> historyFilterCombo;
    private UITheme.ModernTextField historySearchField;
    private JLabel historySummaryLabel;

    public BookNestGUI() {
        setTitle("BookNest • Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1060, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BG);

        initUI();
        refreshAllData();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Left Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // 2. Main Container (Top Header + Card Content)
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(UITheme.COLOR_BG);
        mainContainer.add(createTopHeader(), BorderLayout.NORTH);

        // Card Content Views
        contentCards.setOpaque(false);
        contentCards.add(createDashboardPanel(), "DASHBOARD");
        contentCards.add(createBooksPanel(), "BOOKS");
        contentCards.add(createMembersPanel(), "MEMBERS");
        contentCards.add(createIssuePanel(), "ISSUE");
        contentCards.add(createReturnPanel(), "RETURN");
        contentCards.add(createHistoryPanel(), "HISTORY");

        mainContainer.add(contentCards, BorderLayout.CENTER);
        add(mainContainer, BorderLayout.CENTER);
    }

    // ==========================================
    // SIDEBAR
    // ==========================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(UITheme.COLOR_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.COLOR_BORDER));

        // Brand Logo & Title
        JPanel brandPanel = new JPanel(new BorderLayout(12, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(26, 22, 26, 22));

        JLabel logoBadge = new JLabel("📖", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.COLOR_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setPreferredSize(new Dimension(44, 44));
        logoBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        logoBadge.setForeground(Color.WHITE);

        JPanel brandTextPanel = new JPanel();
        brandTextPanel.setLayout(new BoxLayout(brandTextPanel, BoxLayout.Y_AXIS));
        brandTextPanel.setOpaque(false);

        JLabel brandTitle = new JLabel("BookNest");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brandTitle.setForeground(UITheme.COLOR_TEXT);

        JLabel brandSubtitle = new JLabel("Library Management");
        brandSubtitle.setFont(UITheme.FONT_SMALL);
        brandSubtitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        brandTextPanel.add(brandTitle);
        brandTextPanel.add(Box.createVerticalStrut(2));
        brandTextPanel.add(brandSubtitle);

        brandPanel.add(logoBadge, BorderLayout.WEST);
        brandPanel.add(brandTextPanel, BorderLayout.CENTER);

        // Navigation Items
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        String[][] navItems = {
                {"📊  Dashboard", "DASHBOARD"},
                {"📚  Books Catalog", "BOOKS"},
                {"👥  Members Directory", "MEMBERS"},
                {"🔄  Issue Book", "ISSUE"},
                {"📥  Return & Fines", "RETURN"},
                {"📜  Borrow History", "HISTORY"}
        };

        for (int i = 0; i < navItems.length; i++) {
            final String viewId = navItems[i][1];
            final String text = navItems[i][0];
            final int index = i;

            JButton btn = new JButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (currentView.equals(viewId)) {
                        g2.setColor(UITheme.COLOR_PRIMARY);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(30, 41, 59));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            btn.setPreferredSize(new Dimension(220, 46));
            btn.setFont(UITheme.FONT_BODY_BOLD);
            btn.setForeground(i == 0 ? Color.WHITE : UITheme.COLOR_TEXT_MUTED);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(new EmptyBorder(0, 16, 0, 16));
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> switchView(viewId, index));
            navButtons[i] = btn;
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(6));
        }

        // Sidebar Footer Status Widget
        UITheme.RoundedPanel statusPanel = new UITheme.RoundedPanel(12, new Color(24, 34, 53), UITheme.COLOR_BORDER);
        statusPanel.setLayout(new BorderLayout(8, 6));
        statusPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel statusDot = new JLabel("● MySQL 8.0 Connected");
        statusDot.setFont(UITheme.FONT_BODY_BOLD);
        statusDot.setForeground(UITheme.COLOR_SUCCESS);

        JLabel statusSub = new JLabel("localhost:3306 • booknest");
        statusSub.setFont(UITheme.FONT_SMALL);
        statusSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        statusPanel.add(statusDot, BorderLayout.NORTH);
        statusPanel.add(statusSub, BorderLayout.SOUTH);

        JPanel footerWrapper = new JPanel(new BorderLayout());
        footerWrapper.setOpaque(false);
        footerWrapper.setBorder(new EmptyBorder(16, 14, 20, 14));
        footerWrapper.add(statusPanel, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(footerWrapper, BorderLayout.SOUTH);

        return sidebar;
    }

    private void switchView(String viewId, int btnIndex) {
        currentView = viewId;
        cardLayout.show(contentCards, viewId);

        for (int i = 0; i < navButtons.length; i++) {
            if (i == btnIndex) {
                navButtons[i].setForeground(Color.WHITE);
            } else {
                navButtons[i].setForeground(UITheme.COLOR_TEXT_MUTED);
            }
            navButtons[i].repaint();
        }

        switch (viewId) {
            case "DASHBOARD":
                viewTitleLabel.setText("Overview & Analytics");
                viewSubtitleLabel.setText("Live metrics, inventory summary, and circulation status");
                break;
            case "BOOKS":
                viewTitleLabel.setText("Book Inventory");
                viewSubtitleLabel.setText("Manage titles, authors, and real-time physical copy availability");
                break;
            case "MEMBERS":
                viewTitleLabel.setText("Member Directory");
                viewSubtitleLabel.setText("Registered library patrons and their active borrow quotas");
                break;
            case "ISSUE":
                viewTitleLabel.setText("Issue Book");
                viewSubtitleLabel.setText("Circulate copies to members (Max 3 active loans allowed per member)");
                break;
            case "RETURN":
                viewTitleLabel.setText("Return & Fine Assessment");
                viewSubtitleLabel.setText("Process returned books and automatically calculate overdue fines");
                break;
            case "HISTORY":
                viewTitleLabel.setText("Circulation History");
                viewSubtitleLabel.setText("Comprehensive transaction logs and audit trail");
                break;
        }

        refreshAllData();
    }

    // ==========================================
    // TOP HEADER
    // ==========================================
    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.COLOR_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
                new EmptyBorder(18, 28, 18, 28)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        viewTitleLabel = new JLabel("Overview & Analytics");
        viewTitleLabel.setFont(UITheme.FONT_TITLE);
        viewTitleLabel.setForeground(UITheme.COLOR_TEXT);

        viewSubtitleLabel = new JLabel("Live metrics, inventory summary, and circulation status");
        viewSubtitleLabel.setFont(UITheme.FONT_BODY);
        viewSubtitleLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        titlePanel.add(viewTitleLabel);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(viewSubtitleLabel);

        // Action Buttons (Refresh Data)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setOpaque(false);

        UITheme.ModernButton refreshBtn = UITheme.ModernButton.secondary("↻ Refresh Data");
        refreshBtn.addActionListener(e -> {
            refreshAllData();
            UITheme.showSuccess(this, "Data refreshed from MySQL database successfully.", "Refreshed");
        });

        actionPanel.add(refreshBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);

        return header;
    }

    // ==========================================
    // 1. DASHBOARD PANEL
    // ==========================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 22));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top 4 Stat Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 18, 0));
        statsGrid.setOpaque(false);

        statTotalBooks = new UITheme.StatCard("Total Titles", "0", "0 distinct books", UITheme.COLOR_PRIMARY, "📚");
        statTotalCopies = new UITheme.StatCard("Total Copies", "0", "0 available to borrow", UITheme.COLOR_CYAN, "📖");
        statTotalMembers = new UITheme.StatCard("Active Members", "0", "Registered patrons", UITheme.COLOR_SUCCESS, "👥");
        statActiveLoans = new UITheme.StatCard("Active Loans", "0", "₹0.00 fines collected", UITheme.COLOR_WARNING, "🔄");

        statsGrid.add(statTotalBooks);
        statsGrid.add(statTotalCopies);
        statsGrid.add(statTotalMembers);
        statsGrid.add(statActiveLoans);

        panel.add(statsGrid, BorderLayout.NORTH);

        // Center Content: Quick Action Buttons & Active Loans Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 18));
        centerPanel.setOpaque(false);

        // Quick Actions Row
        UITheme.RoundedPanel actionCard = new UITheme.RoundedPanel(14, UITheme.COLOR_CARD, UITheme.COLOR_BORDER);
        actionCard.setLayout(new BorderLayout());
        actionCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel actionHeader = new JLabel("Quick Actions");
        actionHeader.setFont(UITheme.FONT_SUBTITLE);
        actionHeader.setForeground(UITheme.COLOR_TEXT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnRow.setOpaque(false);

        UITheme.ModernButton addBookBtn = UITheme.ModernButton.primary("+ Add New Book");
        addBookBtn.addActionListener(e -> showAddBookDialog());

        UITheme.ModernButton addMemberBtn = UITheme.ModernButton.success("+ Register Member");
        addMemberBtn.addActionListener(e -> showAddMemberDialog());

        UITheme.ModernButton issueBtn = UITheme.ModernButton.secondary("Issue Book →");
        issueBtn.addActionListener(e -> switchView("ISSUE", 3));

        UITheme.ModernButton returnBtn = UITheme.ModernButton.warning("Return Book →");
        returnBtn.addActionListener(e -> switchView("RETURN", 4));

        btnRow.add(addBookBtn);
        btnRow.add(addMemberBtn);
        btnRow.add(issueBtn);
        btnRow.add(returnBtn);

        actionCard.add(actionHeader, BorderLayout.NORTH);
        actionCard.add(btnRow, BorderLayout.CENTER);

        centerPanel.add(actionCard, BorderLayout.NORTH);

        // Active Loans Table Card
        UITheme.RoundedPanel activeTableCard = new UITheme.RoundedPanel(14, UITheme.COLOR_CARD, UITheme.COLOR_BORDER);
        activeTableCard.setLayout(new BorderLayout(0, 12));
        activeTableCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel activeHeaderPanel = new JPanel(new BorderLayout());
        activeHeaderPanel.setOpaque(false);

        JLabel tableTitle = new JLabel("Currently Active Borrowings");
        tableTitle.setFont(UITheme.FONT_SUBTITLE);
        tableTitle.setForeground(UITheme.COLOR_TEXT);

        JLabel tableSub = new JLabel("Books currently circulating with patrons");
        tableSub.setFont(UITheme.FONT_SMALL);
        tableSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel tableTitleBox = new JPanel();
        tableTitleBox.setLayout(new BoxLayout(tableTitleBox, BoxLayout.Y_AXIS));
        tableTitleBox.setOpaque(false);
        tableTitleBox.add(tableTitle);
        tableTitleBox.add(tableSub);

        activeHeaderPanel.add(tableTitleBox, BorderLayout.WEST);

        String[] cols = {"Record ID", "Member ID", "Member Name", "Book ID", "Book Title", "Issue Date", "Days Active"};
        dashboardActiveLoansModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable activeTable = new JTable(dashboardActiveLoansModel);
        UITheme.styleTable(activeTable);

        activeTableCard.add(activeHeaderPanel, BorderLayout.NORTH);
        activeTableCard.add(UITheme.createModernScrollPane(activeTable), BorderLayout.CENTER);

        centerPanel.add(activeTableCard, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // 2. BOOKS CATALOG PANEL
    // ==========================================
    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Top Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(14, 0));
        toolbar.setOpaque(false);

        booksSearchField = new UITheme.ModernTextField("🔍  Search books by title, author, or book ID...");
        booksSearchField.setPreferredSize(new Dimension(380, 42));
        booksSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchBooks(booksSearchField.getText().trim());
            }
        });

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        UITheme.ModernButton addBtn = UITheme.ModernButton.primary("+ Add New Book");
        addBtn.addActionListener(e -> showAddBookDialog());

        UITheme.ModernButton deleteBtn = UITheme.ModernButton.danger("Delete Book");
        deleteBtn.addActionListener(e -> deleteSelectedBook());

        rightActions.add(addBtn);
        rightActions.add(deleteBtn);

        toolbar.add(booksSearchField, BorderLayout.WEST);
        toolbar.add(rightActions, BorderLayout.EAST);

        // Table
        String[] cols = {"Book ID", "Title", "Author", "Total Copies", "Available Copies", "Status"};
        booksTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        booksTable = new JTable(booksTableModel);
        UITheme.styleTable(booksTable);

        // Status badge renderer for available copies
        booksTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasF, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasF, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(UITheme.FONT_BODY_BOLD);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));

                String status = val != null ? val.toString() : "";
                if (status.contains("Available")) {
                    l.setForeground(UITheme.COLOR_SUCCESS);
                } else if (status.contains("Low Stock")) {
                    l.setForeground(UITheme.COLOR_WARNING);
                } else {
                    l.setForeground(UITheme.COLOR_DANGER);
                }
                if (!isSel) {
                    l.setBackground(r % 2 == 0 ? UITheme.COLOR_BG : UITheme.COLOR_TABLE_ROW_ALT);
                }
                return l;
            }
        });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(UITheme.createModernScrollPane(booksTable), BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // 3. MEMBERS DIRECTORY PANEL
    // ==========================================
    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(14, 0));
        toolbar.setOpaque(false);

        membersSearchField = new UITheme.ModernTextField("🔍  Filter members by name or member ID...");
        membersSearchField.setPreferredSize(new Dimension(380, 42));
        membersSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterMembers(membersSearchField.getText().trim());
            }
        });

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        UITheme.ModernButton addBtn = UITheme.ModernButton.success("+ Register Member");
        addBtn.addActionListener(e -> showAddMemberDialog());

        UITheme.ModernButton deleteBtn = UITheme.ModernButton.danger("Delete Member");
        deleteBtn.addActionListener(e -> deleteSelectedMember());

        rightActions.add(addBtn);
        rightActions.add(deleteBtn);

        toolbar.add(membersSearchField, BorderLayout.WEST);
        toolbar.add(rightActions, BorderLayout.EAST);

        // Table
        String[] cols = {"Member ID", "Member Name", "Active Loans", "Max Limit", "Borrowing Status"};
        membersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        membersTable = new JTable(membersTableModel);
        UITheme.styleTable(membersTable);

        membersTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasF, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasF, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(UITheme.FONT_BODY_BOLD);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));

                String status = val != null ? val.toString() : "";
                if (status.contains("Limit Reached")) {
                    l.setForeground(UITheme.COLOR_DANGER);
                } else {
                    l.setForeground(UITheme.COLOR_SUCCESS);
                }
                if (!isSel) {
                    l.setBackground(r % 2 == 0 ? UITheme.COLOR_BG : UITheme.COLOR_TABLE_ROW_ALT);
                }
                return l;
            }
        });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(UITheme.createModernScrollPane(membersTable), BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // 4. ISSUE BOOK PANEL
    // ==========================================
    private JPanel createIssuePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 24, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Left: Issue Form Card
        UITheme.RoundedPanel formCard = new UITheme.RoundedPanel(16, UITheme.COLOR_CARD, UITheme.COLOR_BORDER);
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel formHeader = new JPanel();
        formHeader.setLayout(new BoxLayout(formHeader, BoxLayout.Y_AXIS));
        formHeader.setOpaque(false);

        JLabel formTitle = new JLabel("Issue Book to Member");
        formTitle.setFont(UITheme.FONT_HEADER);
        formTitle.setForeground(UITheme.COLOR_TEXT);

        JLabel formSub = new JLabel("Members may borrow up to 3 books concurrently for up to 7 free days.");
        formSub.setFont(UITheme.FONT_SMALL);
        formSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        formHeader.add(formTitle);
        formHeader.add(Box.createVerticalStrut(4));
        formHeader.add(formSub);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setOpaque(false);
        fields.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Member Selector
        JLabel memLabel = new JLabel("Select Member:");
        memLabel.setFont(UITheme.FONT_BODY_BOLD);
        memLabel.setForeground(UITheme.COLOR_TEXT);

        issueMemberCombo = new JComboBox<>();
        issueMemberCombo.setFont(UITheme.FONT_BODY);
        issueMemberCombo.setBackground(UITheme.COLOR_INPUT_BG);
        issueMemberCombo.setForeground(UITheme.COLOR_TEXT);
        issueMemberCombo.setPreferredSize(new Dimension(300, 40));
        issueMemberCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        issueMemberCombo.addActionListener(e -> onIssueMemberChanged());

        issueMemberStatusLabel = new JLabel("Select a member to view active loan limit");
        issueMemberStatusLabel.setFont(UITheme.FONT_SMALL);
        issueMemberStatusLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        // Book Selector
        JLabel bookLabel = new JLabel("Select Book:");
        bookLabel.setFont(UITheme.FONT_BODY_BOLD);
        bookLabel.setForeground(UITheme.COLOR_TEXT);

        issueBookCombo = new JComboBox<>();
        issueBookCombo.setFont(UITheme.FONT_BODY);
        issueBookCombo.setBackground(UITheme.COLOR_INPUT_BG);
        issueBookCombo.setForeground(UITheme.COLOR_TEXT);
        issueBookCombo.setPreferredSize(new Dimension(300, 40));
        issueBookCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        issueBookCombo.addActionListener(e -> onIssueBookChanged());

        issueBookStatusLabel = new JLabel("Select a book to verify copy availability");
        issueBookStatusLabel.setFont(UITheme.FONT_SMALL);
        issueBookStatusLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        fields.add(memLabel);
        fields.add(Box.createVerticalStrut(6));
        fields.add(issueMemberCombo);
        fields.add(Box.createVerticalStrut(4));
        fields.add(issueMemberStatusLabel);
        fields.add(Box.createVerticalStrut(18));
        fields.add(bookLabel);
        fields.add(Box.createVerticalStrut(6));
        fields.add(issueBookCombo);
        fields.add(Box.createVerticalStrut(4));
        fields.add(issueBookStatusLabel);

        // Submit Button
        UITheme.ModernButton submitBtn = UITheme.ModernButton.primary("Confirm & Issue Book");
        submitBtn.setPreferredSize(new Dimension(0, 46));
        submitBtn.addActionListener(e -> processIssueBook());

        formCard.add(formHeader, BorderLayout.NORTH);
        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(submitBtn, BorderLayout.SOUTH);

        // Right: Active Loans for selected member
        UITheme.RoundedPanel rightCard = new UITheme.RoundedPanel(16, UITheme.COLOR_CARD, UITheme.COLOR_BORDER);
        rightCard.setLayout(new BorderLayout(0, 14));
        rightCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel rightTitle = new JLabel("Selected Member's Active Borrowings");
        rightTitle.setFont(UITheme.FONT_HEADER);
        rightTitle.setForeground(UITheme.COLOR_TEXT);

        String[] cols = {"Record ID", "Book ID", "Title", "Issue Date"};
        issueMemberLoansModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable loansTable = new JTable(issueMemberLoansModel);
        UITheme.styleTable(loansTable);

        rightCard.add(rightTitle, BorderLayout.NORTH);
        rightCard.add(UITheme.createModernScrollPane(loansTable), BorderLayout.CENTER);

        panel.add(formCard);
        panel.add(rightCard);

        return panel;
    }

    private void onIssueMemberChanged() {
        String selected = (String) issueMemberCombo.getSelectedItem();
        if (selected == null || selected.startsWith("--")) {
            issueMemberStatusLabel.setText("Please select a member");
            issueMemberLoansModel.setRowCount(0);
            return;
        }

        String memberId = selected.split(" - ")[0].trim();
        try {
            int activeLoans = borrowDAO.getActiveLoanCountForMember(memberId);
            if (activeLoans >= 3) {
                issueMemberStatusLabel.setText("⚠️ Loan limit reached: " + activeLoans + "/3 active loans. Cannot borrow more.");
                issueMemberStatusLabel.setForeground(UITheme.COLOR_DANGER);
            } else {
                issueMemberStatusLabel.setText("✓ Eligible: " + activeLoans + "/3 active loans (" + (3 - activeLoans) + " remaining).");
                issueMemberStatusLabel.setForeground(UITheme.COLOR_SUCCESS);
            }

            // Populate member loans table
            issueMemberLoansModel.setRowCount(0);
            List<BorrowRecordDetail> active = borrowDAO.getActiveDetailedBorrowRecords();
            for (BorrowRecordDetail d : active) {
                if (d.getMemberId().equalsIgnoreCase(memberId)) {
                    issueMemberLoansModel.addRow(new Object[]{
                            d.getRecordId(),
                            d.getBookId(),
                            d.getBookTitle(),
                            d.getIssueDate()
                    });
                }
            }

        } catch (SQLException e) {
            issueMemberStatusLabel.setText("Error loading member loan status.");
        }
    }

    private void onIssueBookChanged() {
        String selected = (String) issueBookCombo.getSelectedItem();
        if (selected == null || selected.startsWith("--")) {
            issueBookStatusLabel.setText("Please select a book");
            return;
        }

        String bookId = selected.split(" - ")[0].trim();
        try {
            Book b = bookDAO.getBookById(bookId);
            if (b != null) {
                if (b.getAvailableCopies() <= 0) {
                    issueBookStatusLabel.setText("✕ Out of stock: 0 copies available for this book.");
                    issueBookStatusLabel.setForeground(UITheme.COLOR_DANGER);
                } else {
                    issueBookStatusLabel.setText("✓ In stock: " + b.getAvailableCopies() + " of " + b.getTotalCopies() + " copies available.");
                    issueBookStatusLabel.setForeground(UITheme.COLOR_SUCCESS);
                }
            }
        } catch (SQLException e) {
            issueBookStatusLabel.setText("Error reading book availability.");
        }
    }

    private void processIssueBook() {
        String memSelected = (String) issueMemberCombo.getSelectedItem();
        String bookSelected = (String) issueBookCombo.getSelectedItem();

        if (memSelected == null || memSelected.startsWith("--") || bookSelected == null || bookSelected.startsWith("--")) {
            UITheme.showError(this, "Please select both a valid member and an available book.", "Selection Required");
            return;
        }

        String memberId = memSelected.split(" - ")[0].trim();
        String bookId = bookSelected.split(" - ")[0].trim();

        try {
            String result = libraryService.issueBook(memberId, bookId);
            if (result.contains("successfully")) {
                UITheme.showSuccess(this, result + "\n\nMember: " + memberId + "\nBook: " + bookId, "Book Issued");
                refreshAllData();
            } else {
                UITheme.showError(this, result, "Issue Rejected");
            }
        } catch (SQLException e) {
            UITheme.showError(this, "Database error while issuing book:\n" + e.getMessage(), "Database Error");
        }
    }

    // ==========================================
    // 5. RETURN BOOK & FINES PANEL
    // ==========================================
    private JPanel createReturnPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Policy Banner
        UITheme.RoundedPanel policyBanner = new UITheme.RoundedPanel(12, new Color(24, 34, 53), UITheme.COLOR_BORDER);
        policyBanner.setLayout(new BorderLayout(14, 0));
        policyBanner.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel infoIcon = new JLabel("ℹ", SwingConstants.CENTER);
        infoIcon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        infoIcon.setForeground(UITheme.COLOR_CYAN);

        JLabel policyText = new JLabel("<html><b>Circulation Policy:</b> Standard lending period is <b>7 Days Free</b>. A fine of <b>₹2.00 per day</b> applies to each overdue day beyond day 7. Returning automatically replenishes shelf inventory.</html>");
        policyText.setFont(UITheme.FONT_BODY);
        policyText.setForeground(UITheme.COLOR_TEXT);

        policyBanner.add(infoIcon, BorderLayout.WEST);
        policyBanner.add(policyText, BorderLayout.CENTER);

        panel.add(policyBanner, BorderLayout.NORTH);

        // Center Content: Active Loans Table + Return Summary Card
        JPanel centerSplit = new JPanel(new BorderLayout(20, 0));
        centerSplit.setOpaque(false);

        // Left Table: Active Loans
        UITheme.RoundedPanel tableCard = new UITheme.RoundedPanel(14, UITheme.COLOR_CARD, UITheme.COLOR_BORDER);
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel listTitle = new JLabel("Select an Active Loan to Return:");
        listTitle.setFont(UITheme.FONT_SUBTITLE);
        listTitle.setForeground(UITheme.COLOR_TEXT);

        String[] cols = {"Record ID", "Member", "Book Title", "Issue Date", "Days Active", "Est. Fine"};
        returnLoansModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        returnLoansTable = new JTable(returnLoansModel);
        UITheme.styleTable(returnLoansTable);

        returnLoansTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onReturnRowSelected();
            }
        });

        tableCard.add(listTitle, BorderLayout.NORTH);
        tableCard.add(UITheme.createModernScrollPane(returnLoansTable), BorderLayout.CENTER);

        centerSplit.add(tableCard, BorderLayout.CENTER);

        // Right Card: Return & Fine Summary
        UITheme.RoundedPanel returnSummaryCard = new UITheme.RoundedPanel(14, UITheme.COLOR_CARD, UITheme.COLOR_BORDER);
        returnSummaryCard.setPreferredSize(new Dimension(360, 0));
        returnSummaryCard.setLayout(new BorderLayout());
        returnSummaryCard.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel sumTitle = new JLabel("Return Summary");
        sumTitle.setFont(UITheme.FONT_HEADER);
        sumTitle.setForeground(UITheme.COLOR_TEXT);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(new EmptyBorder(18, 0, 18, 0));

        returnSelectedLoanLabel = new JLabel("<html><i>Select an active loan from the table to preview return details.</i></html>");
        returnSelectedLoanLabel.setFont(UITheme.FONT_BODY);
        returnSelectedLoanLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        returnDaysLabel = new JLabel("Days Kept: —");
        returnDaysLabel.setFont(UITheme.FONT_BODY_BOLD);
        returnDaysLabel.setForeground(UITheme.COLOR_TEXT);

        returnFineLabel = new JLabel("Fine Due: —");
        returnFineLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        returnFineLabel.setForeground(UITheme.COLOR_SUCCESS);

        detailsPanel.add(returnSelectedLoanLabel);
        detailsPanel.add(Box.createVerticalStrut(18));
        detailsPanel.add(returnDaysLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(returnFineLabel);

        confirmReturnBtn = UITheme.ModernButton.warning("Confirm Return & Collect Fine");
        confirmReturnBtn.setEnabled(false);
        confirmReturnBtn.setPreferredSize(new Dimension(0, 46));
        confirmReturnBtn.addActionListener(e -> processReturnBook());

        returnSummaryCard.add(sumTitle, BorderLayout.NORTH);
        returnSummaryCard.add(detailsPanel, BorderLayout.CENTER);
        returnSummaryCard.add(confirmReturnBtn, BorderLayout.SOUTH);

        centerSplit.add(returnSummaryCard, BorderLayout.EAST);
        panel.add(centerSplit, BorderLayout.CENTER);

        return panel;
    }

    private void onReturnRowSelected() {
        int selectedRow = returnLoansTable.getSelectedRow();
        if (selectedRow < 0) {
            confirmReturnBtn.setEnabled(false);
            returnSelectedLoanLabel.setText("<html><i>Select an active loan from the table to preview return details.</i></html>");
            returnDaysLabel.setText("Days Kept: —");
            returnFineLabel.setText("Fine Due: —");
            selectedReturnRecord = null;
            return;
        }

        int recordId = (int) returnLoansModel.getValueAt(selectedRow, 0);

        try {
            List<BorrowRecordDetail> active = borrowDAO.getActiveDetailedBorrowRecords();
            for (BorrowRecordDetail d : active) {
                if (d.getRecordId() == recordId) {
                    selectedReturnRecord = d;

                    LocalDate issueDate = d.getIssueDate();
                    LocalDate today = LocalDate.now();
                    long daysBetween = ChronoUnit.DAYS.between(issueDate, today);
                    int daysKept = Math.toIntExact(daysBetween + 1);
                    double fine = Math.max(0, daysKept - 7) * 2.0;

                    returnSelectedLoanLabel.setText("<html><b>Borrower:</b> " + d.getMemberName() + " (" + d.getMemberId() + ")<br/>" +
                            "<b>Book:</b> " + d.getBookTitle() + " (" + d.getBookId() + ")<br/>" +
                            "<b>Issue Date:</b> " + d.getIssueDate() + "<br/>" +
                            "<b>Return Date:</b> " + today + " (Today)</html>");

                    returnDaysLabel.setText("Days Kept: " + daysKept + " days (Grace Period: 7 days)");

                    if (fine > 0) {
                        returnFineLabel.setText("Fine Due: ₹" + String.format("%.2f", fine));
                        returnFineLabel.setForeground(UITheme.COLOR_DANGER);
                    } else {
                        returnFineLabel.setText("Fine Due: ₹0.00 (On Time)");
                        returnFineLabel.setForeground(UITheme.COLOR_SUCCESS);
                    }

                    confirmReturnBtn.setEnabled(true);
                    break;
                }
            }
        } catch (SQLException e) {
            UITheme.showError(this, "Could not fetch return record details: " + e.getMessage(), "Error");
        }
    }

    private void processReturnBook() {
        if (selectedReturnRecord == null) return;

        int recordId = selectedReturnRecord.getRecordId();
        try {
            String result = libraryService.returnBook(recordId);
            if (result.contains("successfully")) {
                UITheme.showSuccess(this, result, "Return Processed");
                selectedReturnRecord = null;
                confirmReturnBtn.setEnabled(false);
                refreshAllData();
            } else {
                UITheme.showError(this, result, "Return Failed");
            }
        } catch (SQLException e) {
            UITheme.showError(this, "Database error during book return:\n" + e.getMessage(), "Error");
        }
    }

    // ==========================================
    // 6. BORROW HISTORY PANEL
    // ==========================================
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Toolbar: Filter combo + Search
        JPanel toolbar = new JPanel(new BorderLayout(14, 0));
        toolbar.setOpaque(false);

        historySearchField = new UITheme.ModernTextField("🔍  Filter history by member name, book title, or record ID...");
        historySearchField.setPreferredSize(new Dimension(380, 42));
        historySearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyHistoryFilter();
            }
        });

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        JLabel filterLabel = new JLabel("Status:");
        filterLabel.setFont(UITheme.FONT_BODY_BOLD);
        filterLabel.setForeground(UITheme.COLOR_TEXT);

        historyFilterCombo = UITheme.createModernComboBox(new String[]{"All Transactions", "Active Loans Only", "Returned Books Only"});
        historyFilterCombo.setPreferredSize(new Dimension(190, 40));
        historyFilterCombo.addActionListener(e -> applyHistoryFilter());

        rightBox.add(filterLabel);
        rightBox.add(historyFilterCombo);

        toolbar.add(historySearchField, BorderLayout.WEST);
        toolbar.add(rightBox, BorderLayout.EAST);

        // Table
        String[] cols = {"Record ID", "Member ID", "Member Name", "Book ID", "Book Title", "Issue Date", "Return Date", "Days Kept", "Fine (₹)", "Status"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        UITheme.styleTable(historyTable);

        // Status pill renderer
        historyTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasF, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasF, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(UITheme.FONT_BODY_BOLD);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));

                String status = val != null ? val.toString() : "";
                if ("Active Loan".equals(status)) {
                    l.setForeground(UITheme.COLOR_WARNING);
                } else {
                    l.setForeground(UITheme.COLOR_CYAN);
                }
                if (!isSel) {
                    l.setBackground(r % 2 == 0 ? UITheme.COLOR_BG : UITheme.COLOR_TABLE_ROW_ALT);
                }
                return l;
            }
        });

        // Summary footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 4, 4, 4));

        historySummaryLabel = new JLabel("Total Transactions: 0");
        historySummaryLabel.setFont(UITheme.FONT_BODY_BOLD);
        historySummaryLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        footer.add(historySummaryLabel, BorderLayout.WEST);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(UITheme.createModernScrollPane(historyTable), BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // DATA REFRESHING & FILTERING
    // ==========================================
    public void refreshAllData() {
        try {
            // 1. Dashboard Stats
            int totalBooks = bookDAO.getTotalBooksCount();
            int totalCopies = bookDAO.getTotalCopiesCount();
            int availCopies = bookDAO.getAvailableCopiesCount();
            int totalMembers = memberDAO.getTotalMembersCount();
            int activeLoans = borrowDAO.getActiveLoansCount();
            double totalFines = borrowDAO.getTotalFinesCollected();

            statTotalBooks.setValue(String.valueOf(totalBooks));
            statTotalBooks.setSubtitle(totalCopies + " physical copies total");

            statTotalCopies.setValue(String.valueOf(availCopies));
            statTotalCopies.setSubtitle((totalCopies - availCopies) + " currently issued out");

            statTotalMembers.setValue(String.valueOf(totalMembers));
            statTotalMembers.setSubtitle("Patrons registered");

            statActiveLoans.setValue(String.valueOf(activeLoans));
            statActiveLoans.setSubtitle("₹" + String.format("%.2f", totalFines) + " fines collected");

            // 2. Active Loans (Dashboard + Return Table)
            List<BorrowRecordDetail> activeList = borrowDAO.getActiveDetailedBorrowRecords();

            dashboardActiveLoansModel.setRowCount(0);
            returnLoansModel.setRowCount(0);
            LocalDate today = LocalDate.now();

            for (BorrowRecordDetail d : activeList) {
                long days = ChronoUnit.DAYS.between(d.getIssueDate(), today) + 1;
                double estFine = Math.max(0, days - 7) * 2.0;

                dashboardActiveLoansModel.addRow(new Object[]{
                        d.getRecordId(), d.getMemberId(), d.getMemberName(),
                        d.getBookId(), d.getBookTitle(), d.getIssueDate(), days + " days"
                });

                returnLoansModel.addRow(new Object[]{
                        d.getRecordId(),
                        d.getMemberName() + " (" + d.getMemberId() + ")",
                        d.getBookTitle() + " (" + d.getBookId() + ")",
                        d.getIssueDate(),
                        days + " days",
                        "₹" + String.format("%.2f", estFine)
                });
            }

            // 3. Books Table
            loadBooksTable(bookDAO.getAllBooks());

            // 4. Members Table
            loadMembersTable(memberDAO.getAllMembers());

            // 5. Issue Selectors
            updateIssueSelectors();

            // 6. History Table
            applyHistoryFilter();

        } catch (SQLException e) {
            UITheme.showError(this, "Failed to load library data:\n" + e.getMessage(), "Database Error");
        }
    }

    private void loadBooksTable(List<Book> books) {
        booksTableModel.setRowCount(0);
        for (Book b : books) {
            String status;
            if (b.getAvailableCopies() == 0) {
                status = "● Out of Stock (0)";
            } else if (b.getAvailableCopies() <= 2) {
                status = "● Low Stock (" + b.getAvailableCopies() + ")";
            } else {
                status = "● Available (" + b.getAvailableCopies() + ")";
            }

            booksTableModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getTotalCopies(),
                    b.getAvailableCopies(),
                    status
            });
        }
    }

    private void loadMembersTable(List<Member> members) throws SQLException {
        membersTableModel.setRowCount(0);
        for (Member m : members) {
            int active = borrowDAO.getActiveLoanCountForMember(m.getMemberId());
            String status = active >= 3 ? "● Limit Reached (3/3)" : "● Eligible (" + active + "/3)";

            membersTableModel.addRow(new Object[]{
                    m.getMemberId(),
                    m.getName(),
                    active,
                    "3 Books",
                    status
            });
        }
    }

    private void updateIssueSelectors() throws SQLException {
        issueMemberCombo.removeAllItems();
        issueMemberCombo.addItem("-- Select Member --");
        List<Member> members = memberDAO.getAllMembers();
        for (Member m : members) {
            int active = borrowDAO.getActiveLoanCountForMember(m.getMemberId());
            issueMemberCombo.addItem(m.getMemberId() + " - " + m.getName() + " (" + active + "/3 loans)");
        }

        issueBookCombo.removeAllItems();
        issueBookCombo.addItem("-- Select Book --");
        List<Book> books = bookDAO.getAllBooks();
        for (Book b : books) {
            issueBookCombo.addItem(b.getBookId() + " - " + b.getTitle() + " (" + b.getAvailableCopies() + " avail)");
        }
    }

    private void searchBooks(String query) {
        try {
            if (query.isEmpty()) {
                loadBooksTable(bookDAO.getAllBooks());
            } else {
                loadBooksTable(bookDAO.searchBooksByTitle(query));
            }
        } catch (SQLException e) {
            UITheme.showError(this, "Search error: " + e.getMessage(), "Search Failed");
        }
    }

    private void filterMembers(String query) {
        try {
            List<Member> all = memberDAO.getAllMembers();
            if (query.isEmpty()) {
                loadMembersTable(all);
            } else {
                String q = query.toLowerCase();
                List<Member> filtered = all.stream()
                        .filter(m -> m.getName().toLowerCase().contains(q) || m.getMemberId().toLowerCase().contains(q))
                        .toList();
                loadMembersTable(filtered);
            }
        } catch (SQLException e) {
            UITheme.showError(this, "Filter error: " + e.getMessage(), "Error");
        }
    }

    private void applyHistoryFilter() {
        try {
            String selectedFilter = (String) historyFilterCombo.getSelectedItem();
            String query = historySearchField.getText().trim().toLowerCase();

            List<BorrowRecordDetail> all = borrowDAO.getDetailedBorrowRecords();
            historyTableModel.setRowCount(0);

            double finesInView = 0.0;
            int countInView = 0;

            for (BorrowRecordDetail d : all) {
                // Filter by dropdown status
                if ("Active Loans Only".equals(selectedFilter) && d.isReturned()) continue;
                if ("Returned Books Only".equals(selectedFilter) && !d.isReturned()) continue;

                // Search query match
                if (!query.isEmpty()) {
                    boolean match = String.valueOf(d.getRecordId()).contains(query)
                            || d.getMemberId().toLowerCase().contains(query)
                            || d.getMemberName().toLowerCase().contains(query)
                            || d.getBookId().toLowerCase().contains(query)
                            || d.getBookTitle().toLowerCase().contains(query);
                    if (!match) continue;
                }

                countInView++;
                finesInView += d.getFine();

                historyTableModel.addRow(new Object[]{
                        d.getRecordId(),
                        d.getMemberId(),
                        d.getMemberName(),
                        d.getBookId(),
                        d.getBookTitle(),
                        d.getIssueDate(),
                        d.getReturnDate() != null ? d.getReturnDate() : "—",
                        d.getDaysKept() != null ? d.getDaysKept() : "—",
                        "₹" + String.format("%.2f", d.getFine()),
                        d.isReturned() ? "Returned" : "Active Loan"
                });
            }

            historySummaryLabel.setText("Showing " + countInView + " transactions • Total Fines: ₹" + String.format("%.2f", finesInView));

        } catch (SQLException e) {
            UITheme.showError(this, "Error filtering circulation history: " + e.getMessage(), "Error");
        }
    }

    // ==========================================
    // ACTION DIALOGS (ADD BOOK & REGISTER MEMBER)
    // ==========================================
    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        UITheme.RoundedPanel panel = new UITheme.RoundedPanel(16, UITheme.COLOR_CARD, UITheme.COLOR_BORDER);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(24, 26, 24, 26));

        JLabel title = new JLabel("Add New Book Title");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.COLOR_TEXT);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setOpaque(false);

        UITheme.ModernTextField idField = new UITheme.ModernTextField("e.g. B002");
        UITheme.ModernTextField titleField = new UITheme.ModernTextField("e.g. Clean Code");
        UITheme.ModernTextField authorField = new UITheme.ModernTextField("e.g. Robert C. Martin");
        UITheme.ModernTextField copiesField = new UITheme.ModernTextField("e.g. 5");

        fields.add(createFieldRow("Book ID:", idField));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFieldRow("Title:", titleField));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFieldRow("Author:", authorField));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFieldRow("Total Copies:", copiesField));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        UITheme.ModernButton cancelBtn = UITheme.ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        UITheme.ModernButton saveBtn = UITheme.ModernButton.primary("Save Book");
        saveBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String bTitle = titleField.getText().trim();
            String author = authorField.getText().trim();
            String copiesStr = copiesField.getText().trim();

            if (id.isEmpty() || bTitle.isEmpty() || author.isEmpty() || copiesStr.isEmpty()) {
                UITheme.showError(dialog, "All fields are required.", "Validation Error");
                return;
            }

            int copies;
            try {
                copies = Integer.parseInt(copiesStr);
                if (copies <= 0) {
                    UITheme.showError(dialog, "Total copies must be a positive number greater than 0.", "Validation Error");
                    return;
                }
            } catch (NumberFormatException ex) {
                UITheme.showError(dialog, "Please enter a valid whole number for copies.", "Validation Error");
                return;
            }

            try {
                Book newBook = new Book(id, bTitle, author, copies, copies);
                if (bookDAO.addBook(newBook)) {
                    dialog.dispose();
                    UITheme.showSuccess(this, "Book '" + bTitle + "' added to catalog successfully!", "Success");
                    refreshAllData();
                } else {
                    UITheme.showError(dialog, "Could not add book. Please check for duplicate ID.", "Error");
                }
            } catch (SQLException ex) {
                UITheme.showError(dialog, "Database error: " + ex.getMessage(), "Error");
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showAddMemberDialog() {
        JDialog dialog = new JDialog(this, "Register New Member", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        UITheme.RoundedPanel panel = new UITheme.RoundedPanel(16, UITheme.COLOR_CARD, UITheme.COLOR_BORDER);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(24, 26, 24, 26));

        JLabel title = new JLabel("Register New Library Member");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.COLOR_TEXT);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setOpaque(false);

        UITheme.ModernTextField idField = new UITheme.ModernTextField("e.g. M001");
        UITheme.ModernTextField nameField = new UITheme.ModernTextField("e.g. Priya Sharma");

        fields.add(createFieldRow("Member ID:", idField));
        fields.add(Box.createVerticalStrut(12));
        fields.add(createFieldRow("Full Name:", nameField));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        UITheme.ModernButton cancelBtn = UITheme.ModernButton.secondary("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        UITheme.ModernButton saveBtn = UITheme.ModernButton.success("Register");
        saveBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                UITheme.showError(dialog, "Both Member ID and Name are required.", "Validation Error");
                return;
            }

            try {
                Member newMember = new Member(id, name);
                if (memberDAO.addMember(newMember)) {
                    dialog.dispose();
                    UITheme.showSuccess(this, "Member '" + name + "' registered successfully!", "Success");
                    refreshAllData();
                } else {
                    UITheme.showError(dialog, "Could not register member. Check for duplicate ID.", "Error");
                }
            } catch (SQLException ex) {
                UITheme.showError(dialog, "Database error: " + ex.getMessage(), "Error");
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(6, 4));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.COLOR_TEXT);
        row.add(lbl, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private void deleteSelectedBook() {
        int row = booksTable.getSelectedRow();
        if (row < 0) {
            UITheme.showInfo(this, "Please select a book from the table first.", "Selection Needed");
            return;
        }

        String bookId = (String) booksTableModel.getValueAt(row, 0);
        String bookTitle = (String) booksTableModel.getValueAt(row, 1);

        boolean confirm = UITheme.showConfirm(this,
                "Are you sure you want to delete book <b>" + bookTitle + "</b> (" + bookId + ")?<br/><br/>" +
                        "Note: Books with active borrowings or history cannot be deleted.",
                "Confirm Deletion");

        if (confirm) {
            try {
                if (bookDAO.deleteBook(bookId)) {
                    UITheme.showSuccess(this, "Book deleted successfully.", "Deleted");
                    refreshAllData();
                } else {
                    UITheme.showError(this, "Book could not be deleted.", "Error");
                }
            } catch (SQLException e) {
                UITheme.showError(this, e.getMessage(), "Deletion Prohibited");
            }
        }
    }

    private void deleteSelectedMember() {
        int row = membersTable.getSelectedRow();
        if (row < 0) {
            UITheme.showInfo(this, "Please select a member from the table first.", "Selection Needed");
            return;
        }

        String memberId = (String) membersTableModel.getValueAt(row, 0);
        String memberName = (String) membersTableModel.getValueAt(row, 1);

        boolean confirm = UITheme.showConfirm(this,
                "Are you sure you want to delete member <b>" + memberName + "</b> (" + memberId + ")?<br/><br/>" +
                        "Note: Members with active borrowings or loan history cannot be deleted.",
                "Confirm Deletion");

        if (confirm) {
            try {
                if (memberDAO.deleteMember(memberId)) {
                    UITheme.showSuccess(this, "Member deleted successfully.", "Deleted");
                    refreshAllData();
                } else {
                    UITheme.showError(this, "Member could not be deleted.", "Error");
                }
            } catch (SQLException e) {
                UITheme.showError(this, e.getMessage(), "Deletion Prohibited");
            }
        }
    }
}
