package model;

/**
 * Concrete subclass representing a Faculty user.
 * Demonstrates OOP Inheritance and Polymorphic Method Overriding.
 * Faculty can view inventory, issue and return laboratory items, and view reports.
 */
public class Faculty extends User {

    public Faculty(String username, String password, String fullName) {
        super(username, password, fullName, "Faculty");
    }

    @Override
    public boolean canModifyInventory() {
        return false; // Faculty cannot delete or edit base equipment catalog
    }

    @Override
    public boolean canManageMaintenance() {
        return false; // Maintenance scheduling is reserved for lab technicians/assistants
    }

    @Override
    public boolean canDeleteRecords() {
        return false;
    }
}
