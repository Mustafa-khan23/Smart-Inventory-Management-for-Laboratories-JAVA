package model;

import util.ValidationException;

/**
 * Model class representing an Equipment item in the laboratory inventory.
 * Fully encapsulated with validation rules.
 */
public class Equipment {
    private String id;
    private String name;
    private String category;
    private int quantity;
    private int availableQuantity;
    private String location;
    private String status;
    private String purchaseDate;

    public Equipment() {
    }

    public Equipment(String id, String name, String category, int quantity, int availableQuantity,
                     String location, String status, String purchaseDate) throws ValidationException {
        setId(id);
        setName(name);
        setCategory(category);
        setLocation(location);
        setPurchaseDate(purchaseDate);
        setQuantities(quantity, availableQuantity);
        setStatus(status);
        updateStatus();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Equipment ID cannot be empty.");
        }
        this.id = id.trim().toUpperCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Equipment Name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();
    }

    public int getQuantity() {
        return quantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setQuantities(int totalQty, int availQty) throws ValidationException {
        if (totalQty < 0) {
            throw new ValidationException("Total quantity cannot be negative.");
        }
        if (availQty < 0) {
            throw new ValidationException("Available quantity cannot be negative.");
        }
        if (availQty > totalQty) {
            throw new ValidationException("Available quantity (" + availQty + ") cannot exceed total quantity (" + totalQty + ").");
        }
        this.quantity = totalQty;
        this.availableQuantity = availQty;
        updateStatus();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = (location == null || location.trim().isEmpty()) ? "Main Lab" : location.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status == null || status.trim().isEmpty()) ? "Available" : status.trim();
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = (purchaseDate == null || purchaseDate.trim().isEmpty()) ? "-" : purchaseDate.trim();
    }

    /**
     * Issues a specific quantity of this equipment.
     */
    public void issue(int qty) throws ValidationException {
        if (qty <= 0) {
            throw new ValidationException("Quantity to issue must be greater than zero.");
        }
        if (qty > this.availableQuantity) {
            throw new ValidationException("Insufficient stock! Available: " + this.availableQuantity + ", Requested: " + qty);
        }
        this.availableQuantity -= qty;
        updateStatus();
    }

    /**
     * Returns a specific quantity of this equipment.
     */
    public void returnItem(int qty) throws ValidationException {
        if (qty <= 0) {
            throw new ValidationException("Returned quantity must be greater than zero.");
        }
        if (this.availableQuantity + qty > this.quantity) {
            throw new ValidationException("Returned quantity would exceed total equipment inventory (" + this.quantity + ").");
        }
        this.availableQuantity += qty;
        updateStatus();
    }

    /**
     * Clears maintenance flag and recalibrates status.
     */
    public void clearMaintenanceStatus() {
        this.status = "Available";
        updateStatus();
    }

    /**
     * Synchronizes status string based on availability and maintenance.
     */
    public void updateStatus() {
        if ("Under Maintenance".equalsIgnoreCase(this.status)) {
            return; // Preserve maintenance status until maintenance completed
        }
        if (this.availableQuantity == 0) {
            this.status = "Issued (Out of Stock)";
        } else if (this.availableQuantity < this.quantity) {
            this.status = "Partially Available";
        } else {
            this.status = "Available";
        }
    }

    public String toCsv() {
        return String.join(",",
                escapeCsv(id),
                escapeCsv(name),
                escapeCsv(category),
                String.valueOf(quantity),
                String.valueOf(availableQuantity),
                escapeCsv(location),
                escapeCsv(status),
                escapeCsv(purchaseDate)
        );
    }

    public static Equipment fromCsv(String csvLine) throws ValidationException {
        String[] parts = csvLine.split(",", -1);
        if (parts.length < 8) {
            throw new ValidationException("Invalid Equipment CSV record: " + csvLine);
        }
        try {
            String id = unescapeCsv(parts[0]);
            String name = unescapeCsv(parts[1]);
            String category = unescapeCsv(parts[2]);
            int qty = Integer.parseInt(parts[3].trim());
            int avail = Integer.parseInt(parts[4].trim());
            String loc = unescapeCsv(parts[5]);
            String stat = unescapeCsv(parts[6]);
            String date = unescapeCsv(parts[7]);
            return new Equipment(id, name, category, qty, avail, loc, stat, date);
        } catch (NumberFormatException e) {
            throw new ValidationException("Corrupted numeric data in equipment record: " + csvLine);
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

    @Override
    public String toString() {
        return id + " - " + name + " (Available: " + availableQuantity + "/" + quantity + ")";
    }
}
