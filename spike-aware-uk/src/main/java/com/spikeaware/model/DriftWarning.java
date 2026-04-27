package com.spikeaware.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents data drift warnings detected during resource submission.
 * Warnings are stored with resources to alert moderators of potential issues.
 */
public class DriftWarning {
    public enum WarningType {
        DUPLICATE_URL,           // URL already exists in system
        INVALID_URL,             // URL format is invalid
        SUSPICIOUS_URL,          // URL contains suspicious patterns
        SUSPICIOUS_TITLE,        // Title contains suspicious patterns
        NEAR_DUPLICATE_URL       // URL is similar to existing URL
    }

    private String warningId;
    private long resourceId;
    private WarningType type;
    private String message;
    private String suggestion;
    private int severity;  // 1-5
    private LocalDateTime detectedAt;
    private boolean acknowledged;

    /**
     * Constructor for DriftWarning
     */
    public DriftWarning(long resourceId, WarningType type, String message, String suggestion, int severity) {
        this.warningId = System.nanoTime() + "_" + resourceId;
        this.resourceId = resourceId;
        this.type = type;
        this.message = message;
        this.suggestion = suggestion;
        this.severity = severity;
        this.detectedAt = LocalDateTime.now();
        this.acknowledged = false;
    }

    // Getters
    public String getWarningId() {
        return warningId;
    }

    public long getResourceId() {
        return resourceId;
    }

    public WarningType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public int getSeverity() {
        return severity;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    @Override
    public String toString() {
        return "DriftWarning{" +
                "type=" + type +
                ", severity=" + severity +
                ", message='" + message + '\'' +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }
}
