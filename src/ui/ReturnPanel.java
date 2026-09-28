package ui;

import manager.InventoryManager;
import manager.IssueReturnManager;
import manager.LoginManager;
import model.IssueRecord;
import util.DateUtil;
import util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Return Equipment Panel displaying active checkouts and facilitating
 * full or partial equipment returns with stock replenishment.
 */
public class ReturnPanel extends JPanel {
    private final InventoryManager inventoryManager;
    private final IssueReturnManager issueReturnManager;
    private final LoginManager loginManager;
    private final MainDashboardFrame mainFrame;

    private JTable activeIssuesTable;
    private DefaultTableModel activeIssuesModel;

    private JLabel lblSelectedRecord;
    private JLabel lblEquipmentName;
    private JLabel lblIssuedTo;
    private JLabel lblPendingQuantity;
    private JLabel lblDueDate;

    private JTextField txtReturnQty;
    private JTextField txtReturnDate;
    private JButton btnReturn;

    private IssueRecord currentSelectedRecord = null;

    public ReturnPanel(InventoryManager invMgr, IssueReturnManager issMgr, LoginManager loginMgr, MainDashboardFrame mainFrame) {
        this.inventoryManager = invMgr;
        this.issueReturnManager = issMgr;
        this.loginManager = loginMgr;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        refreshPanel();
    }

