package ui;

import manager.InventoryManager;
import manager.IssueReturnManager;
import manager.LoginManager;
import model.Equipment;
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
 * Issue Equipment Panel providing checkout forms, quantity validation,
 * expected return date calculation, and active stock preview.
 */
public class IssuePanel extends JPanel {
    private final InventoryManager inventoryManager;
    private final IssueReturnManager issueReturnManager;
    private final LoginManager loginManager;
    private final MainDashboardFrame mainFrame;

    private JComboBox<EquipmentItemWrapper> cmbEquipment;
    private JLabel lblStockIndicator;
    private JTextField txtIssuedTo;
    private JComboBox<String> cmbRole;
    private JTextField txtQuantity;
    private JTextField txtIssueDate;
    private JTextField txtExpectedDate;

    private JTable recentIssuesTable;
    private DefaultTableModel recentModel;

    public IssuePanel(InventoryManager invMgr, IssueReturnManager issMgr, LoginManager loginMgr, MainDashboardFrame mainFrame) {
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
        JLabel title = new JLabel("Issue Laboratory Equipment");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel subtitle = new JLabel("Check out apparatus to students, research scholars, or faculty members");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        topPanel.add(title);
        topPanel.add(subtitle);
        add(topPanel, BorderLayout.NORTH);

        // Center Split: Left = Issue Form Card, Right = Recent Checkouts Table Card
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 16));
        centerGrid.setOpaque(false);

        // LEFT CARD: FORM
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(12, 12));

        JLabel lblFormTitle = new JLabel("New Checkout Requisition");
        lblFormTitle.setFont(UITheme.FONT_HEADER);
        lblFormTitle.setForeground(UITheme.TEXT_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(7, 2, 8, 12));
        fieldsPanel.setOpaque(false);

        cmbEquipment = new JComboBox<>();
        cmbEquipment.setFont(UITheme.FONT_REGULAR);
        cmbEquipment.addActionListener(e -> updateStockIndicator());

        lblStockIndicator = new JLabel("Select an equipment");
        lblStockIndicator.setFont(UITheme.FONT_BOLD);
        lblStockIndicator.setForeground(UITheme.ACCENT);

        txtIssuedTo = UITheme.createTextField(15);
        cmbRole = new JComboBox<>(new String[]{"Student", "Faculty", "Research Scholar", "Lab Assistant"});
        cmbRole.setFont(UITheme.FONT_REGULAR);

        txtQuantity = UITheme.createTextField(15);
        txtQuantity.setText("1");

        txtIssueDate = UITheme.createTextField(15);
        txtIssueDate.setText(DateUtil.getTodayString());

        txtExpectedDate = UITheme.createTextField(15);
        txtExpectedDate.setText(DateUtil.getDefaultReturnDate(14)); // default 2 weeks

        fieldsPanel.add(new JLabel("Select Equipment: *"));
        fieldsPanel.add(cmbEquipment);

        fieldsPanel.add(new JLabel("Current Availability:"));
        fieldsPanel.add(lblStockIndicator);

        fieldsPanel.add(new JLabel("Issued To (Name): *"));
        fieldsPanel.add(txtIssuedTo);

        fieldsPanel.add(new JLabel("Borrower Role:"));
        fieldsPanel.add(cmbRole);

        fieldsPanel.add(new JLabel("Quantity to Issue: *"));
        fieldsPanel.add(txtQuantity);

        fieldsPanel.add(new JLabel("Issue Date (YYYY-MM-DD): *"));
        fieldsPanel.add(txtIssueDate);

        fieldsPanel.add(new JLabel("Expected Return Date: *"));
        fieldsPanel.add(txtExpectedDate);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Form Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setOpaque(false);

        JButton btnClear = UITheme.createSecondaryButton("Clear Form");
        btnClear.addActionListener(e -> resetForm());

        JButton btnIssue = UITheme.createPrimaryButton("Confirm & Issue Equipment");
        btnIssue.addActionListener(e -> handleIssue());

        btnPanel.add(btnClear);
        btnPanel.add(btnIssue);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        // RIGHT CARD: RECENT CHECKOUTS
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        JLabel lblTableTitle = new JLabel("Recent Checkout Records");
        lblTableTitle.setFont(UITheme.FONT_HEADER);
        lblTableTitle.setForeground(UITheme.TEXT_DARK);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Record ID", "Equipment", "Issued To", "Role", "Qty", "Due Date", "Status"};
        recentModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentIssuesTable = new JTable(recentModel);
        UITheme.styleTable(recentIssuesTable);

        JScrollPane scroll = new JScrollPane(recentIssuesTable);
        scroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scroll, BorderLayout.CENTER);

        centerGrid.add(formCard);
        centerGrid.add(tableCard);

        add(centerGrid, BorderLayout.CENTER);
    }

    private void updateStockIndicator() {
        EquipmentItemWrapper selected = (EquipmentItemWrapper) cmbEquipment.getSelectedItem();
        if (selected != null && selected.equipment != null) {
            int avail = selected.equipment.getAvailableQuantity();
            int total = selected.equipment.getQuantity();
            lblStockIndicator.setText(avail + " units available (out of " + total + ")");
            if (avail == 0) {
                lblStockIndicator.setForeground(UITheme.DANGER);
            } else if (avail < 3) {
                lblStockIndicator.setForeground(UITheme.WARNING);
            } else {
                lblStockIndicator.setForeground(UITheme.SUCCESS);
            }
        } else {
            lblStockIndicator.setText("No equipment selected");
            lblStockIndicator.setForeground(UITheme.TEXT_MUTED);
        }
    }

    private void handleIssue() {
        try {
            EquipmentItemWrapper item = (EquipmentItemWrapper) cmbEquipment.getSelectedItem();
            if (item == null || item.equipment == null) {
                throw new ValidationException("Please select an equipment item.");
            }

            String issuedTo = txtIssuedTo.getText().trim();
            String role = (String) cmbRole.getSelectedItem();
            String qtyStr = txtQuantity.getText().trim();
            String issDate = txtIssueDate.getText().trim();
            String expDate = txtExpectedDate.getText().trim();

            if (qtyStr.isEmpty()) {
                throw new ValidationException("Please enter the quantity to issue.");
            }
            int qty;
            try {
                qty = Integer.parseInt(qtyStr);
            } catch (NumberFormatException e) {
                throw new ValidationException("Quantity must be a positive integer.");
            }

            // Perform issue via Manager
            IssueRecord record = issueReturnManager.issueEquipment(
                    item.equipment.getId(),
                    issuedTo,
                    role,
                    qty,
                    issDate,
                    expDate,
                    inventoryManager
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Equipment successfully issued!\n\nRecord ID: " + record.getRecordId() +
                            "\nEquipment: " + record.getEquipmentName() +
                            "\nIssued To: " + record.getIssuedTo() +
                            "\nQuantity: " + record.getQuantity() +
                            "\nExpected Return: " + record.getExpectedReturnDate(),
                    "Issue Confirmed",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetForm();
            refreshPanel();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Issue Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        txtIssuedTo.setText("");
        txtQuantity.setText("1");
        txtIssueDate.setText(DateUtil.getTodayString());
        txtExpectedDate.setText(DateUtil.getDefaultReturnDate(14));
        if (cmbEquipment.getItemCount() > 0) {
            cmbEquipment.setSelectedIndex(0);
        }
        updateStockIndicator();
    }

    public void refreshPanel() {
        // Refresh equipment dropdown
        EquipmentItemWrapper selected = (EquipmentItemWrapper) cmbEquipment.getSelectedItem();
        String prevSelectedId = (selected != null && selected.equipment != null) ? selected.equipment.getId() : null;

        cmbEquipment.removeAllItems();
        List<Equipment> equipmentList = inventoryManager.getAllEquipment();
        int restoreIdx = 0;
        int idx = 0;
        for (Equipment eq : equipmentList) {
            cmbEquipment.addItem(new EquipmentItemWrapper(eq));
            if (prevSelectedId != null && eq.getId().equals(prevSelectedId)) {
                restoreIdx = idx;
            }
            idx++;
        }
        if (cmbEquipment.getItemCount() > 0) {
            cmbEquipment.setSelectedIndex(restoreIdx);
        }
        updateStockIndicator();

        // Refresh recent issues table
        recentModel.setRowCount(0);
        List<IssueRecord> records = issueReturnManager.getAllRecords();
        int limit = Math.min(10, records.size());
        for (int i = 0; i < limit; i++) {
            IssueRecord r = records.get(i);
            recentModel.addRow(new Object[]{
                    r.getRecordId(),
                    r.getEquipmentName(),
                    r.getIssuedTo(),
                    r.getUserRole(),
                    r.getQuantity(),
                    r.getExpectedReturnDate(),
                    r.getStatus()
            });
        }
    }

    /**
     * Wrapper for Equipment in JComboBox to present clean label.
     */
    private static class EquipmentItemWrapper {
        private final Equipment equipment;

        public EquipmentItemWrapper(Equipment equipment) {
            this.equipment = equipment;
        }

        @Override
        public String toString() {
            if (equipment == null) return "Select Equipment...";
            return equipment.getId() + " - " + equipment.getName() + " (" + equipment.getAvailableQuantity() + " avail)";
        }
    }
}
