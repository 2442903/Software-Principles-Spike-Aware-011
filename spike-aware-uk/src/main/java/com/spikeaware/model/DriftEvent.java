package com.spikeaware.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single data drift event detected in resources.
 */
public class DriftEvent {
    public enum DriftType {
        DUPLICATE_URL,           // Same URL found in multiple resources
        URL_CHANGE,              // URL changed for an existing resource
        INVALID_URL,             // URL format is invalid
        DATA_INCONSISTENCY,      // Inconsistent data (e.g., title changed)
        MISSING_DATA,            // Required field is missing
        SUSPICIOUS_PATTERN       // Suspicious patterns in URL or title
    }

    private String driftId;
    private DriftType type;
    private long primaryResourceId;
    private long secondaryResourceId;  // For duplicate issues
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String description;
    private LocalDateTime detectedAt;
    private int severity;  // 1-5, where 5 is critical

    /**
     * Constructor for DriftEvent
     */
    public DriftEvent(DriftType type, long resourceId, String fieldName, 
                     String oldValue, String newValue, String description, int severity) {
        this.driftId = System.nanoTime() + "_" + resourceId;
        this.type = type;
        this.primaryResourceId = resourceId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.description = description;
        this.detectedAt = LocalDateTime.now();
        this.severity = severity;
    }

    /**
     * Constructor for duplicate detection
     */
    public DriftEvent(long resourceId1, long resourceId2, String commonValue, int severity) {
        this.driftId = System.nanoTime() + "_dup";
        this.type = DriftType.DUPLICATE_URL;
        this.primaryResourceId = resourceId1;
        this.secondaryResourceId = resourceId2;
        this.newValue = commonValue;
        this.description = "Duplicate URL detected";
        this.detectedAt = LocalDateTime.now();
        this.severity = severity;
    }

    // Getters and Setters
    public String getDriftId() {
        return driftId;
    }

    public DriftType getType() {
        return type;
    }

    public long getPrimaryResourceId() {
        return primaryResourceId;
    }

    public long getSecondaryResourceId() {
        return secondaryResourceId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public int getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return "DriftEvent{" +
                "driftId='" + driftId + '\'' +
                ", type=" + type +
                ", primaryResourceId=" + primaryResourceId +
                ", severity=" + severity +
                ", description='" + description + '\'' +
                ", detectedAt=" + detectedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriftEvent that = (DriftEvent) o;
        return Objects.equals(driftId, that.driftId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driftId);
    }
}
