package model;

import util.DateUtil;
import util.ValidationException;

/**
 * Model class representing an Equipment Checkout/Issue record.
 * Supports partial and full returns, overdue detection, and status management.
 */
public class IssueRecord {
    private String recordId;
    private String equipmentId;
    private String equipmentName;
    private String issuedTo;
    private String userRole;
    private int quantity;
    private int returnedQuantity;
    private String issueDate;
    private String expectedReturnDate;
    private String actualReturnDate;
    private String status;

    public IssueRecord() {
    }

    public IssueRecord(String recordId, String equipmentId, String equipmentName, String issuedTo,
                       String userRole, int quantity, int returnedQuantity, String issueDate,
                       String expectedReturnDate, String actualReturnDate, String status) throws ValidationException {
        this.recordId = recordId;
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.issuedTo = issuedTo;
        this.userRole = userRole;
        this.quantity = quantity;
        this.returnedQuantity = returnedQuantity;
        this.issueDate = issueDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = (actualReturnDate == null || actualReturnDate.trim().isEmpty()) ? "-" : actualReturnDate.trim();
        this.status = status;
        refreshStatus();
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getIssuedTo() {
        return issuedTo;
    }

    public void setIssuedTo(String issuedTo) {
        this.issuedTo = issuedTo;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReturnedQuantity() {
        return returnedQuantity;
    }

    public void setReturnedQuantity(int returnedQuantity) {
        this.returnedQuantity = returnedQuantity;
    }

    public int getRemainingQuantity() {
        return quantity - returnedQuantity;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(String expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public String getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(String actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public String getStatus() {
        refreshStatus();
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Processes return of equipment items.
     */
    public void processReturn(int returnQty, String returnDate) throws ValidationException {
        if (returnQty <= 0) {
            throw new ValidationException("Return quantity must be greater than zero.");
        }
        int remaining = getRemainingQuantity();
        if (returnQty > remaining) {
            throw new ValidationException("Cannot return " + returnQty + " items. Only " + remaining + " item(s) are currently pending return.");
        }

        this.returnedQuantity += returnQty;
        this.actualReturnDate = (returnDate == null || returnDate.trim().isEmpty()) ? DateUtil.getTodayString() : returnDate.trim();
        refreshStatus();
    }

    /**
     * Refreshes the status based on quantities and due dates.
     */
    public void refreshStatus() {
        if (this.returnedQuantity >= this.quantity) {
            this.status = "Returned";
        } else if (this.returnedQuantity > 0) {
            if (DateUtil.isOverdue(this.expectedReturnDate, this.actualReturnDate)) {
                this.status = "Overdue (Partially Returned)";
            } else {
                this.status = "Partially Returned";
            }
        } else {
            if (DateUtil.isOverdue(this.expectedReturnDate, "-")) {
                this.status = "Overdue";
            } else {
                this.status = "Issued";
            }
        }
    }

    public String toCsv() {
        return String.join(",",
                escapeCsv(recordId),
                escapeCsv(equipmentId),
                escapeCsv(equipmentName),
                escapeCsv(issuedTo),
                escapeCsv(userRole),
                String.valueOf(quantity),
                String.valueOf(returnedQuantity),
                escapeCsv(issueDate),
                escapeCsv(expectedReturnDate),
                escapeCsv(actualReturnDate),
                escapeCsv(status)
        );
    }

    public static IssueRecord fromCsv(String csvLine) throws ValidationException {
        String[] parts = csvLine.split(",", -1);
        if (parts.length < 11) {
            throw new ValidationException("Invalid Issue Record CSV line: " + csvLine);
        }
        try {
            String recId = unescapeCsv(parts[0]);
            String eqId = unescapeCsv(parts[1]);
            String eqName = unescapeCsv(parts[2]);
            String to = unescapeCsv(parts[3]);
            String role = unescapeCsv(parts[4]);
            int qty = Integer.parseInt(parts[5].trim());
            int retQty = Integer.parseInt(parts[6].trim());
            String issDate = unescapeCsv(parts[7]);
            String expDate = unescapeCsv(parts[8]);
            String actDate = unescapeCsv(parts[9]);
            String stat = unescapeCsv(parts[10]);
            return new IssueRecord(recId, eqId, eqName, to, role, qty, retQty, issDate, expDate, actDate, stat);
        } catch (NumberFormatException e) {
            throw new ValidationException("Corrupted numeric data in issue record: " + csvLine);
        }
    }

    private static String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace(",", ";");
    }

    private static String unescapeCsv(String str) {
        if (str == null) return "";
        return str.trim();
    }
}
