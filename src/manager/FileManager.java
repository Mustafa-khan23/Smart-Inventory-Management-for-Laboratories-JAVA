package manager;

import model.Equipment;
import model.IssueRecord;
import model.MaintenanceRecord;
import util.DateUtil;
import util.ValidationException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent file storage using standard Java File I/O.
 * Manages CSV-based data files in the 'data/' directory.
 */
public class FileManager {
    public static final String DATA_DIR = "data";
    public static final String EQUIPMENT_FILE = DATA_DIR + File.separator + "equipment.csv";
    public static final String ISSUE_FILE = DATA_DIR + File.separator + "issue_records.csv";
    public static final String MAINTENANCE_FILE = DATA_DIR + File.separator + "maintenance.csv";

    public FileManager() {
        ensureDataDirectory();
    }

    private void ensureDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ==========================================
    // Equipment Persistence
    // ==========================================

    public ArrayList<Equipment> loadEquipment() {
        ArrayList<Equipment> list = new ArrayList<>();
        File file = new File(EQUIPMENT_FILE);
        if (!file.exists()) {
            list = getSampleEquipment();
            saveEquipment(list);
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    list.add(Equipment.fromCsv(line));
                } catch (ValidationException e) {
                    System.err.println("Skipping malformed equipment line: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading equipment file: " + e.getMessage());
        }

        if (list.isEmpty()) {
            list = getSampleEquipment();
            saveEquipment(list);
        }
        return list;
    }

    public synchronized void saveEquipment(List<Equipment> list) {
        ensureDataDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EQUIPMENT_FILE))) {
            for (Equipment eq : list) {
                writer.write(eq.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving equipment file: " + e.getMessage());
        }
    }

    private ArrayList<Equipment> getSampleEquipment() {
        ArrayList<Equipment> list = new ArrayList<>();
        try {
            list.add(new Equipment("EQ001", "Arduino Uno", "Microcontroller", 25, 20, "IoT Lab", "Available", "2024-01-15"));
            list.add(new Equipment("EQ002", "Raspberry Pi 4", "Single Board Computer", 15, 8, "AI & Robotics Lab", "Available", "2024-02-10"));
            list.add(new Equipment("EQ003", "Digital Multimeter", "Electronics", 30, 26, "Electronics Lab", "Available", "2023-11-05"));
            list.add(new Equipment("EQ004", "Digital Storage Oscilloscope", "Electronics", 8, 5, "Signal Processing Lab", "Available", "2023-08-20"));
            list.add(new Equipment("EQ005", "Function Generator", "Electronics", 10, 9, "Electronics Lab", "Available", "2023-09-12"));
            list.add(new Equipment("EQ006", "Breadboard Kit", "Prototyping", 50, 42, "Hardware Lab", "Available", "2024-03-01"));
            list.add(new Equipment("EQ007", "DHT22 Temperature Sensor", "Sensors", 40, 35, "IoT Lab", "Available", "2024-04-10"));
            list.add(new Equipment("EQ008", "ESP32 Wi-Fi & BLE Module", "Microcontroller", 30, 22, "IoT Lab", "Available", "2024-05-18"));
            list.add(new Equipment("EQ009", "Digital Soldering Station", "Hardware Tools", 12, 10, "Fabrication Lab", "Available", "2023-12-01"));
        } catch (ValidationException e) {
            System.err.println("Sample initialization error: " + e.getMessage());
        }
        return list;
    }

    // ==========================================
    // Issue Records Persistence
    // ==========================================

    public ArrayList<IssueRecord> loadIssues() {
        ArrayList<IssueRecord> list = new ArrayList<>();
        File file = new File(ISSUE_FILE);
        if (!file.exists()) {
            list = getSampleIssues();
            saveIssues(list);
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    list.add(IssueRecord.fromCsv(line));
                } catch (ValidationException e) {
                    System.err.println("Skipping malformed issue line: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading issue file: " + e.getMessage());
        }

        if (list.isEmpty()) {
            list = getSampleIssues();
            saveIssues(list);
        }
        return list;
    }

    public synchronized void saveIssues(List<IssueRecord> list) {
        ensureDataDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ISSUE_FILE))) {
            for (IssueRecord rec : list) {
                writer.write(rec.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving issues file: " + e.getMessage());
        }
    }

    private ArrayList<IssueRecord> getSampleIssues() {
        ArrayList<IssueRecord> list = new ArrayList<>();
        try {
            // Some realistic sample records
            list.add(new IssueRecord("REC1001", "EQ001", "Arduino Uno", "Rahul Sharma", "Student", 3, 0, "2026-09-18", "2026-10-02", "-", "Issued"));
            list.add(new IssueRecord("REC1002", "EQ002", "Raspberry Pi 4", "Dr. Priya Nair", "Faculty", 2, 0, "2026-09-15", "2026-09-29", "-", "Issued"));
            list.add(new IssueRecord("REC1003", "EQ004", "Digital Storage Oscilloscope", "Vikram Verma", "Research Scholar", 1, 0, "2026-09-10", "2026-09-24", "-", "Overdue"));
            list.add(new IssueRecord("REC1004", "EQ003", "Digital Multimeter", "Ananya Sen", "Student", 4, 4, "2026-09-05", "2026-09-12", "2026-09-11", "Returned"));
        } catch (ValidationException e) {
            System.err.println("Sample issues initialization error: " + e.getMessage());
        }
        return list;
    }

    // ==========================================
    // Maintenance Persistence
    // ==========================================

    public ArrayList<MaintenanceRecord> loadMaintenance() {
        ArrayList<MaintenanceRecord> list = new ArrayList<>();
        File file = new File(MAINTENANCE_FILE);
        if (!file.exists()) {
            list = getSampleMaintenance();
            saveMaintenance(list);
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    list.add(MaintenanceRecord.fromCsv(line));
                } catch (ValidationException e) {
                    System.err.println("Skipping malformed maintenance line: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading maintenance file: " + e.getMessage());
        }

        if (list.isEmpty()) {
            list = getSampleMaintenance();
            saveMaintenance(list);
        }
        return list;
    }

    public synchronized void saveMaintenance(List<MaintenanceRecord> list) {
        ensureDataDirectory();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAINTENANCE_FILE))) {
            for (MaintenanceRecord rec : list) {
                writer.write(rec.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving maintenance file: " + e.getMessage());
        }
    }

    private ArrayList<MaintenanceRecord> getSampleMaintenance() {
        ArrayList<MaintenanceRecord> list = new ArrayList<>();
        try {
            list.add(new MaintenanceRecord("MN101", "EQ004", "Digital Storage Oscilloscope", "2026-09-20", "2026-10-05", "Probe frequency calibration & firmware check", "Tektronix Service Engineer", "Scheduled"));
            list.add(new MaintenanceRecord("MN102", "EQ009", "Digital Soldering Station", "2026-09-10", "2026-09-15", "Heating element replacement and tip tinning", "Lab Technician", "Completed"));
            list.add(new MaintenanceRecord("MN103", "EQ002", "Raspberry Pi 4", "2026-09-25", "2026-10-10", "Thermal stress and heatsink inspection", "Assistant Technician", "In Progress"));
        } catch (ValidationException e) {
            System.err.println("Sample maintenance initialization error: " + e.getMessage());
        }
        return list;
    }
}
