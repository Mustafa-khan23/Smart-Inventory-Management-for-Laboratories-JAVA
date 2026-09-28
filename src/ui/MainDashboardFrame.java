package ui;

import manager.InventoryManager;
import manager.IssueReturnManager;
import manager.LoginManager;
import manager.MaintenanceManager;
import model.User;
import util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Application Window housing the navigation bar, CardLayout content panels,
 * role badge, and status indicators.
 */
public class MainDashboardFrame extends JFrame {
    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_EQUIPMENT = "EQUIPMENT";
    public static final String CARD_ISSUE = "ISSUE";
    public static final String CARD_RETURN = "RETURN";
    public static final String CARD_HISTORY = "HISTORY";
    public static final String CARD_MAINTENANCE = "MAINTENANCE";
    public static final String CARD_REPORTS = "REPORTS";

    private final LoginManager loginManager;
    private final InventoryManager inventoryManager;
    private final IssueReturnManager issueReturnManager;
    private final MaintenanceManager maintenanceManager;

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private DashboardPanel dashboardPanel;
    private EquipmentPanel equipmentPanel;
    private IssuePanel issuePanel;
    private ReturnPanel returnPanel;
    private HistoryPanel historyPanel;
    private MaintenancePanel maintenancePanel;
    private ReportPanel reportPanel;

    private final Map<String, JButton> navButtons = new HashMap<>();
    private String currentCard = CARD_DASHBOARD;

