package com.spikeaware.service;

import com.spikeaware.team.UserRole;

/**
 * AuthenticationService handles user authentication and role management.
 * Currently uses hardcoded credentials (should be replaced with proper auth system).
 */
public class AuthenticationService {
    private static final String MOD_PASSWORD = "mod123";
    private static final String ADMIN_PASSWORD = "admin123";
    private UserRole currentRole = UserRole.PUBLIC_USER;

    /**
     * Get the current user role.
     *
     * @return the current UserRole
     */
    public UserRole getCurrentRole() {
        return currentRole;
    }

    /**
     * Set the current user role.
     *
     * @param role the role to set
     */
    public void setCurrentRole(UserRole role) {
        this.currentRole = role;
    }

    /**
     * Authenticate a user with a password and return their role.
     *
     * @param password the password to authenticate
     * @return the UserRole if authentication succeeds, null otherwise
     */
    public UserRole authenticate(String password) {
        if (MOD_PASSWORD.equals(password)) {
            this.currentRole = UserRole.MODERATOR;
            return UserRole.MODERATOR;
        } else if (ADMIN_PASSWORD.equals(password)) {
            this.currentRole = UserRole.ADMINISTRATOR;
            return UserRole.ADMINISTRATOR;
        }
        return null;
    }

    /**
     * Logout the current user, returning to PUBLIC_USER role.
     */
    public void logout() {
        this.currentRole = UserRole.PUBLIC_USER;
    }

    /**
     * Check if current role is strictly public user (no permissions).
     *
     * @return true if user has no permissions
     */
    public boolean isPublicMode() {
        return currentRole == UserRole.PUBLIC_USER;
    }

    /**
     * Check if current role is moderator or above.
     *
     * @return true if user has moderator permissions
     */
    public boolean isModeratorMode() {
        return currentRole != UserRole.PUBLIC_USER;
    }

    /**
     * Check if current role is administrator.
     *
     * @return true if user is an administrator
     */
    public boolean isAdministratorMode() {
        return currentRole == UserRole.ADMINISTRATOR;
    }
}
