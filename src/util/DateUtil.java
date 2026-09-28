package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility helper class for date parsing, formatting, and validation.
 */
public class DateUtil {
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    public static String getTodayString() {
        return LocalDate.now().format(FORMATTER);
    }

    public static String getDefaultReturnDate(int daysLater) {
        return LocalDate.now().plusDays(daysLater).format(FORMATTER);
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(dateStr.trim(), FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static LocalDate parse(String dateStr) throws ValidationException {
        if (!isValidDate(dateStr)) {
            throw new ValidationException("Invalid date format: '" + dateStr + "'. Expected format: YYYY-MM-DD");
        }
        return LocalDate.parse(dateStr.trim(), FORMATTER);
    }

    public static boolean isOverdue(String expectedReturnDate, String actualReturnDate) {
        if (actualReturnDate != null && !actualReturnDate.trim().isEmpty() && !"-".equals(actualReturnDate.trim())) {
            return false; // Already returned
        }
        if (!isValidDate(expectedReturnDate)) {
            return false;
        }
        try {
            LocalDate expected = LocalDate.parse(expectedReturnDate.trim(), FORMATTER);
            return LocalDate.now().isAfter(expected);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDueWithinDays(String targetDateStr, int days) {
        if (!isValidDate(targetDateStr)) {
            return false;
        }
        try {
            LocalDate target = LocalDate.parse(targetDateStr.trim(), FORMATTER);
            LocalDate today = LocalDate.now();
            long diff = ChronoUnit.DAYS.between(today, target);
            return diff >= 0 && diff <= days;
        } catch (Exception e) {
            return false;
        }
    }

    public static long daysBetween(String startStr, String endStr) {
        try {
            LocalDate start = LocalDate.parse(startStr.trim(), FORMATTER);
            LocalDate end = LocalDate.parse(endStr.trim(), FORMATTER);
            return ChronoUnit.DAYS.between(start, end);
        } catch (Exception e) {
            return 0;
        }
    }
}
