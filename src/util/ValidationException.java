package util;

/**
 * Custom checked exception used across the Laboratory Inventory Management application
 * to indicate business logic and validation errors.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
