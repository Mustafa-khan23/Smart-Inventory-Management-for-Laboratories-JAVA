package manager;

import model.Equipment;
import model.IssueRecord;
import util.DateUtil;
import util.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic manager for equipment issue/checkout and return tracking.
 */
public class IssueReturnManager {
    private final ArrayList<IssueRecord> issueRecords;
    private final FileManager fileManager;

    public IssueReturnManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.issueRecords = fileManager.loadIssues();
        refreshAllStatuses();
    }

    public synchronized void refreshAllStatuses() {
        for (IssueRecord rec : issueRecords) {
            rec.refreshStatus();
        }
    }

    public synchronized ArrayList<IssueRecord> getAllRecords() {
        refreshAllStatuses();
        return new ArrayList<>(issueRecords);
    }

    public synchronized ArrayList<IssueRecord> getActiveIssuedRecords() {
        refreshAllStatuses();
        ArrayList<IssueRecord> active = new ArrayList<>();
        for (IssueRecord rec : issueRecords) {
            if (!"Returned".equalsIgnoreCase(rec.getStatus())) {
                active.add(rec);
            }
        }
        return active;
    }

    public synchronized IssueRecord getRecordById(String recordId) {
        if (recordId == null) return null;
        for (IssueRecord rec : issueRecords) {
            if (rec.getRecordId().equalsIgnoreCase(recordId.trim())) {
                return rec;
            }
        }
        return null;
    }

    public synchronized IssueRecord issueEquipment(String equipmentId, String issuedTo, String userRole,
                                                  int quantity, String issueDate, String expectedReturnDate,
                                                  InventoryManager inventoryManager) throws ValidationException {
        if (equipmentId == null || equipmentId.trim().isEmpty()) {
            throw new ValidationException("Please select an equipment to issue.");
        }
        if (issuedTo == null || issuedTo.trim().isEmpty()) {
            throw new ValidationException("Please enter the name of the person equipment is issued to.");
        }
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be a positive integer greater than zero.");
        }
        if (!DateUtil.isValidDate(issueDate)) {
            throw new ValidationException("Invalid issue date: '" + issueDate + "'. Format: YYYY-MM-DD");
        }
        if (!DateUtil.isValidDate(expectedReturnDate)) {
            throw new ValidationException("Invalid expected return date: '" + expectedReturnDate + "'. Format: YYYY-MM-DD");
        }

        Equipment eq = inventoryManager.getEquipmentById(equipmentId);
        if (eq == null) {
            throw new ValidationException("Selected equipment '" + equipmentId + "' was not found in inventory.");
        }

        if (eq.getAvailableQuantity() < quantity) {
            throw new ValidationException("Insufficient stock! Available: " + eq.getAvailableQuantity() + ", Requested: " + quantity);
        }

        // Deduct available stock
        eq.issue(quantity);
        inventoryManager.saveChanges();

        // Generate unique Record ID
        String recordId = generateNextRecordId();
        IssueRecord newRecord = new IssueRecord(
                recordId,
                eq.getId(),
                eq.getName(),
                issuedTo.trim(),
                userRole.trim(),
                quantity,
                0,
                issueDate.trim(),
                expectedReturnDate.trim(),
                "-",
                "Issued"
        );

        issueRecords.add(0, newRecord); // Add at top for recent visibility
        fileManager.saveIssues(issueRecords);
        return newRecord;
    }

    public synchronized void returnEquipment(String recordId, int returnQty, String returnDate,
                                            InventoryManager inventoryManager) throws ValidationException {
        if (recordId == null || recordId.trim().isEmpty()) {
            throw new ValidationException("Please select an issue record to return.");
        }
        IssueRecord record = getRecordById(recordId);
        if (record == null) {
            throw new ValidationException("Issue record '" + recordId + "' not found.");
        }

        if ("Returned".equalsIgnoreCase(record.getStatus())) {
            throw new ValidationException("This record has already been completely returned.");
        }

        if (returnQty <= 0) {
            throw new ValidationException("Return quantity must be greater than zero.");
        }

        int remaining = record.getRemainingQuantity();
        if (returnQty > remaining) {
            throw new ValidationException("Cannot return " + returnQty + " units. Only " + remaining + " unit(s) were pending return.");
        }

        if (returnDate != null && !returnDate.trim().isEmpty() && !DateUtil.isValidDate(returnDate)) {
            throw new ValidationException("Invalid return date: '" + returnDate + "'. Format: YYYY-MM-DD");
        }
        String actualDate = (returnDate == null || returnDate.trim().isEmpty()) ? DateUtil.getTodayString() : returnDate.trim();

        // Update issue record
        record.processReturn(returnQty, actualDate);

        // Replenish equipment stock
        Equipment eq = inventoryManager.getEquipmentById(record.getEquipmentId());
        if (eq != null) {
            eq.returnItem(returnQty);
            inventoryManager.saveChanges();
        }

        fileManager.saveIssues(issueRecords);
    }

    public synchronized List<IssueRecord> filterRecords(String keyword, String statusFilter) {
        refreshAllStatuses();
        List<IssueRecord> list = new ArrayList<>();
        String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();
        String sf = (statusFilter == null) ? "All" : statusFilter.trim();

        for (IssueRecord rec : issueRecords) {
            boolean matchesStatus = "All".equalsIgnoreCase(sf) ||
                    rec.getStatus().toLowerCase().contains(sf.toLowerCase());

            boolean matchesKeyword = kw.isEmpty() ||
                    rec.getRecordId().toLowerCase().contains(kw) ||
                    rec.getEquipmentId().toLowerCase().contains(kw) ||
                    rec.getEquipmentName().toLowerCase().contains(kw) ||
                    rec.getIssuedTo().toLowerCase().contains(kw) ||
                    rec.getUserRole().toLowerCase().contains(kw);

            if (matchesStatus && matchesKeyword) {
                list.add(rec);
            }
        }
        return list;
    }

    private String generateNextRecordId() {
        int max = 1000;
        for (IssueRecord rec : issueRecords) {
            try {
                String numStr = rec.getRecordId().replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    int val = Integer.parseInt(numStr);
                    if (val > max) max = val;
                }
            } catch (Exception ignored) {
            }
        }
        return "REC" + (max + 1);
    }

    public synchronized int getTotalRecordsCount() {
        return issueRecords.size();
    }

    public synchronized int getCompletedReturnsCount() {
        int count = 0;
        for (IssueRecord rec : issueRecords) {
            if ("Returned".equalsIgnoreCase(rec.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public synchronized int getOverdueCount() {
        int count = 0;
        refreshAllStatuses();
        for (IssueRecord rec : issueRecords) {
            if (rec.getStatus().toLowerCase().contains("overdue")) {
                count++;
            }
        }
        return count;
    }
}
