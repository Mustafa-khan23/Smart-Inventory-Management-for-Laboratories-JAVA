package manager;

import model.Equipment;
import model.MaintenanceRecord;
import util.DateUtil;
import util.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic manager for laboratory equipment maintenance and calibration.
 */
public class MaintenanceManager {
    private final ArrayList<MaintenanceRecord> maintenanceList;
    private final FileManager fileManager;

    public MaintenanceManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.maintenanceList = fileManager.loadMaintenance();
    }

    public synchronized ArrayList<MaintenanceRecord> getAllMaintenance() {
        return new ArrayList<>(maintenanceList);
    }

    public synchronized MaintenanceRecord getById(String id) {
        if (id == null) return null;
        for (MaintenanceRecord rec : maintenanceList) {
            if (rec.getMaintenanceId().equalsIgnoreCase(id.trim())) {
                return rec;
            }
        }
        return null;
    }

    public synchronized void scheduleMaintenance(MaintenanceRecord rec, InventoryManager invMgr) throws ValidationException {
        if (rec == null) {
            throw new ValidationException("Maintenance record cannot be null.");
        }
        if (getById(rec.getMaintenanceId()) != null) {
            throw new ValidationException("Maintenance ID '" + rec.getMaintenanceId() + "' already exists!");
        }

        Equipment eq = invMgr.getEquipmentById(rec.getEquipmentId());
        if (eq == null) {
            throw new ValidationException("Equipment with ID '" + rec.getEquipmentId() + "' does not exist.");
        }
        rec.setEquipmentName(eq.getName());

        maintenanceList.add(0, rec);
        fileManager.saveMaintenance(maintenanceList);

        // If status is In Progress, mark equipment status as Under Maintenance
        if ("In Progress".equalsIgnoreCase(rec.getStatus())) {
            eq.setStatus("Under Maintenance");
            invMgr.saveChanges();
        }
    }

    public synchronized void updateMaintenance(MaintenanceRecord updated, InventoryManager invMgr) throws ValidationException {
        if (updated == null) {
            throw new ValidationException("Maintenance record cannot be null.");
        }
        MaintenanceRecord existing = getById(updated.getMaintenanceId());
        if (existing == null) {
            throw new ValidationException("Maintenance record '" + updated.getMaintenanceId() + "' not found.");
        }

        existing.setEquipmentId(updated.getEquipmentId());
        existing.setEquipmentName(updated.getEquipmentName());
        existing.setMaintenanceDate(updated.getMaintenanceDate());
        existing.setNextMaintenanceDate(updated.getNextMaintenanceDate());
        existing.setDescription(updated.getDescription());
        existing.setTechnician(updated.getTechnician());
        existing.setStatus(updated.getStatus());

        fileManager.saveMaintenance(maintenanceList);

        Equipment eq = invMgr.getEquipmentById(existing.getEquipmentId());
        if (eq != null) {
            if ("In Progress".equalsIgnoreCase(existing.getStatus())) {
                eq.setStatus("Under Maintenance");
            } else if ("Completed".equalsIgnoreCase(existing.getStatus()) && "Under Maintenance".equalsIgnoreCase(eq.getStatus())) {
                eq.clearMaintenanceStatus();
            }
            invMgr.saveChanges();
        }
    }

    public synchronized void markAsCompleted(String id, InventoryManager invMgr) throws ValidationException {
        MaintenanceRecord rec = getById(id);
        if (rec == null) {
            throw new ValidationException("Maintenance record '" + id + "' not found.");
        }
        rec.setStatus("Completed");
        fileManager.saveMaintenance(maintenanceList);

        Equipment eq = invMgr.getEquipmentById(rec.getEquipmentId());
        if (eq != null && "Under Maintenance".equalsIgnoreCase(eq.getStatus())) {
            eq.clearMaintenanceStatus();
            invMgr.saveChanges();
        }
    }

    public synchronized void deleteMaintenance(String id, InventoryManager invMgr) throws ValidationException {
        MaintenanceRecord rec = getById(id);
        if (rec == null) {
            throw new ValidationException("Maintenance record '" + id + "' not found.");
        }
        maintenanceList.remove(rec);
        fileManager.saveMaintenance(maintenanceList);

        Equipment eq = invMgr.getEquipmentById(rec.getEquipmentId());
        if (eq != null && "Under Maintenance".equalsIgnoreCase(eq.getStatus())) {
            eq.clearMaintenanceStatus();
            invMgr.saveChanges();
        }
    }

    public synchronized List<MaintenanceRecord> getUpcomingMaintenance(int days) {
        List<MaintenanceRecord> upcoming = new ArrayList<>();
        for (MaintenanceRecord rec : maintenanceList) {
            if (rec.isUpcoming(days)) {
                upcoming.add(rec);
            }
        }
        return upcoming;
    }

    public synchronized int getUpcomingCount() {
        return getUpcomingMaintenance(14).size();
    }

    public synchronized int getScheduledOrInProgressCount() {
        int count = 0;
        for (MaintenanceRecord rec : maintenanceList) {
            if (!"Completed".equalsIgnoreCase(rec.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public synchronized String generateNextId() {
        int max = 100;
        for (MaintenanceRecord rec : maintenanceList) {
            try {
                String num = rec.getMaintenanceId().replaceAll("[^0-9]", "");
                if (!num.isEmpty()) {
                    int val = Integer.parseInt(num);
                    if (val > max) max = val;
                }
            } catch (Exception ignored) {
            }
        }
        return "MN" + (max + 1);
    }
}
