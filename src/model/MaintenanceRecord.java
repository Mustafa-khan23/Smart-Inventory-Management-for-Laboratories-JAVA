package model;

import util.DateUtil;
import util.ValidationException;

/**
 * Model class representing a laboratory equipment maintenance schedule or service event.
 */
public class MaintenanceRecord {
    private String maintenanceId;
    private String equipmentId;
    private String equipmentName;
    private String maintenanceDate;
    private String nextMaintenanceDate;
    private String description;
    private String technician;
    private String status;

    public MaintenanceRecord() {
    }

    public MaintenanceRecord(String maintenanceId, String equipmentId, String equipmentName,
                             String maintenanceDate, String nextMaintenanceDate,
                             String description, String technician, String status) throws ValidationException {
        setMaintenanceId(maintenanceId);
        setEquipmentId(equipmentId);
        setEquipmentName(equipmentName);
        setMaintenanceDate(maintenanceDate);
        setNextMaintenanceDate(nextMaintenanceDate);
        setDescription(description);
        setTechnician(technician);
        setStatus(status);
    }

    public String getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(String maintenanceId) throws ValidationException {
        if (maintenanceId == null || maintenanceId.trim().isEmpty()) {
            throw new ValidationException("Maintenance ID cannot be empty.");
        }
        this.maintenanceId = maintenanceId.trim().toUpperCase();
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) throws ValidationException {
        if (equipmentId == null || equipmentId.trim().isEmpty()) {
            throw new ValidationException("Equipment ID cannot be empty.");
        }
        this.equipmentId = equipmentId.trim().toUpperCase();
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = (equipmentName == null) ? "" : equipmentName.trim();
    }

    public String getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(String maintenanceDate) throws ValidationException {
        if (maintenanceDate != null && !maintenanceDate.trim().isEmpty() && !DateUtil.isValidDate(maintenanceDate)) {
            throw new ValidationException("Invalid maintenance date: " + maintenanceDate + ". Format: YYYY-MM-DD");
        }
        this.maintenanceDate = (maintenanceDate == null || maintenanceDate.trim().isEmpty()) ? DateUtil.getTodayString() : maintenanceDate.trim();
    }

    public String getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public void setNextMaintenanceDate(String nextMaintenanceDate) throws ValidationException {
        if (nextMaintenanceDate != null && !nextMaintenanceDate.trim().isEmpty() && !DateUtil.isValidDate(nextMaintenanceDate)) {
            throw new ValidationException("Invalid next maintenance date: " + nextMaintenanceDate + ". Format: YYYY-MM-DD");
        }
        this.nextMaintenanceDate = (nextMaintenanceDate == null || nextMaintenanceDate.trim().isEmpty()) ? "-" : nextMaintenanceDate.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description.trim();
    }

    public String getTechnician() {
        return technician;
    }

    public void setTechnician(String technician) {
        this.technician = (technician == null) ? "Internal Staff" : technician.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status == null || status.trim().isEmpty()) ? "Scheduled" : status.trim();
    }

    public boolean isUpcoming(int days) {
        if ("Completed".equalsIgnoreCase(this.status)) {
            return false;
        }
        return DateUtil.isDueWithinDays(this.maintenanceDate, days) ||
               DateUtil.isDueWithinDays(this.nextMaintenanceDate, days);
    }

    public String toCsv() {
        return String.join(",",
                escapeCsv(maintenanceId),
                escapeCsv(equipmentId),
                escapeCsv(equipmentName),
                escapeCsv(maintenanceDate),
                escapeCsv(nextMaintenanceDate),
                escapeCsv(description),
                escapeCsv(technician),
                escapeCsv(status)
        );
    }

    public static MaintenanceRecord fromCsv(String csvLine) throws ValidationException {
        String[] parts = csvLine.split(",", -1);
        if (parts.length < 8) {
            throw new ValidationException("Invalid Maintenance CSV line: " + csvLine);
        }
        String mId = unescapeCsv(parts[0]);
        String eqId = unescapeCsv(parts[1]);
        String eqName = unescapeCsv(parts[2]);
        String mDate = unescapeCsv(parts[3]);
        String nDate = unescapeCsv(parts[4]);
        String desc = unescapeCsv(parts[5]);
        String tech = unescapeCsv(parts[6]);
        String stat = unescapeCsv(parts[7]);
        return new MaintenanceRecord(mId, eqId, eqName, mDate, nDate, desc, tech, stat);
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
