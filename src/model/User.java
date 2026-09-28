package model;

/**
 * Abstract base class representing a user of the Laboratory Inventory Management system.
 * Demonstrates Object-Oriented Abstraction and Encapsulation.
 */
public abstract class User {
    private String username;
    private String password;
    private String fullName;
    private String role;

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    protected void setRole(String role) {
        this.role = role;
    }

    /**
     * Polymorphic permission checks demonstrating method overriding in subclasses.
     */
    public abstract boolean canModifyInventory();
    public abstract boolean canManageMaintenance();
    public abstract boolean canDeleteRecords();

    public boolean authenticate(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
