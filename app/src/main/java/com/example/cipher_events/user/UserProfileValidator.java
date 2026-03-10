package com.example.cipher_events.user;

import java.util.regex.Pattern;

/**
 * Validation helper for user profile input.
 */
public final class UserProfileValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private UserProfileValidator() {
        // Utility class
    }

    public static void validateRequiredProfileFields(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Email format is invalid.");
        }
    }

    public static void validateOptionalPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return;
        }

        String cleaned = phoneNumber.trim();
        if (cleaned.length() < 7 || cleaned.length() > 20) {
            throw new IllegalArgumentException("Phone number length is invalid.");
        }
    }
}