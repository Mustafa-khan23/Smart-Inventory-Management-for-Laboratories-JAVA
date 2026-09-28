package ui;

import manager.InventoryManager;
import manager.LoginManager;
import manager.MaintenanceManager;
import model.Equipment;
import model.MaintenanceRecord;
import util.DateUtil;
import util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Maintenance Management Panel allowing lab assistants to schedule calibration,
 * track repairs, mark services completed, and inspect service histories.
 */
public class MaintenancePanel extends JPanel {
    private final MaintenanceManager maintenanceManager;
    private final InventoryManager inventoryManager;
    private final LoginManager loginManager;

    private JTable maintenanceTable;
    private DefaultTableModel tableModel;

    private JButton btnSchedule;
    private JButton btnEdit;
    private JButton btnMarkCompleted;
    private JButton btnDelete;

    public MaintenancePanel(MaintenanceManager maintMgr, InventoryManager invMgr, LoginManager loginMgr) {
        this.maintenanceManager = maintMgr;
        this.inventoryManager = invMgr;
        this.loginManager = loginMgr;

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
        JLabel title = new JLabel("Equipment Maintenance & Calibration");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel subtitle = new JLabel("Plan servicing schedules, track calibration cycles, and oversee lab hardware safety");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        topContainer.add(titlePanel, BorderLayout.NORTH);

        // Actions Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        toolbar.setOpaque(false);

        btnSchedule = UITheme.createPrimaryButton("+ Schedule Maintenance");
        btnSchedule.addActionListener(e -> showScheduleDialog());

        btnEdit = UITheme.createSecondaryButton("✏️ Update Details");
        btnEdit.addActionListener(e -> showEditDialog());

        btnMarkCompleted = UITheme.createSuccessButton("✓ Mark as Completed");
        btnMarkCompleted.addActionListener(e -> handleMarkCompleted());

        btnDelete = UITheme.createDangerButton("🗑️ Delete");
        btnDelete.addActionListener(e -> handleDelete());

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshTable());