    private void initComponents() {
        // Top Header
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        topPanel.setOpaque(false);
        JLabel title = new JLabel("Return Laboratory Equipment");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel subtitle = new JLabel("Process apparatus check-in, update inventory levels, and settle checkout records");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        topPanel.add(title);
        topPanel.add(subtitle);
        add(topPanel, BorderLayout.NORTH);

        // Center Split: Left = Active Issues Table, Right = Return Action Card
        JPanel centerGrid = new JPanel(new BorderLayout(16, 16));
        centerGrid.setOpaque(false);

        // LEFT: Table of Active Checkouts
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Active Unreturned & Overdue Apparatus Checkouts");
        lblTableTitle.setFont(UITheme.FONT_HEADER);
        lblTableTitle.setForeground(UITheme.TEXT_DARK);
        tableHeader.add(lblTableTitle, BorderLayout.WEST);

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshPanel());
        tableHeader.add(btnRefresh, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Record ID", "Equipment ID", "Equipment Name", "Issued To", "Role", "Issued Qty", "Pending Qty", "Due Date", "Status"};
        activeIssuesModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        activeIssuesTable = new JTable(activeIssuesModel);
        UITheme.styleTable(activeIssuesTable);
        activeIssuesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        activeIssuesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleRowSelection();
            }
        });

        JScrollPane scroll = new JScrollPane(activeIssuesTable);
        scroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scroll, BorderLayout.CENTER);

        centerGrid.add(tableCard, BorderLayout.CENTER);

        // RIGHT: Action Panel
        JPanel actionCard = UITheme.createCardPanel();
        actionCard.setPreferredSize(new Dimension(360, 0));
        actionCard.setLayout(new BorderLayout(12, 12));

        JLabel lblActionTitle = new JLabel("Return Process Form");
        lblActionTitle.setFont(UITheme.FONT_HEADER);
        lblActionTitle.setForeground(UITheme.TEXT_DARK);
        actionCard.add(lblActionTitle, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel(new GridLayout(7, 2, 8, 12));
        detailsPanel.setOpaque(false);

        lblSelectedRecord = new JLabel("None selected");
        lblSelectedRecord.setFont(UITheme.FONT_BOLD);
        lblEquipmentName = new JLabel("-");
        lblIssuedTo = new JLabel("-");
        lblPendingQuantity = new JLabel("-");
        lblPendingQuantity.setFont(UITheme.FONT_BOLD);
        lblPendingQuantity.setForeground(UITheme.DANGER);
        lblDueDate = new JLabel("-");

        txtReturnQty = UITheme.createTextField(10);
        txtReturnDate = UITheme.createTextField(10);
        txtReturnDate.setText(DateUtil.getTodayString());

        detailsPanel.add(new JLabel("Record ID:"));
        detailsPanel.add(lblSelectedRecord);

        detailsPanel.add(new JLabel("Equipment:"));
        detailsPanel.add(lblEquipmentName);

        detailsPanel.add(new JLabel("Issued To:"));
        detailsPanel.add(lblIssuedTo);

        detailsPanel.add(new JLabel("Pending Return Qty:"));
        detailsPanel.add(lblPendingQuantity);

        detailsPanel.add(new JLabel("Expected Due Date:"));
        detailsPanel.add(lblDueDate);

        detailsPanel.add(new JLabel("Return Quantity: *"));
        detailsPanel.add(txtReturnQty);

        detailsPanel.add(new JLabel("Return Date (YYYY-MM-DD): *"));
        detailsPanel.add(txtReturnDate);

        actionCard.add(detailsPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionBtnPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        actionBtnPanel.setOpaque(false);

        btnReturn = UITheme.createSuccessButton("✓ Settle & Return Equipment");
        btnReturn.setEnabled(false);
        btnReturn.addActionListener(e -> processReturnAction());

        JButton btnViewHist = UITheme.createSecondaryButton("View Full History →");
        btnViewHist.addActionListener(e -> mainFrame.showCard(MainDashboardFrame.CARD_HISTORY));

        actionBtnPanel.add(btnReturn);
        actionBtnPanel.add(btnViewHist);
        actionCard.add(actionBtnPanel, BorderLayout.SOUTH);

        centerGrid.add(actionCard, BorderLayout.EAST);
        add(centerGrid, BorderLayout.CENTER);
    }

    private void handleRowSelection() {
        int row = activeIssuesTable.getSelectedRow();
        if (row >= 0) {
            String recordId = (String) activeIssuesModel.getValueAt(row, 0);
            currentSelectedRecord = issueReturnManager.getRecordById(recordId);
            if (currentSelectedRecord != null) {
                lblSelectedRecord.setText(currentSelectedRecord.getRecordId());
                lblEquipmentName.setText(currentSelectedRecord.getEquipmentName());
                lblIssuedTo.setText(currentSelectedRecord.getIssuedTo());
                lblPendingQuantity.setText(String.valueOf(currentSelectedRecord.getRemainingQuantity()));
                lblDueDate.setText(currentSelectedRecord.getExpectedReturnDate());
                txtReturnQty.setText(String.valueOf(currentSelectedRecord.getRemainingQuantity()));
                txtReturnDate.setText(DateUtil.getTodayString());
                btnReturn.setEnabled(true);
                return;
            }
        }
        clearSelectionDetails();
    }

    private void clearSelectionDetails() {
        currentSelectedRecord = null;
        lblSelectedRecord.setText("None selected");
        lblEquipmentName.setText("-");
        lblIssuedTo.setText("-");
        lblPendingQuantity.setText("-");
        lblDueDate.setText("-");
        txtReturnQty.setText("");
        btnReturn.setEnabled(false);
    }

    private void processReturnAction() {
        if (currentSelectedRecord == null) {
            JOptionPane.showMessageDialog(this, "Please select an active checkout record first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String qtyStr = txtReturnQty.getText().trim();
            if (qtyStr.isEmpty()) {
                throw new ValidationException("Please enter the return quantity.");
            }
            int returnQty;
            try {
                returnQty = Integer.parseInt(qtyStr);
            } catch (NumberFormatException e) {
                throw new ValidationException("Return quantity must be a valid numeric integer.");
            }

            String returnDate = txtReturnDate.getText().trim();

            issueReturnManager.returnEquipment(
                    currentSelectedRecord.getRecordId(),
                    returnQty,
                    returnDate,
                    inventoryManager
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Equipment check-in successful!\n" +
                            returnQty + " unit(s) of '" + currentSelectedRecord.getEquipmentName() +
                            "' have been returned into active inventory.",
                    "Return Processed",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearSelectionDetails();
            refreshPanel();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Return Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshPanel() {
        activeIssuesModel.setRowCount(0);
        List<IssueRecord> activeList = issueReturnManager.getActiveIssuedRecords();
        for (IssueRecord r : activeList) {
            activeIssuesModel.addRow(new Object[]{
                    r.getRecordId(),
                    r.getEquipmentId(),
                    r.getEquipmentName(),
                    r.getIssuedTo(),
                    r.getUserRole(),
                    r.getQuantity(),
                    r.getRemainingQuantity(),
                    r.getExpectedReturnDate(),
                    r.getStatus()
            });
        }
        clearSelectionDetails();
    }
}
