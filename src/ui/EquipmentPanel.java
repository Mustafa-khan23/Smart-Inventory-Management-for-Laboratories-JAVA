package ui;

import manager.InventoryManager;
import manager.LoginManager;
import model.Equipment;
import util.DateUtil;
import util.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Equipment Management Panel providing full CRUD, search, filter,
 * and inventory catalog controls.
 */
public class EquipmentPanel extends JPanel {
    private final InventoryManager inventoryManager;
    private final LoginManager loginManager;

    private JTable equipmentTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbCategoryFilter;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    public EquipmentPanel(InventoryManager invMgr, LoginManager loginMgr) {
        this.inventoryManager = invMgr;
        this.loginManager = loginMgr;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        // Top Header and Controls
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Laboratory Equipment Inventory");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel subtitle = new JLabel("Manage apparatus specifications, locations, quantities, and operational statuses");
        subtitle.setFont(UITheme.FONT_SUBHEADER);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        topContainer.add(titlePanel, BorderLayout.NORTH);

        // Action Toolbar & Search Bar
        JPanel toolbar = new JPanel(new BorderLayout(10, 10));
        toolbar.setOpaque(false);

        // Search + Filter on Left
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        txtSearch = UITheme.createTextField(14);
        txtSearch.setToolTipText("Search by ID, Name, Category, or Location");

        // Live search listener
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applySearch(); }
            @Override public void removeUpdate(DocumentEvent e) { applySearch(); }
            @Override public void changedUpdate(DocumentEvent e) { applySearch(); }
        });

        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(UITheme.FONT_BOLD);
        cmbCategoryFilter = new JComboBox<>(new String[]{"All Categories", "Microcontroller", "Single Board Computer", "Electronics", "Prototyping", "Sensors", "Hardware Tools", "General"});
        cmbCategoryFilter.setFont(UITheme.FONT_REGULAR);
        cmbCategoryFilter.addActionListener(e -> applySearch());

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategoryFilter.setSelectedIndex(0);
            refreshTable();
        });

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(lblCategory);
        searchPanel.add(cmbCategoryFilter);
        searchPanel.add(btnReset);
        toolbar.add(searchPanel, BorderLayout.WEST);

        // CRUD Buttons on Right
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.setOpaque(false);

        btnAdd = UITheme.createPrimaryButton("+ Add Equipment");
        btnAdd.addActionListener(e -> showAddDialog());

        btnEdit = UITheme.createSecondaryButton("✏️ Edit Equipment");
        btnEdit.addActionListener(e -> showEditDialog());

        btnDelete = UITheme.createDangerButton("🗑️ Delete Equipment");
        btnDelete.addActionListener(e -> handleDelete());

        actionButtons.add(btnAdd);
        actionButtons.add(btnEdit);
        actionButtons.add(btnDelete);
        toolbar.add(actionButtons, BorderLayout.EAST);

        topContainer.add(toolbar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"Equipment ID", "Equipment Name", "Category", "Total Qty", "Available Qty", "Location", "Status", "Purchase Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        equipmentTable = new JTable(tableModel);
        UITheme.styleTable(equipmentTable);
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Double-click row to edit
        equipmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && equipmentTable.getSelectedRow() != -1) {
                    showEditDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(equipmentTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        add(scrollPane, BorderLayout.CENTER);

        applyPermissions();
    }

    public void applyPermissions() {
        boolean canModify = (loginManager.getCurrentUser() != null && loginManager.getCurrentUser().canModifyInventory());
        btnAdd.setEnabled(canModify);
        btnEdit.setEnabled(canModify);
        btnDelete.setEnabled(canModify);
        if (!canModify) {
            btnAdd.setToolTipText("Faculty can view equipment. Login as Lab Assistant to add/edit/delete.");
            btnEdit.setToolTipText("Faculty can view equipment. Login as Lab Assistant to add/edit/delete.");
            btnDelete.setToolTipText("Faculty can view equipment. Login as Lab Assistant to add/edit/delete.");
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Equipment> list = inventoryManager.getAllEquipment();
        for (Equipment eq : list) {
            tableModel.addRow(new Object[]{
                    eq.getId(),
                    eq.getName(),
                    eq.getCategory(),
                    eq.getQuantity(),
                    eq.getAvailableQuantity(),
                    eq.getLocation(),
                    eq.getStatus(),
                    eq.getPurchaseDate()
            });
        }
    }

    private void applySearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedCategory = (String) cmbCategoryFilter.getSelectedItem();
        boolean filterCategory = selectedCategory != null && !"All Categories".equalsIgnoreCase(selectedCategory);

        tableModel.setRowCount(0);
        List<Equipment> list = inventoryManager.searchEquipment(keyword);
        for (Equipment eq : list) {
            if (filterCategory && !eq.getCategory().equalsIgnoreCase(selectedCategory)) {
                continue;
            }
            tableModel.addRow(new Object[]{
                    eq.getId(),
                    eq.getName(),
                    eq.getCategory(),
                    eq.getQuantity(),
                    eq.getAvailableQuantity(),
                    eq.getLocation(),
                    eq.getStatus(),
                    eq.getPurchaseDate()
            });
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Equipment", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(440, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 8, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JTextField txtId = UITheme.createTextField(15);
        JTextField txtName = UITheme.createTextField(15);
        JComboBox<String> cmbCat = new JComboBox<>(new String[]{"Microcontroller", "Single Board Computer", "Electronics", "Prototyping", "Sensors", "Hardware Tools", "General"});
        JTextField txtTotal = UITheme.createTextField(15);
        JTextField txtLocation = UITheme.createTextField(15);
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Available", "Under Maintenance"});
        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(DateUtil.getTodayString());

        formPanel.add(new JLabel("Equipment ID: *"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Equipment Name: *"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(cmbCat);
        formPanel.add(new JLabel("Total Quantity: *"));
        formPanel.add(txtTotal);
        formPanel.add(new JLabel("Location / Lab Room:"));
        formPanel.add(txtLocation);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(cmbStatus);
        formPanel.add(new JLabel("Purchase Date (YYYY-MM-DD):"));
        formPanel.add(txtDate);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createPrimaryButton("Save Equipment");
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                String id = txtId.getText().trim();
                String name = txtName.getText().trim();
                String cat = (String) cmbCat.getSelectedItem();
                String totalStr = txtTotal.getText().trim();
                String loc = txtLocation.getText().trim();
                String stat = (String) cmbStatus.getSelectedItem();
                String date = txtDate.getText().trim();

                if (totalStr.isEmpty()) {
                    throw new ValidationException("Please enter a total quantity.");
                }
                int total;
                try {
                    total = Integer.parseInt(totalStr);
                } catch (NumberFormatException nfe) {
                    throw new ValidationException("Quantity must be a valid numeric integer.");
                }

                Equipment eq = new Equipment(id, name, cat, total, total, loc, stat, date);
                inventoryManager.addEquipment(eq);
                JOptionPane.showMessageDialog(dialog, "Equipment '" + name + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        int selectedRow = equipmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an equipment row to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String eqId = (String) tableModel.getValueAt(selectedRow, 0);
        Equipment existing = inventoryManager.getEquipmentById(eqId);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Equipment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Equipment - " + existing.getId(), true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(440, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 8, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JTextField txtId = UITheme.createTextField(15);
        txtId.setText(existing.getId());
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));

        JTextField txtName = UITheme.createTextField(15);
        txtName.setText(existing.getName());

        JComboBox<String> cmbCat = new JComboBox<>(new String[]{"Microcontroller", "Single Board Computer", "Electronics", "Prototyping", "Sensors", "Hardware Tools", "General"});
        cmbCat.setSelectedItem(existing.getCategory());

        JTextField txtTotal = UITheme.createTextField(15);
        txtTotal.setText(String.valueOf(existing.getQuantity()));

        JLabel lblAvail = new JLabel(String.valueOf(existing.getAvailableQuantity()));
        lblAvail.setFont(UITheme.FONT_BOLD);

        JTextField txtLocation = UITheme.createTextField(15);
        txtLocation.setText(existing.getLocation());

        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Available", "Under Maintenance", "Partially Available", "Issued (Out of Stock)"});
        cmbStatus.setSelectedItem(existing.getStatus());

        JTextField txtDate = UITheme.createTextField(15);
        txtDate.setText(existing.getPurchaseDate());

        formPanel.add(new JLabel("Equipment ID (Immutable):"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Equipment Name: *"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(cmbCat);
        formPanel.add(new JLabel("Total Quantity: *"));
        formPanel.add(txtTotal);
        formPanel.add(new JLabel("Current Available Qty:"));
        formPanel.add(lblAvail);
        formPanel.add(new JLabel("Location / Lab Room:"));
        formPanel.add(txtLocation);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(cmbStatus);
        formPanel.add(new JLabel("Purchase Date:"));
        formPanel.add(txtDate);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = UITheme.createPrimaryButton("Update Equipment");
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String cat = (String) cmbCat.getSelectedItem();
                String totalStr = txtTotal.getText().trim();
                String loc = txtLocation.getText().trim();
                String stat = (String) cmbStatus.getSelectedItem();
                String date = txtDate.getText().trim();

                int total = Integer.parseInt(totalStr);
                Equipment updated = new Equipment(existing.getId(), name, cat, total, existing.getAvailableQuantity(), loc, stat, date);
                inventoryManager.updateEquipment(updated);

                JOptionPane.showMessageDialog(dialog, "Equipment updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Total quantity must be a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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

    private void handleDelete() {
        int selectedRow = equipmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an equipment item to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String eqId = (String) tableModel.getValueAt(selectedRow, 0);
        String eqName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete equipment:\n" + eqId + " - " + eqName + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                inventoryManager.deleteEquipment(eqId);
                JOptionPane.showMessageDialog(this, "Equipment '" + eqName + "' deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
