package com.spikeaware.model;

/**
 * Enum representing different resource statuses in the system.
 */
public enum ResourceStatus {
    PENDING("Pending"),
    APPROVED("Published"),
    REJECTED("Rejected"),
    ARCHIVED("Archived");

    private final String displayName;

    ResourceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}