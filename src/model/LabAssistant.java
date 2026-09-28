package model;

/**
 * Concrete subclass representing a Lab Assistant.
 * Demonstrates OOP Inheritance and Polymorphic Method Overriding.
 * Lab Assistant has full operational and administrative rights on inventory and maintenance.
 */
public class LabAssistant extends User {

    public LabAssistant(String username, String password, String fullName) {
        super(username, password, fullName, "Lab Assistant");
    }

    @Override
    public boolean canModifyInventory() {
        return true;
    }

    @Override
    public boolean canManageMaintenance() {
        return true;
    }

    @Override
    public boolean canDeleteRecords() {
        return true;
    }
}