        toolbar.add(btnSchedule);
        toolbar.add(btnEdit);
        toolbar.add(btnMarkCompleted);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        topContainer.add(toolbar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Center Table
        String[] columns = {
                "Maintenance ID", "Equipment ID", "Equipment Name", "Service Date",
                "Next Due Date", "Technician / Agency", "Status", "Description"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        maintenanceTable = new JTable(tableModel);
        UITheme.styleTable(maintenanceTable);
        maintenanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(maintenanceTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        add(scrollPane, BorderLayout.CENTER);

        applyPermissions();
    }

    public void applyPermissions() {
        boolean canManage = (loginManager.getCurrentUser() != null && loginManager.getCurrentUser().canManageMaintenance());
        btnSchedule.setEnabled(canManage);
        btnEdit.setEnabled(canManage);
        btnMarkCompleted.setEnabled(canManage);
        btnDelete.setEnabled(canManage);
        if (!canManage) {
            String tooltip = "Maintenance operations are reserved for Lab Assistants.";
            btnSchedule.setToolTipText(tooltip);
            btnEdit.setToolTipText(tooltip);
            btnMarkCompleted.setToolTipText(tooltip);
            btnDelete.setToolTipText(tooltip);
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<MaintenanceRecord> list = maintenanceManager.getAllMaintenance();
        for (MaintenanceRecord m : list) {
            tableModel.addRow(new Object[]{
                    m.getMaintenanceId(),
                    m.getEquipmentId(),
                    m.getEquipmentName(),
                    m.getMaintenanceDate(),
                    m.getNextMaintenanceDate(),
                    m.getTechnician(),
                    m.getStatus(),
                    m.getDescription()
            });
        }
    }

    private void showScheduleDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Schedule New Maintenance", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(460, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 12));
        formPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JTextField txtId = UITheme.createTextField(15);
        txtId.setText(maintenanceManager.generateNextId());

        JComboBox<EquipmentItemWrapper> cmbEq = new JComboBox<>();
        for (Equipment eq : inventoryManager.getAllEquipment()) {
            cmbEq.addItem(new EquipmentItemWrapper(eq));
        }

        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(DateUtil.getTodayString());

        JTextField txtNextDate = UITheme.createTextField(15);
        txtNextDate.setText(DateUtil.getDefaultReturnDate(30));

        JTextField txtDesc = UITheme.createTextField(15);
        txtDesc.setText("Routine inspection and calibration");

        JTextField txtTech = UITheme.createTextField(15);
        txtTech.setText("Lab Technician");

        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Scheduled", "In Progress", "Completed"});

        formPanel.add(new JLabel("Maintenance ID: *"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Select Equipment: *"));
        formPanel.add(cmbEq);
        formPanel.add(new JLabel("Maintenance Date: *"));
        formPanel.add(txtDate);
        formPanel.add(new JLabel("Next Maintenance Date:"));
        formPanel.add(txtNextDate);
        formPanel.add(new JLabel("Description: *"));
        formPanel.add(txtDesc);
        formPanel.add(new JLabel("Technician / Assigned:"));
        formPanel.add(txtTech);
        formPanel.add(new JLabel("Initial Status:"));
        formPanel.add(cmbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createPrimaryButton("Schedule");
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                String mId = txtId.getText().trim();
                EquipmentItemWrapper item = (EquipmentItemWrapper) cmbEq.getSelectedItem();
                if (item == null || item.equipment == null) {
                    throw new ValidationException("Please select an equipment.");
                }
                String mDate = txtDate.getText().trim();
                String nDate = txtNextDate.getText().trim();
                String desc = txtDesc.getText().trim();
                String tech = txtTech.getText().trim();
                String stat = (String) cmbStatus.getSelectedItem();

                if (desc.isEmpty()) {
                    throw new ValidationException("Please provide a description of maintenance.");
                }

                MaintenanceRecord record = new MaintenanceRecord(
                        mId,
                        item.equipment.getId(),
                        item.equipment.getName(),
                        mDate,
                        nDate,
                        desc,
                        tech,
                        stat
                );

                maintenanceManager.scheduleMaintenance(record, inventoryManager);
                JOptionPane.showMessageDialog(dialog, "Maintenance scheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();

            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int row = maintenanceTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a maintenance record to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String mId = (String) tableModel.getValueAt(row, 0);
        MaintenanceRecord existing = maintenanceManager.getById(mId);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Maintenance - " + existing.getMaintenanceId(), true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(460, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 12));
        formPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JTextField txtId = UITheme.createTextField(15);
        txtId.setText(existing.getMaintenanceId());
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));

        JLabel lblEq = new JLabel(existing.getEquipmentId() + " - " + existing.getEquipmentName());
        lblEq.setFont(UITheme.FONT_BOLD);

        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(existing.getMaintenanceDate());

        JTextField txtNextDate = UITheme.createTextField(15);
        txtNextDate.setText(existing.getNextMaintenanceDate());

        JTextField txtDesc = UITheme.createTextField(15);
        txtDesc.setText(existing.getDescription());

        JTextField txtTech = UITheme.createTextField(15);
        txtTech.setText(existing.getTechnician());

        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Scheduled", "In Progress", "Completed"});
        cmbStatus.setSelectedItem(existing.getStatus());

        formPanel.add(new JLabel("Maintenance ID:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Equipment:"));
        formPanel.add(lblEq);
        formPanel.add(new JLabel("Maintenance Date:"));
        formPanel.add(txtDate);
        formPanel.add(new JLabel("Next Maintenance Date:"));
        formPanel.add(txtNextDate);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(txtDesc);
        formPanel.add(new JLabel("Technician:"));
        formPanel.add(txtTech);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(cmbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createPrimaryButton("Save Changes");
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                String mDate = txtDate.getText().trim();
                String nDate = txtNextDate.getText().trim();
                String desc = txtDesc.getText().trim();
                String tech = txtTech.getText().trim();
                String stat = (String) cmbStatus.getSelectedItem();

                MaintenanceRecord updated = new MaintenanceRecord(
                        existing.getMaintenanceId(),
                        existing.getEquipmentId(),
                        existing.getEquipmentName(),
                        mDate,
                        nDate,
                        desc,
                        tech,
                        stat
                );

                maintenanceManager.updateMaintenance(updated, inventoryManager);
                JOptionPane.showMessageDialog(dialog, "Maintenance record updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();

            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleMarkCompleted() {
        int row = maintenanceTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a maintenance record to mark completed.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String mId = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Mark maintenance record '" + mId + "' as Completed and restore equipment status?",
                "Confirm Completion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                maintenanceManager.markAsCompleted(mId, inventoryManager);
                JOptionPane.showMessageDialog(this, "Maintenance record marked as Completed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete() {
        int row = maintenanceTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a maintenance record to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String mId = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete maintenance record '" + mId + "'?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                maintenanceManager.deleteMaintenance(mId, inventoryManager);
                JOptionPane.showMessageDialog(this, "Maintenance record deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class EquipmentItemWrapper {
        private final Equipment equipment;
        public EquipmentItemWrapper(Equipment equipment) { this.equipment = equipment; }
        @Override public String toString() { return equipment.getId() + " - " + equipment.getName(); }
    }
}
