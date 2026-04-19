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

    /**
     * Deprecated method to convert string to UserRole. AuthenticationService and TeamService should be used instead for role management.
     * 
     * @param role
     * @return
     */
    @Deprecated
    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.getDisplayName().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}
