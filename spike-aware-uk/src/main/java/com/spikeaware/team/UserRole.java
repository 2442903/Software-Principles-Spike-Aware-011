package com.spikeaware.team;


/**
 * Enum representing different user roles in the system.
 */
public enum UserRole {
    PUBLIC_USER("Public User"),
    MODERATOR("Moderator"),
    ADMINISTRATOR("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}
