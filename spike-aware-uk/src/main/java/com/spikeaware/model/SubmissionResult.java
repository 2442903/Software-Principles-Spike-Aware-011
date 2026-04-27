package com.spikeaware.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a resource submission, including the created resource
 * and any drift warnings detected during submission.
 */
public class SubmissionResult {
    private Resource resource;
    private List<DriftWarning> warnings;
    private String status;  // SUCCESS, SUCCESS_WITH_WARNINGS, FAILED
    private String message;

    /**
     * Constructor for successful submission with no warnings
     */
    public SubmissionResult(Resource resource) {
        this.resource = resource;
        this.warnings = new ArrayList<>();
        this.status = "SUCCESS";
        this.message = "Resource submitted successfully.";
    }

    /**
     * Constructor for submission with warnings
     */
    public SubmissionResult(Resource resource, List<DriftWarning> warnings) {
        this.resource = resource;
        this.warnings = new ArrayList<>(warnings);
        this.status = warnings.isEmpty() ? "SUCCESS" : "SUCCESS_WITH_WARNINGS";
        
        if (warnings.isEmpty()) {
            this.message = "Resource submitted successfully.";
        } else {
            this.message = "Resource submitted with " + warnings.size() + " warning(s). Please review.";
        }
    }

    /**
     * Constructor for failed submission
     */
    public SubmissionResult(String errorMessage) {
        this.resource = null;
        this.warnings = new ArrayList<>();
        this.status = "FAILED";
        this.message = errorMessage;
    }

    // Getters
    public Resource getResource() {
        return resource;
    }

    public List<DriftWarning> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return !status.equals("FAILED");
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public int getWarningCount() {
        return warnings.size();
    }

    /**
     * Get critical warnings (severity 4-5)
     */
    public List<DriftWarning> getCriticalWarnings() {
        List<DriftWarning> critical = new ArrayList<>();
        for (DriftWarning w : warnings) {
            if (w.getSeverity() >= 4) {
                critical.add(w);
            }
        }
        return critical;
    }

    /**
     * Get warnings sorted by severity (descending)
     */
    public List<DriftWarning> getSortedWarnings() {
        List<DriftWarning> sorted = new ArrayList<>(warnings);
        sorted.sort((w1, w2) -> Integer.compare(w2.getSeverity(), w1.getSeverity()));
        return sorted;
    }

    /**
     * Format submission result for display
     */
    public String formatForDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== SUBMISSION RESULT ===\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Message: ").append(message).append("\n");

        if (resource != null) {
            sb.append("Resource ID: ").append(resource.getId()).append("\n");
            sb.append("Title: ").append(resource.getTitle()).append("\n");
        }

        if (!warnings.isEmpty()) {
            sb.append("\nDetected Issues:\n");
            List<DriftWarning> sorted = getSortedWarnings();
            for (int i = 0; i < sorted.size(); i++) {
                DriftWarning w = sorted.get(i);
                sb.append((i + 1)).append(". [").append(w.getType()).append("] (Severity: ");
                sb.append(w.getSeverity()).append("/5)\n");
                sb.append("   Message: ").append(w.getMessage()).append("\n");
                if (w.getSuggestion() != null && !w.getSuggestion().isEmpty()) {
                    sb.append("   Suggestion: ").append(w.getSuggestion()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "SubmissionResult{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", warningCount=" + warnings.size() +
                '}';
    }
}
