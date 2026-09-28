# Smart Inventory Management for Laboratories (Java Mini Project)

A complete, robust, and functional desktop GUI application built in Core Java using AWT/Swing and File I/O persistence. Designed specifically as an academic mini project demonstrating foundational and advanced Object-Oriented Programming (OOP) principles, exception handling, data structures, event handling, and desktop user interface engineering.

---

## 1. Problem Statement
In educational institutions and research facilities, laboratory apparatus and equipment are frequently tracked using manual registers or unstructured spreadsheets. This leads to common bottlenecks:
- Lack of real-time visibility into available versus checked-out apparatus stock.
- Missing accountability when items are returned late, damaged, or partially returned.
- Untracked calibration, servicing, and safety maintenance schedules.
- Absence of role-based segregation between administrative lab staff and academic faculty.

---

## 2. Proposed Solution
The **Smart Laboratory Inventory Management System** automates the entire apparatus lifecycle within college and research laboratories:
1. **Central Apparatus Catalog**: Tracks distinct equipment models, categories, laboratory room locations, quantities, and real-time operational status.
2. **Issue / Loan Workflow**: Validates current stock availability, generates unique checkout records, and tracks expected return dates.
3. **Return & Stock Replenishment**: Supports full and partial returns with automatic stock adjustment.
4. **Maintenance & Calibration Scheduler**: Tracks scheduled servicing, technicians, due dates, and flags equipment under maintenance.
5. **Role-Based Access Control**: Differentiates capabilities between **Lab Assistants** (full administrative authority) and **Faculty** (requisition, loan management, and reporting).
6. **File-Based Persistence**: Zero external database dependencies; persists data across application restarts via structured, human-readable CSV files in the `data/` directory.

---

## 3. Key Features

- **Dashboard**: High-level real-time summary cards (Total Apparatus Units, Available Stock, Issued Units, Under Maintenance, Upcoming Due Services) and quick-action shortcuts.
- **Equipment Inventory Management**:
  - Add, Update, and Delete laboratory equipment items.
  - Search by equipment name, ID, category, or lab location with real-time text matching.
  - Multi-category filtering (Microcontrollers, Single Board Computers, Electronics, Prototyping, Sensors, Hardware Tools).
  - Validation prevents duplicate IDs, negative numbers, or deleting equipment with active loans.
- **Equipment Checkout (Issue)**:
  - Dropdown selector displaying live available units.
  - Borrower classification (Student, Research Scholar, Faculty, Lab Assistant).
  - Configurable loan duration (defaults to 14 days).
  - Real-time stock decrement upon checkout.
- **Equipment Return**:
  - Displays all active checkouts requiring return.
  - Supports partial and complete item returns.
  - Automatically restores available equipment inventory.
- **Audit History**:
  - Chronological log of all checkout and return transactions.
  - Automatic overdue detection based on current system date.
  - Multi-parameter filtering by loan status and keywords.
- **Maintenance & Calibration**:
  - Schedule preventative maintenance cycles.
  - Mark services as Completed to automatically restore equipment availability.
  - Highlight overdue or upcoming maintenance tasks.
- **Utilization Reports**:
  - Aggregate inventory health and status metrics.
  - Category-wise utilization percentage table.

---

## 4. User Roles & Demo Credentials

| Role | Username | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Lab Assistant** | `assistant` | `assistant123` | **Full Admin**: Add/Edit/Delete equipment, Issue/Return items, Schedule & Complete maintenance, View reports. |
| **Faculty** | `faculty` | `faculty123` | **Operational**: View catalog, Issue/Return apparatus, View history and reports (Base catalog modification restricted). |

---

## 5. Technologies Used

- **Language**: Java Standard Edition (SE 8+ / JDK 17 / JDK 21 / JDK 27 compatible)
- **GUI Toolkit**: Java Swing (`JFrame`, `JPanel`, `CardLayout`, `JTable`, `DefaultTableModel`, `JButton`, `JTextField`, `JComboBox`, `JOptionPane`) & AWT (`Color`, `Font`, `BorderLayout`, `GridLayout`, `FlowLayout`)
- **Data Persistence**: Java Standard File I/O (`BufferedReader`, `BufferedWriter`, `FileReader`, `FileWriter`)
- **Collections Framework**: `ArrayList`, `List`, `Map`, `HashMap`
- **Date Architecture**: Modern Java Time API (`java.time.LocalDate`, `DateTimeFormatter`, `ChronoUnit`)

