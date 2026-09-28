package ui;

import manager.InventoryManager;
import manager.IssueReturnManager;
import manager.MaintenanceManager;
import model.Equipment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reports Panel showing laboratory inventory analytics, category distribution,
 * utilization ratios, and operational summaries.
 */
public class ReportPanel extends JPanel {
    private final InventoryManager inventoryManager;
    private final IssueReturnManager issueReturnManager;
    private final MaintenanceManager maintenanceManager;

    private JLabel lblTotalEquipment;
    private JLabel lblAvailableEquipment;
    private JLabel lblIssuedEquipment;
    private JLabel lblUnderMaintenance;
    private JLabel lblTotalIssues;
    private JLabel lblCompletedReturns;
    private JLabel lblUpcomingMaintenance;

    private DefaultTableModel categoryTableModel;

    public ReportPanel(InventoryManager invMgr, IssueReturnManager issMgr, MaintenanceManager maintMgr) {
        this.inventoryManager = invMgr;
        this.issueReturnManager = issMgr;
        this.maintenanceManager = maintMgr;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        refreshReport();
    }

    private void initComponents() {
        // Top Header
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Laboratory Inventory & Utilization Reports");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel subtitle = new JLabel("Summary analytics, apparatus category breakdowns, and audit figures");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        topContainer.add(titlePanel, BorderLayout.WEST);

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh Report Data");
        btnRefresh.addActionListener(e -> refreshReport());
        topContainer.add(btnRefresh, BorderLayout.EAST);
        add(topContainer, BorderLayout.NORTH);

        // Center Content: Summary Panels + Category Distribution Table
        JPanel centerPanel = new JPanel(new BorderLayout(16, 16));
        centerPanel.setOpaque(false);

        // Top Metrics Grid
        JPanel metricsGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        metricsGrid.setOpaque(false);

        lblTotalEquipment = new JLabel("0");
        lblAvailableEquipment = new JLabel("0");
        lblIssuedEquipment = new JLabel("0");
        lblUnderMaintenance = new JLabel("0");
        lblTotalIssues = new JLabel("0");
        lblCompletedReturns = new JLabel("0");
        lblUpcomingMaintenance = new JLabel("0");
        JLabel lblHealthRate = new JLabel("Operational");

        metricsGrid.add(createReportCard("Total Equipment Units", lblTotalEquipment, UITheme.PRIMARY));
        metricsGrid.add(createReportCard("Available Units", lblAvailableEquipment, UITheme.SUCCESS));
        metricsGrid.add(createReportCard("Currently Issued Units", lblIssuedEquipment, UITheme.ACCENT));
        metricsGrid.add(createReportCard("Under Maintenance", lblUnderMaintenance, UITheme.WARNING));

        metricsGrid.add(createReportCard("Total Checkout Records", lblTotalIssues, UITheme.PRIMARY));
        metricsGrid.add(createReportCard("Completed Returns", lblCompletedReturns, UITheme.SUCCESS));
        metricsGrid.add(createReportCard("Upcoming Maintenance", lblUpcomingMaintenance, UITheme.DANGER));
        metricsGrid.add(createReportCard("System Readiness", lblHealthRate, UITheme.SUCCESS));

        centerPanel.add(metricsGrid, BorderLayout.NORTH);

        // Category Breakdown Card & Table
        JPanel categoryCard = UITheme.createCardPanel();
        categoryCard.setLayout(new BorderLayout(10, 10));

        JLabel lblCatHeader = new JLabel("Equipment Breakdown by Category");
        lblCatHeader.setFont(UITheme.FONT_HEADER);
        lblCatHeader.setForeground(UITheme.TEXT_DARK);
        categoryCard.add(lblCatHeader, BorderLayout.NORTH);

        String[] cols = {"Category Name", "Total Apparatus Units", "Available Stock", "Issued Stock", "Utilization Rate (%)"};
        categoryTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable categoryTable = new JTable(categoryTableModel);
        UITheme.styleTable(categoryTable);

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        categoryCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(categoryCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createReportCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(6, 6));

        JPanel strip = new JPanel();
        strip.setBackground(accentColor);
        strip.setPreferredSize(new Dimension(0, 3));
        card.add(strip, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(2, 1, 4, 4));
        content.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SUBHEADER);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_METRIC);
        valueLabel.setForeground(UITheme.TEXT_DARK);

        content.add(lblTitle);
        content.add(valueLabel);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    public void refreshReport() {
        lblTotalEquipment.setText(String.valueOf(inventoryManager.getTotalQuantityUnits()));
        lblAvailableEquipment.setText(String.valueOf(inventoryManager.getTotalAvailableUnits()));
        lblIssuedEquipment.setText(String.valueOf(inventoryManager.getTotalIssuedUnits()));
        lblUnderMaintenance.setText(String.valueOf(inventoryManager.getUnderMaintenanceCount()));

        lblTotalIssues.setText(String.valueOf(issueReturnManager.getTotalRecordsCount()));
        lblCompletedReturns.setText(String.valueOf(issueReturnManager.getCompletedReturnsCount()));
        lblUpcomingMaintenance.setText(String.valueOf(maintenanceManager.getUpcomingCount()));

        // Category breakdown aggregation
        categoryTableModel.setRowCount(0);
        List<Equipment> equipmentList = inventoryManager.getAllEquipment();

        Map<String, int[]> categoryStats = new HashMap<>(); // [total, available]
        for (Equipment eq : equipmentList) {
            String cat = eq.getCategory();
            categoryStats.putIfAbsent(cat, new int[]{0, 0});
            int[] stats = categoryStats.get(cat);
            stats[0] += eq.getQuantity();
            stats[1] += eq.getAvailableQuantity();
        }

        for (Map.Entry<String, int[]> entry : categoryStats.entrySet()) {
            String cat = entry.getKey();
            int total = entry.getValue()[0];
            int avail = entry.getValue()[1];
            int issued = total - avail;
            double utilRate = (total > 0) ? ((double) issued / total) * 100.0 : 0.0;

            categoryTableModel.addRow(new Object[]{
                    cat,
                    total,
                    avail,
                    issued,
                    String.format("%.1f%%", utilRate)
            });
        }
    }
}