    public MainDashboardFrame(LoginManager loginMgr, InventoryManager invMgr,
                              IssueReturnManager issMgr, MaintenanceManager maintMgr) {
        this.loginManager = loginMgr;
        this.inventoryManager = invMgr;
        this.issueReturnManager = issMgr;
        this.maintenanceManager = maintMgr;

        setTitle("Smart Laboratory Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        initComponents();
        showCard(CARD_DASHBOARD);
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG_MAIN);

        // TOP HEADER BAR
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Title and system subtitle
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 1, 1));
        titleBox.setOpaque(false);

        JLabel lblAppTitle = new JLabel("SMART LABORATORY INVENTORY MANAGEMENT");
        lblAppTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAppTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Central Apparatus Catalog, Loan Tracking & Equipment Maintenance");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(new Color(203, 213, 225)); // Slate 300

        titleBox.add(lblAppTitle);
        titleBox.add(lblSub);
        headerPanel.add(titleBox, BorderLayout.WEST);

        // Right side: User Badge & Logout
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userBox.setOpaque(false);

        User currentUser = loginManager.getCurrentUser();
        String userDisplay = (currentUser != null) ? currentUser.getFullName() : "Guest User";
        String roleDisplay = (currentUser != null) ? currentUser.getRole() : "Visitor";

        JLabel lblBadge = new JLabel("👤 Logged in as: " + userDisplay + " (" + roleDisplay + ")");
        lblBadge.setFont(UITheme.FONT_BOLD);
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(UITheme.PRIMARY_DARK);
        lblBadge.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JButton btnLogout = UITheme.createButton("Logout ↩", new Color(239, 68, 68), Color.WHITE);
        btnLogout.addActionListener(e -> handleLogout());

        userBox.add(lblBadge);
        userBox.add(btnLogout);
        headerPanel.add(userBox, BorderLayout.EAST);

        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // NAVIGATION STRIP
        JPanel navStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        navStrip.setBackground(new Color(241, 245, 249)); // Slate 100
        navStrip.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(2, 14, 2, 14)
        ));

        addNavButton(navStrip, CARD_DASHBOARD, "📊 Dashboard");
        addNavButton(navStrip, CARD_EQUIPMENT, "🔬 Equipment Inventory");
        addNavButton(navStrip, CARD_ISSUE, "📤 Issue Equipment");
        addNavButton(navStrip, CARD_RETURN, "📥 Return Equipment");
        addNavButton(navStrip, CARD_HISTORY, "📜 Issue / Return History");
        addNavButton(navStrip, CARD_MAINTENANCE, "🛠️ Maintenance");
        addNavButton(navStrip, CARD_REPORTS, "📈 Reports");

        // CARD CONTENT PANELS
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(UITheme.BG_MAIN);

        dashboardPanel = new DashboardPanel(inventoryManager, issueReturnManager, maintenanceManager, this);
        equipmentPanel = new EquipmentPanel(inventoryManager, loginManager);
        issuePanel = new IssuePanel(inventoryManager, issueReturnManager, loginManager, this);
        returnPanel = new ReturnPanel(inventoryManager, issueReturnManager, loginManager, this);
        historyPanel = new HistoryPanel(issueReturnManager);
        maintenancePanel = new MaintenancePanel(maintenanceManager, inventoryManager, loginManager);
        reportPanel = new ReportPanel(inventoryManager, issueReturnManager, maintenanceManager);

        cardsPanel.add(dashboardPanel, CARD_DASHBOARD);
        cardsPanel.add(equipmentPanel, CARD_EQUIPMENT);
        cardsPanel.add(issuePanel, CARD_ISSUE);
        cardsPanel.add(returnPanel, CARD_RETURN);
        cardsPanel.add(historyPanel, CARD_HISTORY);
        cardsPanel.add(maintenancePanel, CARD_MAINTENANCE);
        cardsPanel.add(reportPanel, CARD_REPORTS);

        // Center Container combining Nav + Cards
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(navStrip, BorderLayout.NORTH);
        centerContainer.add(cardsPanel, BorderLayout.CENTER);
        rootPanel.add(centerContainer, BorderLayout.CENTER);

        // BOTTOM STATUS BAR
        JPanel statusBar = new JPanel(new BorderLayout(8, 2));
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(5, 16, 5, 16)
        ));

        JLabel lblStatusLeft = new JLabel("System: Operational | Connected to Local File Database | Mode: " + roleDisplay);
        lblStatusLeft.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatusLeft.setForeground(UITheme.TEXT_MUTED);

        JLabel lblStatusRight = new JLabel("Today: " + DateUtil.getTodayString() + " | Java Swing GUI Desktop");
        lblStatusRight.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatusRight.setForeground(UITheme.TEXT_MUTED);

        statusBar.add(lblStatusLeft, BorderLayout.WEST);
        statusBar.add(lblStatusRight, BorderLayout.EAST);
        rootPanel.add(statusBar, BorderLayout.SOUTH);

        add(rootPanel);
    }

    private void addNavButton(JPanel container, String cardName, String title) {
        JButton btn = new JButton(title);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(0, 0, 0, 0), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.addActionListener(e -> showCard(cardName));
        container.add(btn);
        navButtons.put(cardName, btn);
    }

    public void showCard(String cardName) {
        this.currentCard = cardName;
        cardLayout.show(cardsPanel, cardName);

        // Update nav button highlights
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(cardName)) {
                btn.setBackground(UITheme.ACCENT);
                btn.setForeground(Color.WHITE);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(UITheme.ACCENT_HOVER, 1, true),
                        new EmptyBorder(8, 14, 8, 14)
                ));
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(UITheme.TEXT_DARK);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(UITheme.BORDER_COLOR, 1, true),
                        new EmptyBorder(8, 14, 8, 14)
                ));
            }
        }

        // Live refresh the activated panel
        switch (cardName) {
            case CARD_DASHBOARD:
                dashboardPanel.refreshData();
                break;
            case CARD_EQUIPMENT:
                equipmentPanel.refreshTable();
                equipmentPanel.applyPermissions();
                break;
            case CARD_ISSUE:
                issuePanel.refreshPanel();
                break;
            case CARD_RETURN:
                returnPanel.refreshPanel();
                break;
            case CARD_HISTORY:
                historyPanel.refreshTable();
                break;
            case CARD_MAINTENANCE:
                maintenancePanel.refreshTable();
                maintenancePanel.applyPermissions();
                break;
            case CARD_REPORTS:
                reportPanel.refreshReport();
                break;
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of the Laboratory Inventory System?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            loginManager.logout();
            this.dispose();

            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(
                        loginManager,
                        inventoryManager,
                        issueReturnManager,
                        maintenanceManager
                );
                loginFrame.setVisible(true);
            });
        }
    }
}