> **Note on Java Applets:**
> Java Applets (`java.applet.Applet`) were formally deprecated in Java 9 and completely removed from the standard JDK in Java 17+. To guarantee complete compatibility across both modern (JDK 17/21/27) and legacy environments without third-party frameworks, this application is engineered as a native **Java Swing Desktop Application** utilizing standard `JFrame` and `JPanel` components.

---

## 6. Java Programming Concepts Demonstrated (Viva Guide)

1. **Object-Oriented Programming (OOP)**:
   - **Abstraction**: Abstract base class `model.User` defines common attributes and abstract permission contracts (`canModifyInventory()`, `canManageMaintenance()`).
   - **Inheritance**: `LabAssistant` and `Faculty` extend `User`, inheriting core identity fields while customizing role behavior.
   - **Polymorphism**: The application refers to users via `User` references and dynamically executes overridden permission checks at runtime.
   - **Encapsulation**: Private fields across `Equipment`, `IssueRecord`, and `MaintenanceRecord` accessible via verified getters, setters, and business methods.
2. **Custom Exception Handling**:
   - `util.ValidationException` encapsulates all domain and validation rules (e.g., negative stock, duplicate IDs, invalid return quantities).
   - Standard checked `IOException` handling prevents file corruption and provides graceful recovery.
3. **Collections & Data Structures**:
   - `ArrayList<Equipment>`, `ArrayList<IssueRecord>`, and `ArrayList<MaintenanceRecord>` for in-memory dynamic state management.
   - `HashMap<String, int[]>` for aggregate category reporting calculations.
4. **Event-Driven Programming**:
   - Swing `ActionListener`, `DocumentListener` (for dynamic typing search), `MouseListener` (double-click row editing), and `ItemListener`.
5. **Thread Safety & GUI Architecture**:
   - Execution initialized strictly on the Swing **Event Dispatch Thread (EDT)** using `SwingUtilities.invokeLater`.
   - `CardLayout` architecture for responsive, flicker-free panel transitions.
6. **File Handling & Serialization**:
   - Comma-Separated Values (CSV) persistence in `data/equipment.csv`, `data/issue_records.csv`, and `data/maintenance.csv` with automated seed initialization.

---

## 7. Project Structure

```
Smart-Inventory-Management-for-Laboratories-JAVA/
├── bin/                       # Compiled Java bytecode (.class files)
├── data/                      # Persistent CSV data storage
│   ├── equipment.csv          # Equipment catalog records
│   ├── issue_records.csv      # Apparatus loans and returns
│   └── maintenance.csv        # Calibration and service records
├── src/
│   ├── main/
│   │   └── Main.java          # Application entry point and system initialization
│   ├── model/
│   │   ├── User.java              # Abstract user base class
│   │   ├── LabAssistant.java      # Subclass with full administrative rights
│   │   ├── Faculty.java           # Subclass with academic loan rights
│   │   ├── Equipment.java         # Apparatus entity model
│   │   ├── IssueRecord.java       # Loan transaction entity model
│   │   └── MaintenanceRecord.java # Service record entity model
│   ├── manager/
│   │   ├── FileManager.java         # File I/O reading, writing, and sample seeding
│   │   ├── LoginManager.java        # Authentication and session state
│   │   ├── InventoryManager.java    # Equipment CRUD, search, and stock tracking
│   │   ├── IssueReturnManager.java  # Checkout, partial return, and overdue rules
│   │   └── MaintenanceManager.java  # Scheduling and servicing lifecycle
│   ├── ui/
│   │   ├── UITheme.java             # Color palette, font constants, and styles
│   │   ├── LoginFrame.java          # Login window with demo role selectors
│   │   ├── MainDashboardFrame.java  # Main window with CardLayout & navigation
│   │   ├── DashboardPanel.java      # Summary metrics and quick action cards
│   │   ├── EquipmentPanel.java      # Equipment catalog management table & dialogs
│   │   ├── IssuePanel.java          # Apparatus checkout requisition form
│   │   ├── ReturnPanel.java         # Active loan return and stock replenishment
│   │   ├── HistoryPanel.java        # Audit history log and multi-filter controls
│   │   ├── MaintenancePanel.java    # Preventative calibration and repair manager
│   │   └── ReportPanel.java         # Inventory health and category utilization
│   ├── util/
│   │   ├── DateUtil.java            # ISO date validation and calculations
│   │   └── ValidationException.java # Domain validation checked exception
│   └── test/
│       ├── SystemWorkflowTest.java  # End-to-end automated lifecycle test suite
│       └── UISmokeTest.java         # Swing GUI initialization smoke test
├── run.sh                     # Linux / macOS compile and launch script
└── README.md                  # Project documentation
```

