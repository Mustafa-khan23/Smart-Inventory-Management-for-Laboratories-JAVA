package ui;

import manager.IssueReturnManager;
import model.IssueRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Issue & Return History Panel displaying full audit logs, checkout lifecycles,
 * statuses, and multi-field search and status filters.
 */
public class HistoryPanel extends JPanel {
    private final IssueReturnManager issueReturnManager;

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;

    private JLabel lblTotalCount;
    private JLabel lblActiveCount;
    private JLabel lblReturnedCount;
    private JLabel lblOverdueCount;

    public HistoryPanel(IssueReturnManager issMgr) {
        this.issueReturnManager = issMgr;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        // Top Header
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Issue & Return History Log");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel subtitle = new JLabel("Comprehensive chronological record of all apparatus loans, returns, and overdue statuses");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        topContainer.add(titlePanel, BorderLayout.NORTH);

        // Status Summary Badges Strip
        JPanel summaryStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        summaryStrip.setOpaque(false);

        lblTotalCount = createBadge("Total Checkouts: 0", UITheme.PRIMARY);
        lblActiveCount = createBadge("Active / Pending: 0", UITheme.ACCENT);
        lblReturnedCount = createBadge("Completed Returns: 0", UITheme.SUCCESS);
        lblOverdueCount = createBadge("Overdue: 0", UITheme.DANGER);

        summaryStrip.add(lblTotalCount);
        summaryStrip.add(lblActiveCount);
        summaryStrip.add(lblReturnedCount);
        summaryStrip.add(lblOverdueCount);
        topContainer.add(summaryStrip, BorderLayout.CENTER);

        // Filter Controls
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        txtSearch = UITheme.createTextField(16);
        txtSearch.setToolTipText("Filter by Record ID, Equipment, Borrower, or Role");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        JLabel lblStatus = new JLabel("Status Filter:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        cmbStatusFilter = new JComboBox<>(new String[]{"All", "Issued", "Partially Returned", "Returned", "Overdue"});
        cmbStatusFilter.setFont(UITheme.FONT_REGULAR);
        cmbStatusFilter.addActionListener(e -> applyFilters());

        JButton btnReset = UITheme.createSecondaryButton("Reset Filters");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
            refreshTable();
        });

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshTable());

        filterBar.add(lblSearch);
        filterBar.add(txtSearch);
        filterBar.add(lblStatus);
        filterBar.add(cmbStatusFilter);
        filterBar.add(btnReset);
        filterBar.add(btnRefresh);

        topContainer.add(filterBar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Center Table
        String[] columns = {
                "Record ID", "Equipment ID", "Equipment Name", "Issued To", "Role",
                "Total Qty", "Returned Qty", "Issue Date", "Due Date", "Actual Return Date", "Status"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(tableModel);
        UITheme.styleTable(historyTable);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createBadge(String text, Color color) {
        JLabel badge = new JLabel(text);
        badge.setFont(UITheme.FONT_BOLD);
        badge.setForeground(color);
        badge.setOpaque(true);
        badge.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
        badge.setBorder(new javax.swing.border.CompoundBorder(
                new LineBorder(color, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return badge;
    }

    public void refreshTable() {
        applyFilters();
        updateSummaryCounts();
    }

    private void updateSummaryCounts() {
        lblTotalCount.setText("Total Checkouts: " + issueReturnManager.getTotalRecordsCount());
        lblActiveCount.setText("Active / Pending: " + issueReturnManager.getActiveIssuedRecords().size());
        lblReturnedCount.setText("Completed Returns: " + issueReturnManager.getCompletedReturnsCount());
        lblOverdueCount.setText("Overdue: " + issueReturnManager.getOverdueCount());
    }

    private void applyFilters() {
        String keyword = txtSearch.getText().trim();
        String statusFilter = (String) cmbStatusFilter.getSelectedItem();

        tableModel.setRowCount(0);
        List<IssueRecord> list = issueReturnManager.filterRecords(keyword, statusFilter);
        for (IssueRecord r : list) {
            tableModel.addRow(new Object[]{
                    r.getRecordId(),
                    r.getEquipmentId(),
                    r.getEquipmentName(),
                    r.getIssuedTo(),
                    r.getUserRole(),
                    r.getQuantity(),
                    r.getReturnedQuantity(),
                    r.getIssueDate(),
                    r.getExpectedReturnDate(),
                    r.getActualReturnDate(),
                    r.getStatus()
            });
        }
    }
}
