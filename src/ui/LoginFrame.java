package ui;

import manager.InventoryManager;
import manager.IssueReturnManager;
import manager.LoginManager;
import manager.MaintenanceManager;
import model.User;
import util.ValidationException;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login Screen for Smart Laboratory Inventory Management.
 * Provides role credential authentication, demo helpers, and user-friendly validation.
 */
public class LoginFrame extends JFrame {
    private final LoginManager loginManager;
    private final InventoryManager inventoryManager;
    private final IssueReturnManager issueReturnManager;
    private final MaintenanceManager maintenanceManager;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblErrorMessage;

    public LoginFrame(LoginManager loginMgr, InventoryManager invMgr, IssueReturnManager issMgr, MaintenanceManager maintMgr) {
        this.loginManager = loginMgr;
        this.inventoryManager = invMgr;
        this.issueReturnManager = issMgr;
        this.maintenanceManager = maintMgr;

        setTitle("Smart Laboratory Inventory Management - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 540);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(UITheme.BG_MAIN);

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout(6, 6));
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(26, 20, 26, 20));

        JLabel lblTitle = new JLabel("SMART LABORATORY", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Inventory & Equipment Management System", SwingConstants.CENTER);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(new Color(203, 213, 225)); // Slate 300

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Login Card
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel card = UITheme.createCardPanel();
        card.setPreferredSize(new Dimension(380, 340));
        card.setLayout(new BorderLayout(12, 12));

        // Form Fields
        JPanel form = new JPanel(new GridLayout(6, 1, 4, 6));
        form.setOpaque(false);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(UITheme.FONT_BOLD);
        txtUsername = UITheme.createTextField(15);
        txtUsername.setText("assistant"); // Pre-filled with assistant for quick evaluation

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_BOLD);
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        txtPassword.setText("assistant123");

        lblErrorMessage = new JLabel(" ", SwingConstants.CENTER);
        lblErrorMessage.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblErrorMessage.setForeground(UITheme.DANGER);

        form.add(lblUser);
        form.add(txtUsername);
        form.add(lblPass);
        form.add(txtPassword);
        form.add(lblErrorMessage);

        // Enter key submits login
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        card.add(form, BorderLayout.CENTER);

        // Buttons Panel: Login + Demo shortcuts
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setOpaque(false);

        JButton btnLogin = UITheme.createPrimaryButton("Sign In to Dashboard →");
        btnLogin.setPreferredSize(new Dimension(0, 38));
        btnLogin.addActionListener(e -> handleLogin());
        bottomPanel.add(btnLogin, BorderLayout.NORTH);

        // Quick Demo Fill Buttons
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        demoPanel.setOpaque(false);
        JLabel lblQuick = new JLabel("Quick Demo:");
        lblQuick.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblQuick.setForeground(UITheme.TEXT_MUTED);

        JButton btnFillAssistant = UITheme.createSecondaryButton("Lab Assistant");
        btnFillAssistant.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnFillAssistant.addActionListener(e -> {
            txtUsername.setText("assistant");
            txtPassword.setText("assistant123");
            lblErrorMessage.setText(" ");
        });

        JButton btnFillFaculty = UITheme.createSecondaryButton("Faculty");
        btnFillFaculty.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnFillFaculty.addActionListener(e -> {
            txtUsername.setText("faculty");
            txtPassword.setText("faculty123");
            lblErrorMessage.setText(" ");
        });

        demoPanel.add(lblQuick);
        demoPanel.add(btnFillAssistant);
        demoPanel.add(btnFillFaculty);
        bottomPanel.add(demoPanel, BorderLayout.SOUTH);

        card.add(bottomPanel, BorderLayout.SOUTH);
        centerContainer.add(card);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        try {
            User user = loginManager.authenticate(username, password);
            lblErrorMessage.setText(" ");

            // Open Dashboard
            SwingUtilities.invokeLater(() -> {
                MainDashboardFrame dashboard = new MainDashboardFrame(
                        loginManager,
                        inventoryManager,
                        issueReturnManager,
                        maintenanceManager
                );
                dashboard.setVisible(true);
            });

            this.dispose();

        } catch (ValidationException ex) {
            lblErrorMessage.setText(ex.getMessage());
        }
    }
}
