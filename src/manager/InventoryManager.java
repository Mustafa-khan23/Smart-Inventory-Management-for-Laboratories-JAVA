package manager;

import model.Equipment;
import util.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic manager for laboratory equipment inventory.
 * Provides CRUD operations, validation rules, search filters, and inventory analytics.
 */
public class InventoryManager {
    private final ArrayList<Equipment> equipmentList;
    private final FileManager fileManager;

    public InventoryManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.equipmentList = fileManager.loadEquipment();
    }

    public synchronized ArrayList<Equipment> getAllEquipment() {
        return new ArrayList<>(equipmentList);
    }

    public synchronized Equipment getEquipmentById(String id) {
        if (id == null) return null;
        for (Equipment eq : equipmentList) {
            if (eq.getId().equalsIgnoreCase(id.trim())) {
                return eq;
            }
        }
        return null;
    }

    public synchronized void addEquipment(Equipment newEq) throws ValidationException {
        if (newEq == null) {
            throw new ValidationException("Equipment cannot be null.");
        }
        if (getEquipmentById(newEq.getId()) != null) {
            throw new ValidationException("Equipment with ID '" + newEq.getId() + "' already exists! Please choose a unique ID.");
        }
        equipmentList.add(newEq);
        fileManager.saveEquipment(equipmentList);
    }

    public synchronized void updateEquipment(Equipment updatedEq) throws ValidationException {
        if (updatedEq == null) {
            throw new ValidationException("Equipment cannot be null.");
        }
        Equipment existing = getEquipmentById(updatedEq.getId());
        if (existing == null) {
            throw new ValidationException("Equipment with ID '" + updatedEq.getId() + "' not found.");
        }

        // Calculate currently issued units: total - available
        int currentlyIssued = existing.getQuantity() - existing.getAvailableQuantity();
        if (updatedEq.getQuantity() < currentlyIssued) {
            throw new ValidationException("Cannot reduce total quantity below currently issued units (" + currentlyIssued + ").");
        }

        // Adjust available quantity properly based on new total
        int newAvailable = updatedEq.getQuantity() - currentlyIssued;
        existing.setName(updatedEq.getName());
        existing.setCategory(updatedEq.getCategory());
        existing.setLocation(updatedEq.getLocation());
        existing.setPurchaseDate(updatedEq.getPurchaseDate());
        existing.setStatus(updatedEq.getStatus());
        existing.setQuantities(updatedEq.getQuantity(), newAvailable);

        fileManager.saveEquipment(equipmentList);
    }

    public synchronized void deleteEquipment(String id) throws ValidationException {
        Equipment eq = getEquipmentById(id);
        if (eq == null) {
            throw new ValidationException("Equipment with ID '" + id + "' not found.");
        }
        int currentlyIssued = eq.getQuantity() - eq.getAvailableQuantity();
        if (currentlyIssued > 0) {
            throw new ValidationException("Cannot delete equipment '" + eq.getName() + "' because " + currentlyIssued + " unit(s) are currently issued out.");
        }
        equipmentList.remove(eq);
        fileManager.saveEquipment(equipmentList);
    }

    public synchronized List<Equipment> searchEquipment(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEquipment();
        }
        String q = keyword.trim().toLowerCase();
        List<Equipment> results = new ArrayList<>();
        for (Equipment eq : equipmentList) {
            if (eq.getId().toLowerCase().contains(q) ||
                eq.getName().toLowerCase().contains(q) ||
                eq.getCategory().toLowerCase().contains(q) ||
                eq.getLocation().toLowerCase().contains(q) ||
                eq.getStatus().toLowerCase().contains(q)) {
                results.add(eq);
            }
        }
        return results;
    }

    public synchronized void saveChanges() {
        fileManager.saveEquipment(equipmentList);
    }

    // ==========================================
    // Analytics & Metrics
    // ==========================================

    public synchronized int getTotalEquipmentTypesCount() {
        return equipmentList.size();
    }

    public synchronized int getTotalQuantityUnits() {
        int sum = 0;
        for (Equipment eq : equipmentList) {
            sum += eq.getQuantity();
        }
        return sum;
    }

    public synchronized int getTotalAvailableUnits() {
        int sum = 0;
        for (Equipment eq : equipmentList) {
            sum += eq.getAvailableQuantity();
        }
        return sum;
    }

    public synchronized int getTotalIssuedUnits() {
        return getTotalQuantityUnits() - getTotalAvailableUnits();
    }

    public synchronized int getUnderMaintenanceCount() {
        int count = 0;
        for (Equipment eq : equipmentList) {
            if ("Under Maintenance".equalsIgnoreCase(eq.getStatus())) {
                count++;
            }
        }
        return count;
    }
}
