package main;

import manager.FileManager;
import manager.InventoryManager;
import manager.IssueReturnManager;
import manager.LoginManager;
import manager.MaintenanceManager;
import ui.LoginFrame;

import javax.swing.*;

/**
 * Main application entry point for the Smart Laboratory Inventory Management system.
 * 
 * Academic Project: Smart Inventory Management for Laboratories
 * Technologies: Java SE, Java Swing/AWT, File I/O, OOP, Collections, Event Handling.
 */
public class Main {
    public static void main(String[] args) {
        // Output project banner and execution context to terminal
        printBanner();

        // Apply clean modern system Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Notice: Using standard Swing Look and Feel (" + e.getMessage() + ")");
        }

        // Initialize Core Persistence and Business Logic Managers
        FileManager fileManager = new FileManager();
        LoginManager loginManager = new LoginManager();
        InventoryManager inventoryManager = new InventoryManager(fileManager);
        IssueReturnManager issueReturnManager = new IssueReturnManager(fileManager);
        MaintenanceManager maintenanceManager = new MaintenanceManager(fileManager);

        // Launch Login GUI on Swing Event Dispatch Thread (EDT)
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

    private static void printBanner() {
        System.out.println("==================================================================");
        System.out.println("   SMART INVENTORY MANAGEMENT FOR LABORATORIES - JAVA MINI PROJECT");
        System.out.println("==================================================================");
        System.out.println(" Architecture : Java Desktop GUI (Swing / AWT)");
        System.out.println(" Persistence  : File-Based CSV Storage ('data/' directory)");
        System.out.println(" Demo Users   :");
        System.out.println("   1. Lab Assistant -> Username: 'assistant' | Password: 'assistant123'");
        System.out.println("   2. Faculty       -> Username: 'faculty'   | Password: 'faculty123'");
        System.out.println(" Java Runtime : " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        System.out.println(" Note on Applets: Applets (java.applet.Applet) were deprecated in Java 9");
        System.out.println(" and removed in JDK 17+. This project uses standard Java Swing/AWT");
        System.out.println(" desktop architecture, compatible with all modern and legacy JDKs.");
        System.out.println("==================================================================");
    }
}
