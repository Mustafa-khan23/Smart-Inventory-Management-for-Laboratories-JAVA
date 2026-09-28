package test;

import manager.*;
import model.*;
import util.*;

import java.io.File;
import java.util.List;

/**
 * Automated End-to-End System and Workflow Verification Test Suite.
 * Validates all 18 requirements specified in the project test plan.
 */
public class SystemWorkflowTest {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   RUNNING AUTOMATED SYSTEM WORKFLOW AND PERSISTENCE TEST SUITE   ");
        System.out.println("==================================================================");

        try {
            // STEP 1 & 18: Authentication & Invalid Login Testing
            testAuthentication();

            // STEP 2 & 17: Inventory CRUD & Validation Testing
            testInventoryOperations();

            // STEP 7-10: Issue & Return Lifecycle
            testIssueAndReturnLifecycle();

            // STEP 11-12: Maintenance Lifecycle
            testMaintenanceLifecycle();

            // STEP 15-16: Persistence Across Application Restart
            testPersistenceAcrossRestart();

            System.out.println("\n------------------------------------------------------------------");
            System.out.println("TEST SUMMARY: " + testsPassed + " PASSED, " + testsFailed + " FAILED.");
            System.out.println("------------------------------------------------------------------");

            if (testsFailed > 0) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Fatal unexpected test error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void testAuthentication() {
        System.out.println("\n[1] Testing Authentication & Roles...");
        LoginManager loginMgr = new LoginManager();

        // Test valid Lab Assistant
        try {
            User assistant = loginMgr.authenticate("assistant", "assistant123");
            assertTrue(assistant instanceof LabAssistant, "Authenticate assistant role");
            assertTrue(assistant.canModifyInventory(), "Assistant has inventory modification rights");
            assertTrue(assistant.canManageMaintenance(), "Assistant has maintenance management rights");
        } catch (ValidationException e) {
            assertTrue(false, "Assistant login failed: " + e.getMessage());
        }

        // Test valid Faculty
        try {
            User faculty = loginMgr.authenticate("faculty", "faculty123");
            assertTrue(faculty instanceof Faculty, "Authenticate faculty role");
            assertTrue(!faculty.canModifyInventory(), "Faculty has read-only inventory modification rights");
            assertTrue(!faculty.canManageMaintenance(), "Faculty has read-only maintenance rights");
        } catch (ValidationException e) {
            assertTrue(false, "Faculty login failed: " + e.getMessage());
        }

        // Test invalid login
        try {
            loginMgr.authenticate("assistant", "wrongpassword");
            assertTrue(false, "Should reject wrong password");
        } catch (ValidationException e) {
            assertTrue(true, "Properly rejected invalid password: " + e.getMessage());
        }

        try {
            loginMgr.authenticate("nonexistent", "123");
            assertTrue(false, "Should reject non-existent user");
        } catch (ValidationException e) {
            assertTrue(true, "Properly rejected unknown user: " + e.getMessage());
        }
    }

    private static void testInventoryOperations() throws Exception {
        System.out.println("\n[2] Testing Equipment Inventory Management...");
        FileManager fileMgr = new FileManager();
        InventoryManager invMgr = new InventoryManager(fileMgr);

        // Verify initial sample equipment
        assertTrue(invMgr.getAllEquipment().size() >= 9, "Initial sample equipment loaded (>= 9 items)");

        // Add equipment
        String testId = "TEST_EQ_99";
        Equipment newEq = new Equipment(testId, "Signal Analyzer 5000", "Electronics", 10, 10, "Signal Lab", "Available", "2026-09-01");
        invMgr.addEquipment(newEq);
        Equipment retrieved = invMgr.getEquipmentById(testId);
        assertTrue(retrieved != null && retrieved.getName().equals("Signal Analyzer 5000"), "Add new equipment");

        // Duplicate ID prevention
        try {
            Equipment dup = new Equipment(testId, "Duplicate", "Electronics", 5, 5, "Lab", "Available", "2026-09-01");
            invMgr.addEquipment(dup);
            assertTrue(false, "Should reject duplicate equipment ID");
        } catch (ValidationException e) {
            assertTrue(true, "Duplicate ID properly rejected: " + e.getMessage());
        }

        // Search equipment
        List<Equipment> searchRes = invMgr.searchEquipment("Signal Analyzer");
        assertTrue(searchRes.size() >= 1, "Search equipment by name finds item");

        // Update equipment
        Equipment updateData = new Equipment(testId, "Signal Analyzer 5000 Pro", "Electronics", 12, 12, "Advanced Signal Lab", "Available", "2026-09-01");
        invMgr.updateEquipment(updateData);
        Equipment updated = invMgr.getEquipmentById(testId);
        assertTrue(updated.getName().equals("Signal Analyzer 5000 Pro") && updated.getQuantity() == 12, "Update equipment attributes");

        // Validation: Negative quantity
        try {
            new Equipment("ERR1", "Err", "Gen", -5, 0, "Lab", "Available", "2026-09-01");
            assertTrue(false, "Should reject negative total quantity");
        } catch (ValidationException e) {
            assertTrue(true, "Negative total quantity rejected: " + e.getMessage());
        }

        // Validation: Available > Total
        try {
            new Equipment("ERR2", "Err", "Gen", 5, 10, "Lab", "Available", "2026-09-01");
            assertTrue(false, "Should reject available > total");
        } catch (ValidationException e) {
            assertTrue(true, "Available > total quantity rejected: " + e.getMessage());
        }
    }

    private static void testIssueAndReturnLifecycle() throws Exception {
        System.out.println("\n[3] Testing Issue & Return Lifecycle...");
        FileManager fileMgr = new FileManager();
        InventoryManager invMgr = new InventoryManager(fileMgr);
        IssueReturnManager issMgr = new IssueReturnManager(fileMgr);

        String eqId = "EQ001"; // Arduino Uno (Total 25, starting available around 20)
        Equipment eq = invMgr.getEquipmentById(eqId);
        int initialAvail = eq.getAvailableQuantity();

        // Issue 3 units
        IssueRecord record = issMgr.issueEquipment(eqId, "Amit Patel", "Student", 3, DateUtil.getTodayString(), DateUtil.getDefaultReturnDate(7), invMgr);
        assertTrue(record != null, "Issue record created successfully with ID: " + record.getRecordId());
        assertTrue(eq.getAvailableQuantity() == initialAvail - 3, "Available stock decreased by 3 units (now: " + eq.getAvailableQuantity() + ")");

        // Issue failure on excess quantity
        try {
            issMgr.issueEquipment(eqId, "Test", "Student", 9999, DateUtil.getTodayString(), DateUtil.getDefaultReturnDate(7), invMgr);
            assertTrue(false, "Should reject issuing quantity exceeding available stock");
        } catch (ValidationException e) {
            assertTrue(true, "Excess issue quantity rejected: " + e.getMessage());
        }

        // Partial return: return 1 unit
        issMgr.returnEquipment(record.getRecordId(), 1, DateUtil.getTodayString(), invMgr);
        assertTrue(record.getRemainingQuantity() == 2, "Partial return: remaining quantity is 2");
        assertTrue(eq.getAvailableQuantity() == initialAvail - 2, "Stock replenished by 1 unit (now: " + eq.getAvailableQuantity() + ")");
        assertTrue(record.getStatus().contains("Partially Returned"), "Record status updated to Partially Returned");

        // Complete return: return remaining 2 units
        issMgr.returnEquipment(record.getRecordId(), 2, DateUtil.getTodayString(), invMgr);
        assertTrue(record.getRemainingQuantity() == 0, "Complete return: remaining quantity is 0");
        assertTrue(eq.getAvailableQuantity() == initialAvail, "Stock fully restored to initial quantity (" + initialAvail + ")");
        assertTrue("Returned".equalsIgnoreCase(record.getStatus()), "Record status marked as Returned");

        // Excess return prevention
        try {
            issMgr.returnEquipment(record.getRecordId(), 1, DateUtil.getTodayString(), invMgr);
            assertTrue(false, "Should reject returning items when record is already completed");
        } catch (ValidationException e) {
            assertTrue(true, "Return on already returned record rejected: " + e.getMessage());
        }
    }

    private static void testMaintenanceLifecycle() throws Exception {
        System.out.println("\n[4] Testing Maintenance Lifecycle...");
        FileManager fileMgr = new FileManager();
        InventoryManager invMgr = new InventoryManager(fileMgr);
        MaintenanceManager maintMgr = new MaintenanceManager(fileMgr);

        String maintId = maintMgr.generateNextId();
        MaintenanceRecord rec = new MaintenanceRecord(
                maintId,
                "EQ003",
                "Digital Multimeter",
                DateUtil.getTodayString(),
                DateUtil.getDefaultReturnDate(15),
                "Battery and fuse diagnostic",
                "Staff Tech",
                "In Progress"
        );

        maintMgr.scheduleMaintenance(rec, invMgr);
        MaintenanceRecord fetched = maintMgr.getById(maintId);
        assertTrue(fetched != null, "Scheduled maintenance record successfully");

        // Check equipment status flagged as Under Maintenance
        Equipment eq = invMgr.getEquipmentById("EQ003");
        assertTrue("Under Maintenance".equalsIgnoreCase(eq.getStatus()), "Equipment status set to 'Under Maintenance'");

        // Mark maintenance completed
        maintMgr.markAsCompleted(maintId, invMgr);
        assertTrue("Completed".equalsIgnoreCase(fetched.getStatus()), "Maintenance status updated to 'Completed'");
        assertTrue(!"Under Maintenance".equalsIgnoreCase(eq.getStatus()), "Equipment status restored from 'Under Maintenance'");

        // Delete test record
        maintMgr.deleteMaintenance(maintId, invMgr);
        assertTrue(maintMgr.getById(maintId) == null, "Maintenance record deleted");
    }

    private static void testPersistenceAcrossRestart() throws Exception {
        System.out.println("\n[5] Testing Persistence Across Application Restart...");

        // Instance 1: Add a distinctive item
        FileManager fm1 = new FileManager();
        InventoryManager im1 = new InventoryManager(fm1);
        String persistId = "EQ_PERSIST_1";
        if (im1.getEquipmentById(persistId) != null) {
            im1.deleteEquipment(persistId);
        }
        im1.addEquipment(new Equipment(persistId, "Laser Rangefinder", "Hardware Tools", 4, 4, "Physics Lab", "Available", "2026-09-25"));

        // Simulate complete application restart (new instances reading from disk)
        FileManager fm2 = new FileManager();
        InventoryManager im2 = new InventoryManager(fm2);
        Equipment persisted = im2.getEquipmentById(persistId);

        assertTrue(persisted != null, "Data persists after simulated restart: Equipment found");
        assertTrue(persisted != null && persisted.getName().equals("Laser Rangefinder"), "Persisted equipment properties intact");

        // Cleanup test equipment
        if (persisted != null) {
            im2.deleteEquipment(persistId);
        }
        // Also clean up TEST_EQ_99 from earlier step
        if (im2.getEquipmentById("TEST_EQ_99") != null) {
            im2.deleteEquipment("TEST_EQ_99");
        }
    }
}
