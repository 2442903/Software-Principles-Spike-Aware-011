package com.spikeaware.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Comprehensive report of all data drift events detected in the resource collection.
 */
public class DriftReport {
    private String reportId;
    private LocalDateTime generatedAt;
    private List<DriftEvent> driftEvents;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;
    private String summary;

    /**
     * Constructor for DriftReport
     */
    public DriftReport() {
        this.reportId = "DRIFT_" + System.currentTimeMillis();
        this.generatedAt = LocalDateTime.now();
        this.driftEvents = new ArrayList<>();
        this.criticalCount = 0;
        this.highCount = 0;
        this.mediumCount = 0;
        this.lowCount = 0;
        this.summary = "";
    }

    /**
     * Add a drift event to the report
     */
    public void addDriftEvent(DriftEvent event) {
        if (event != null) {
            driftEvents.add(event);
            categorizeEvent(event.getSeverity());
        }
    }

    /**
     * Add multiple drift events
     */
    public void addAllDriftEvents(List<DriftEvent> events) {
        if (events != null) {
            for (DriftEvent event : events) {
                addDriftEvent(event);
            }
        }
    }

    /**
     * Categorize event by severity
     */
    private void categorizeEvent(int severity) {
        switch (severity) {
            case 5:
                criticalCount++;
                break;
            case 4:
                highCount++;
                break;
            case 3:
                mediumCount++;
                break;
            default:
                lowCount++;
        }
    }

    /**
     * Generate a summary of the drift report
     */
    public void generateSummary() {
        int totalEvents = driftEvents.size();
        StringBuilder sb = new StringBuilder();
        sb.append("Drift Report Summary:\n");
        sb.append("- Total Events: ").append(totalEvents).append("\n");
        sb.append("- Critical: ").append(criticalCount).append("\n");
        sb.append("- High: ").append(highCount).append("\n");
        sb.append("- Medium: ").append(mediumCount).append("\n");
        sb.append("- Low: ").append(lowCount).append("\n");
        
        if (totalEvents == 0) {
            sb.append("\nNo data drift detected.");
        } else {
            sb.append("\nRecommendations: Review and resolve all critical and high-severity events.");
        }
        
        this.summary = sb.toString();
    }

    /**
     * Get drift events sorted by severity (descending)
     */
    public List<DriftEvent> getSortedEvents() {
        List<DriftEvent> sorted = new ArrayList<>(driftEvents);
        sorted.sort((e1, e2) -> Integer.compare(e2.getSeverity(), e1.getSeverity()));
        return sorted;
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public List<DriftEvent> getDriftEvents() {
        return Collections.unmodifiableList(driftEvents);
    }

    public int getCriticalCount() {
        return criticalCount;
    }

    public int getHighCount() {
        return highCount;
    }

    public int getMediumCount() {
        return mediumCount;
    }

    public int getLowCount() {
        return lowCount;
    }

    public String getSummary() {
        return summary;
    }

    public int getTotalDriftEvents() {
        return driftEvents.size();
    }

    @Override
    public String toString() {
        generateSummary();
        return "DriftReport{" +
                "reportId='" + reportId + '\'' +
                ", generatedAt=" + generatedAt +
                ", totalEvents=" + driftEvents.size() +
                ", summary='" + summary + '\'' +
                '}';
    }
}
