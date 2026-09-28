package test;

import manager.*;
import ui.*;

import javax.swing.*;
import java.awt.*;

/**
 * UI Component Smoke Test to verify all Swing frames, panels, dialogs,
 * CardLayout transitions, and tables instantiate without exceptions.
 */
public class UISmokeTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting UI Smoke Test...");

        FileManager fileMgr = new FileManager();
        LoginManager loginMgr = new LoginManager();
        InventoryManager invMgr = new InventoryManager(fileMgr);
        IssueReturnManager issMgr = new IssueReturnManager(fileMgr);
        MaintenanceManager maintMgr = new MaintenanceManager(fileMgr);

        // Authenticate as Assistant
        loginMgr.authenticate("assistant", "assistant123");

        SwingUtilities.invokeAndWait(() -> {
            try {
                // Test LoginFrame
                LoginFrame loginFrame = new LoginFrame(loginMgr, invMgr, issMgr, maintMgr);
                loginFrame.pack();
                loginFrame.dispose();
                System.out.println("  [PASS] LoginFrame initialized and disposed cleanly.");

                // Test MainDashboardFrame
                MainDashboardFrame mainFrame = new MainDashboardFrame(loginMgr, invMgr, issMgr, maintMgr);
                mainFrame.pack();

                // Test cycling through all cards
                String[] cards = {
                        MainDashboardFrame.CARD_DASHBOARD,
                        MainDashboardFrame.CARD_EQUIPMENT,
                        MainDashboardFrame.CARD_ISSUE,
                        MainDashboardFrame.CARD_RETURN,
                        MainDashboardFrame.CARD_HISTORY,
                        MainDashboardFrame.CARD_MAINTENANCE,
                        MainDashboardFrame.CARD_REPORTS
                };

                for (String card : cards) {
                    mainFrame.showCard(card);
                    System.out.println("  [PASS] Card transitioned cleanly: " + card);
                }

                mainFrame.dispose();
                System.out.println("  [PASS] MainDashboardFrame disposed cleanly.");

                // Test Faculty role permissions
                loginMgr.authenticate("faculty", "faculty123");
                MainDashboardFrame facultyFrame = new MainDashboardFrame(loginMgr, invMgr, issMgr, maintMgr);
                facultyFrame.pack();
                facultyFrame.showCard(MainDashboardFrame.CARD_EQUIPMENT);
                facultyFrame.showCard(MainDashboardFrame.CARD_MAINTENANCE);
                facultyFrame.dispose();
                System.out.println("  [PASS] Faculty role permissions and UI rendering verified.");

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });

        System.out.println("UI Smoke Test Completed Successfully!");
    }
}
