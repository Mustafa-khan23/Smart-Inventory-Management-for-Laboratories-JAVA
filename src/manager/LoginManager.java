package manager;

import model.Faculty;
import model.LabAssistant;
import model.User;
import util.ValidationException;

import java.util.ArrayList;

/**
 * Handles user authentication, session state, and predefined demo credentials.
 */
public class LoginManager {
    private final ArrayList<User> users = new ArrayList<>();
    private User currentUser = null;

    public LoginManager() {
        initDefaultUsers();
    }

    private void initDefaultUsers() {
        // Predefined demo accounts per requirements
        users.add(new LabAssistant("assistant", "assistant123", "Rajesh Kumar (Lab Assistant)"));
        users.add(new Faculty("faculty", "faculty123", "Dr. Shalini Gupta (Faculty)"));
    }

    public User authenticate(String username, String password) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Please enter your username.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Please enter your password.");
        }

        String trimmedUser = username.trim();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(trimmedUser)) {
                if (user.authenticate(password)) {
                    this.currentUser = user;
                    return user;
                } else {
                    throw new ValidationException("Invalid password. Please check your credentials.");
                }
            }
        }
        throw new ValidationException("User '" + trimmedUser + "' not found. Valid roles: 'assistant' or 'faculty'.");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