---

## 8. How to Compile and Run

### Prerequisites
- Java Development Kit (JDK 8 or higher; JDK 17, 21, or 27 recommended)
- Standard terminal / command prompt

### Option A: Using the Convenience Script (Linux / macOS)
```bash
./run.sh
```

### Option B: Manual Compilation & Execution
1. Open a terminal in the project root directory.
2. Compile all source files into the `bin/` directory:
   ```bash
   mkdir -p bin
   javac -d bin -sourcepath src src/main/Main.java
   ```
3. Run the application:
   ```bash
   java -cp bin main.Main
   ```

### Running the Automated Test Suites
To verify all business rules, persistence across restarts, and GUI smoke tests:
```bash
# Run system workflow and persistence test suite
javac -d bin -sourcepath src src/test/SystemWorkflowTest.java
java -cp bin test.SystemWorkflowTest

# Run UI component smoke test
javac -d bin -sourcepath src src/test/UISmokeTest.java
java -cp bin test.UISmokeTest
```

---

## 9. Sample Initial Data

On first run, the system automatically populates realistic laboratory apparatus:
- **EQ001**: Arduino Uno (Microcontroller - IoT Lab)
- **EQ002**: Raspberry Pi 4 (Single Board Computer - AI & Robotics Lab)
- **EQ003**: Digital Multimeter (Electronics - Electronics Lab)
- **EQ004**: Digital Storage Oscilloscope (Electronics - Signal Processing Lab)
- **EQ005**: Function Generator (Electronics - Electronics Lab)
- **EQ006**: Breadboard Kit (Prototyping - Hardware Lab)
- **EQ007**: DHT22 Temperature Sensor (Sensors - IoT Lab)
- **EQ008**: ESP32 Wi-Fi & BLE Module (Microcontroller - IoT Lab)
- **EQ009**: Digital Soldering Station (Hardware Tools - Fabrication Lab)

---

## 10. Screenshots Placeholder

*(Screenshots can be added here for lab records and viva documentation)*

1. **Login Screen**: Clean credential authentication with quick-fill demo buttons.
2. **Dashboard Overview**: Metrics cards (Total Units, Available, Issued, Under Maintenance, Upcoming Tasks).
3. **Equipment Management**: Searchable catalog with category filtering and Add/Edit dialogs.
4. **Issue Requisition**: Checkout form with live availability counters and due date calculation.
5. **Return Center**: Settle apparatus check-in with partial return support.
6. **Maintenance & Calibration**: Scheduled service tracking and completion workflows.
7. **Reports & Utilization**: Category breakdown table with real-time utilization rates.

---

## 11. Future Scope

1. **Barcode / QR Code Scanner**: Integration with USB or camera barcode scanners for rapid equipment check-in/out.
2. **Relational Database Migration**: Transition from CSV files to SQLite / MySQL via JDBC for multi-client concurrent laboratories.
3. **Automated Email / SMS Alerts**: Notification dispatch for overdue items and scheduled calibration warnings.
4. **RFID Integration**: Real-time lab shelf scanning for automated inventory audits.
