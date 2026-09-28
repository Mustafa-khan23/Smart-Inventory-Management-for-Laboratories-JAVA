package ui;

import manager.InventoryManager;
import manager.IssueReturnManager;
import manager.MaintenanceManager;
import model.IssueRecord;
import model.MaintenanceRecord;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard Panel displaying high-level laboratory inventory metrics,
 * quick summary statistics, and recent activity tables.
 */
public class DashboardPanel extends JPanel {
    private final InventoryManager inventoryManager;
    private final IssueReturnManager issueReturnManager;
    private final MaintenanceManager maintenanceManager;
    private final MainDashboardFrame mainFrame;

    private JLabel lblTotalEquipment;
    private JLabel lblAvailableEquipment;
    private JLabel lblIssuedEquipment;
    private JLabel lblUnderMaintenance;
    private JLabel lblUpcomingMaintenance;

    private DefaultTableModel recentIssuesModel;
    private DefaultTableModel upcomingMaintenanceModel;

    public DashboardPanel(InventoryManager invMgr, IssueReturnManager issMgr,
                          MaintenanceManager maintMgr, MainDashboardFrame mainFrame) {
        this.inventoryManager = invMgr;
        this.issueReturnManager = issMgr;
        this.maintenanceManager = maintMgr;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Top Section: Title & Quick Actions
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel headerTextPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        headerTextPanel.setOpaque(false);
        JLabel title = new JLabel("Laboratory Inventory Overview");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel subtitle = new JLabel("Real-time summary of laboratory apparatus, active checkouts, and maintenance");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        headerTextPanel.add(title);
        headerTextPanel.add(subtitle);

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quickActions.setOpaque(false);
        
        JButton btnQuickIssue = UITheme.createPrimaryButton("+ Issue Equipment");
        btnQuickIssue.addActionListener(e -> mainFrame.showCard(MainDashboardFrame.CARD_ISSUE));

        JButton btnQuickReturn = UITheme.createSuccessButton("📥 Return Equipment");
        btnQuickReturn.addActionListener(e -> mainFrame.showCard(MainDashboardFrame.CARD_RETURN));

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        quickActions.add(btnQuickIssue);
        quickActions.add(btnQuickReturn);
        quickActions.add(btnRefresh);

        topPanel.add(headerTextPanel, BorderLayout.WEST);
        topPanel.add(quickActions, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Section: Metrics Cards + Activity Tables
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);

        // 5 Metric Cards in a grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 5, 12, 12));
        metricsGrid.setOpaque(false);

        lblTotalEquipment = new JLabel("0");
        lblAvailableEquipment = new JLabel("0");
        lblIssuedEquipment = new JLabel("0");
        lblUnderMaintenance = new JLabel("0");
        lblUpcomingMaintenance = new JLabel("0");

        metricsGrid.add(createMetricCard("Total Equipment Units", lblTotalEquipment, "Registered in catalog", UITheme.PRIMARY));
        metricsGrid.add(createMetricCard("Available Stock", lblAvailableEquipment, "Ready for immediate issue", UITheme.SUCCESS));
        metricsGrid.add(createMetricCard("Issued Items", lblIssuedEquipment, "Currently checked out", UITheme.ACCENT));
        metricsGrid.add(createMetricCard("Under Maintenance", lblUnderMaintenance, "In service / repair", UITheme.WARNING));
        metricsGrid.add(createMetricCard("Upcoming Maintenance", lblUpcomingMaintenance, "Due within 14 days", UITheme.DANGER));

        centerPanel.add(metricsGrid, BorderLayout.NORTH);

        // Two preview tables side by side
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        tablesPanel.setOpaque(false);

        // Recent Issues Table
        JPanel recentIssuesCard = UITheme.createCardPanel();
        recentIssuesCard.setLayout(new BorderLayout(8, 8));
        JLabel lblRecent = new JLabel("Recent Checkout Activity");
        lblRecent.setFont(UITheme.FONT_HEADER);
        lblRecent.setForeground(UITheme.TEXT_DARK);
        recentIssuesCard.add(lblRecent, BorderLayout.NORTH);

        String[] issueCols = {"Rec ID", "Equipment", "Issued To", "Qty", "Due Date", "Status"};
        recentIssuesModel = new DefaultTableModel(issueCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tableRecentIssues = new JTable(recentIssuesModel);
        UITheme.styleTable(tableRecentIssues);
        JScrollPane scrollRecent = new JScrollPane(tableRecentIssues);
        scrollRecent.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        recentIssuesCard.add(scrollRecent, BorderLayout.CENTER);

        JButton btnViewAllHistory = UITheme.createSecondaryButton("View Full History →");
        btnViewAllHistory.addActionListener(e -> mainFrame.showCard(MainDashboardFrame.CARD_HISTORY));
        JPanel pnlHistBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlHistBtn.setOpaque(false);
        pnlHistBtn.add(btnViewAllHistory);
        recentIssuesCard.add(pnlHistBtn, BorderLayout.SOUTH);

        // Upcoming Maintenance Table
        JPanel maintCard = UITheme.createCardPanel();
        maintCard.setLayout(new BorderLayout(8, 8));
        JLabel lblMaint = new JLabel("Upcoming Maintenance & Calibration");
        lblMaint.setFont(UITheme.FONT_HEADER);
        lblMaint.setForeground(UITheme.TEXT_DARK);
        maintCard.add(lblMaint, BorderLayout.NORTH);

        String[] maintCols = {"ID", "Equipment", "Scheduled Date", "Technician", "Status"};
        upcomingMaintenanceModel = new DefaultTableModel(maintCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tableMaint = new JTable(upcomingMaintenanceModel);
        UITheme.styleTable(tableMaint);
        JScrollPane scrollMaint = new JScrollPane(tableMaint);
        scrollMaint.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        maintCard.add(scrollMaint, BorderLayout.CENTER);

        JButton btnViewAllMaint = UITheme.createSecondaryButton("Manage Maintenance →");
        btnViewAllMaint.addActionListener(e -> mainFrame.showCard(MainDashboardFrame.CARD_MAINTENANCE));
        JPanel pnlMaintBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlMaintBtn.setOpaque(false);
        pnlMaintBtn.add(btnViewAllMaint);
        maintCard.add(pnlMaintBtn, BorderLayout.SOUTH);

        tablesPanel.add(recentIssuesCard);
        tablesPanel.add(maintCard);

        centerPanel.add(tablesPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // Top accent line
        JPanel topIndicator = new JPanel();
        topIndicator.setBackground(accentColor);
        topIndicator.setPreferredSize(new Dimension(0, 3));
        card.add(topIndicator, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(3, 1, 2, 2));
        content.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SUBHEADER);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_METRIC);
        valueLabel.setForeground(UITheme.TEXT_DARK);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(UITheme.TEXT_MUTED);

        content.add(lblTitle);
        content.add(valueLabel);
        content.add(lblSub);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        // Update metric counters
        int totalUnits = inventoryManager.getTotalQuantityUnits();
        int totalTypes = inventoryManager.getTotalEquipmentTypesCount();
        lblTotalEquipment.setText(String.valueOf(totalUnits));

        lblAvailableEquipment.setText(String.valueOf(inventoryManager.getTotalAvailableUnits()));
        lblIssuedEquipment.setText(String.valueOf(inventoryManager.getTotalIssuedUnits()));
        lblUnderMaintenance.setText(String.valueOf(inventoryManager.getUnderMaintenanceCount()));
        lblUpcomingMaintenance.setText(String.valueOf(maintenanceManager.getUpcomingCount()));

        // Populate recent issues (up to 8 items)
        recentIssuesModel.setRowCount(0);
        List<IssueRecord> records = issueReturnManager.getAllRecords();
        int limitIssues = Math.min(8, records.size());
        for (int i = 0; i < limitIssues; i++) {
            IssueRecord r = records.get(i);
            recentIssuesModel.addRow(new Object[]{
                    r.getRecordId(),
                    r.getEquipmentName(),
                    r.getIssuedTo(),
                    r.getQuantity(),
                    r.getExpectedReturnDate(),
                    r.getStatus()
            });
        }

        // Populate upcoming maintenance (up to 8 items)
        upcomingMaintenanceModel.setRowCount(0);
        List<MaintenanceRecord> maint = maintenanceManager.getAllMaintenance();
        int limitMaint = Math.min(8, maint.size());
        for (int i = 0; i < limitMaint; i++) {
            MaintenanceRecord m = maint.get(i);
            upcomingMaintenanceModel.addRow(new Object[]{
                    m.getMaintenanceId(),
                    m.getEquipmentName(),
                    m.getMaintenanceDate(),
                    m.getTechnician(),
                    m.getStatus()
            });
        }
    }
}
