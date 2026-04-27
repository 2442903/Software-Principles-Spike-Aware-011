package com.spikeaware.service;

import com.spikeaware.model.DriftEvent;
import com.spikeaware.model.DriftReport;
import com.spikeaware.model.Resource;
import com.spikeaware.model.DriftWarning;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * DataDriftDetectionService detects data drift in resources.
 * Handles:
 * - Duplicate URL detection
 * - Invalid URL format detection
 * - Data inconsistency detection
 * - Missing required field detection
 * - Suspicious pattern detection
 */
public class DataDriftDetectionService {
    
    // URL validation regex
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$"
    );
    
    // Cache for tracking historical URLs per resource (simulated)
    private Map<Long, String> resourceUrlHistory;
    
    /**
     * Constructor for DataDriftDetectionService
     */
    public DataDriftDetectionService() {
        this.resourceUrlHistory = new HashMap<>();
    }

    /**
     * Perform comprehensive drift detection on a collection of resources
     */
    public DriftReport detectDrift(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            return new DriftReport();
        }

        DriftReport report = new DriftReport();
        
        // Detect duplicate URLs
        report.addAllDriftEvents(detectDuplicateUrls(resources));
        
        // Detect invalid URLs
        report.addAllDriftEvents(detectInvalidUrls(resources));
        
        // Detect missing data
        report.addAllDriftEvents(detectMissingData(resources));
        
        // Detect suspicious patterns
        report.addAllDriftEvents(detectSuspiciousPatterns(resources));
        
        // Generate summary
        report.generateSummary();
        
        return report;
    }

    /**
     * Detect duplicate URLs across resources
     */
    public List<DriftEvent> detectDuplicateUrls(List<Resource> resources) {
        List<DriftEvent> events = new ArrayList<>();
        
        // Group resources by URL
        Map<String, List<Resource>> urlGroups = resources.stream()
                .filter(r -> r.getUrl() != null && !r.getUrl().trim().isEmpty())
                .collect(Collectors.groupingBy(Resource::getUrl));
        
        // Check for duplicates
        for (Map.Entry<String, List<Resource>> entry : urlGroups.entrySet()) {
            if (entry.getValue().size() > 1) {
                // Found duplicates
                List<Resource> duplicates = entry.getValue();
                for (int i = 0; i < duplicates.size() - 1; i++) {
                    DriftEvent event = new DriftEvent(
                            duplicates.get(i).getId(),
                            duplicates.get(i + 1).getId(),
                            entry.getKey(),
                            4  // High severity
                    );
                    events.add(event);
                }
            }
        }
        
        return events;
    }

    /**
     * Detect invalid URL formats
     */
    public List<DriftEvent> detectInvalidUrls(List<Resource> resources) {
        List<DriftEvent> events = new ArrayList<>();
        
        for (Resource resource : resources) {
            if (resource.getUrl() != null && !resource.getUrl().trim().isEmpty()) {
                if (!isValidUrl(resource.getUrl())) {
                    DriftEvent event = new DriftEvent(
                            DriftEvent.DriftType.INVALID_URL,
                            resource.getId(),
                            "url",
                            null,
                            resource.getUrl(),
                            "Invalid URL format detected: " + resource.getUrl(),
                            3  // Medium severity
                    );
                    events.add(event);
                }
            }
        }
        
        return events;
    }

    /**
     * Detect missing required data fields
     */
    public List<DriftEvent> detectMissingData(List<Resource> resources) {
        List<DriftEvent> events = new ArrayList<>();
        
        for (Resource resource : resources) {
            // Check title
            if (resource.getTitle() == null || resource.getTitle().trim().isEmpty()) {
                DriftEvent event = new DriftEvent(
                        DriftEvent.DriftType.MISSING_DATA,
                        resource.getId(),
                        "title",
                        null,
                        null,
                        "Resource missing title",
                        4  // High severity
                );
                events.add(event);
            }
            
            // Check URL
            if (resource.getUrl() == null || resource.getUrl().trim().isEmpty()) {
                DriftEvent event = new DriftEvent(
                        DriftEvent.DriftType.MISSING_DATA,
                        resource.getId(),
                        "url",
                        null,
                        null,
                        "Resource missing URL",
                        5  // Critical severity
                );
                events.add(event);
            }
        }
        
        return events;
    }

    /**
     * Detect suspicious patterns in URLs and titles
     */
    public List<DriftEvent> detectSuspiciousPatterns(List<Resource> resources) {
        List<DriftEvent> events = new ArrayList<>();
        
        for (Resource resource : resources) {
            // Check for suspicious characters in title
            if (resource.getTitle() != null && containsSuspiciousCharacters(resource.getTitle())) {
                DriftEvent event = new DriftEvent(
                        DriftEvent.DriftType.SUSPICIOUS_PATTERN,
                        resource.getId(),
                        "title",
                        null,
                        resource.getTitle(),
                        "Suspicious characters detected in title",
                        2  // Low severity
                );
                events.add(event);
            }
            
            // Check for suspicious URL patterns
            if (resource.getUrl() != null && hasSuspiciousUrlPatterns(resource.getUrl())) {
                DriftEvent event = new DriftEvent(
                        DriftEvent.DriftType.SUSPICIOUS_PATTERN,
                        resource.getId(),
                        "url",
                        null,
                        resource.getUrl(),
                        "Suspicious URL patterns detected",
                        3  // Medium severity
                );
                events.add(event);
            }
        }
        
        return events;
    }

    /**
     * Track URL changes for a resource (for historical drift detection)
     */
    public DriftEvent checkUrlChange(Resource resource, String previousUrl) {
        if (previousUrl == null || previousUrl.equals(resource.getUrl())) {
            return null;
        }
        
        DriftEvent event = new DriftEvent(
                DriftEvent.DriftType.URL_CHANGE,
                resource.getId(),
                "url",
                previousUrl,
                resource.getUrl(),
                "URL was changed from: " + previousUrl + " to: " + resource.getUrl(),
                3  // Medium severity
        );
        
        return event;
    }

    /**
     * Validate URL format
     */
    private boolean isValidUrl(String url) {
        try {
            // Check regex pattern first
            if (!URL_PATTERN.matcher(url.toLowerCase()).matches()) {
                // Try java.net.URL as fallback
                new URL(url);
            }
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Check for suspicious characters
     */
    private boolean containsSuspiciousCharacters(String text) {
        if (text == null) return false;
        
        // Check for SQL injection patterns
        if (text.toLowerCase().matches(".*('|(\\-\\-)|(;)|(\\|\\|)|(\\*)|(exec)|(drop)).*")) {
            return true;
        }
        
        // Check for script injection patterns
        if (text.toLowerCase().matches(".*(<script>|</script>|javascript:|onerror=).*")) {
            return true;
        }
        
        return false;
    }

    /**
     * Check for suspicious URL patterns
     */
    private boolean hasSuspiciousUrlPatterns(String url) {
        if (url == null) return false;
        
        String lowerUrl = url.toLowerCase();
        
        // Check for suspicious domains
        if (lowerUrl.contains("localhost") || lowerUrl.contains("127.0.0.1")) {
            return true;
        }
        
        // Check for known phishing patterns
        if (lowerUrl.contains("bit.ly") || lowerUrl.contains("tinyurl")) {
            return true;
        }
        
        // Check for multiple redirects
        if (lowerUrl.split("redirect").length > 2) {
            return true;
        }
        
        return false;
    }

    /**
     * Get duplicate URL groups
     */
    public Map<String, List<Resource>> getDuplicateUrlGroups(List<Resource> resources) {
        return resources.stream()
                .filter(r -> r.getUrl() != null && !r.getUrl().trim().isEmpty())
                .collect(Collectors.groupingBy(Resource::getUrl))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get resources with invalid URLs
     */
    public List<Resource> getResourcesWithInvalidUrls(List<Resource> resources) {
        return resources.stream()
                .filter(r -> r.getUrl() != null && !r.getUrl().trim().isEmpty())
                .filter(r -> !isValidUrl(r.getUrl()))
                .collect(Collectors.toList());
    }

    /**
     * Get resources with suspicious patterns
     */
    public List<Resource> getResourcesWithSuspiciousPatterns(List<Resource> resources) {
        return resources.stream()
                .filter(r -> {
                    boolean titleSuspicious = r.getTitle() != null && containsSuspiciousCharacters(r.getTitle());
                    boolean urlSuspicious = r.getUrl() != null && hasSuspiciousUrlPatterns(r.getUrl());
                    return titleSuspicious || urlSuspicious;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get a detailed drift analysis for debugging
     */
    public String getDriftAnalysisSummary(DriftReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== DATA DRIFT ANALYSIS SUMMARY ===\n");
        sb.append("Report ID: ").append(report.getReportId()).append("\n");
        sb.append("Generated: ").append(report.getGeneratedAt()).append("\n");
        sb.append("Total Issues: ").append(report.getTotalDriftEvents()).append("\n\n");
        
        sb.append("Severity Breakdown:\n");
        sb.append("  Critical: ").append(report.getCriticalCount()).append("\n");
        sb.append("  High: ").append(report.getHighCount()).append("\n");
        sb.append("  Medium: ").append(report.getMediumCount()).append("\n");
        sb.append("  Low: ").append(report.getLowCount()).append("\n\n");
        
        sb.append("Issues by Type:\n");
        Map<DriftEvent.DriftType, Long> typeCount = report.getDriftEvents().stream()
                .collect(Collectors.groupingBy(DriftEvent::getType, Collectors.counting()));
        
        for (Map.Entry<DriftEvent.DriftType, Long> entry : typeCount.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        sb.append("\n=== DETAILED ISSUES ===\n");
        List<DriftEvent> sorted = report.getSortedEvents();
        for (DriftEvent event : sorted) {
            sb.append("\n[").append(event.getType()).append("] (Severity: ");
            sb.append(event.getSeverity()).append("/5)\n");
            sb.append("  Resource ID: ").append(event.getPrimaryResourceId()).append("\n");
            sb.append("  Description: ").append(event.getDescription()).append("\n");
            if (event.getFieldName() != null) {
                sb.append("  Field: ").append(event.getFieldName()).append("\n");
            }
            if (event.getNewValue() != null) {
                sb.append("  Value: ").append(event.getNewValue()).append("\n");
            }
        }
        
        return sb.toString();
    }

    // ==================== SUBMISSION-TIME DRIFT DETECTION ====================

    /**
     * Check for drift issues when a resource is submitted.
     * Returns warnings but does NOT prevent submission.
     */
    public List<DriftWarning> checkSubmissionDrift(String title, String url, List<Resource> existingResources) {
        List<DriftWarning> warnings = new ArrayList<>();

        if (url == null || url.trim().isEmpty()) {
            return warnings;
        }

        // Check 1: URL already exists (duplicate)
        boolean urlExists = existingResources.stream()
                .anyMatch(r -> url.equalsIgnoreCase(r.getUrl()));
        
        if (urlExists) {
            DriftWarning warning = new DriftWarning(
                    -1,  // Resource ID not assigned yet
                    DriftWarning.WarningType.DUPLICATE_URL,
                    "This URL already exists in the system",
                    "Consider using a different URL or check if this is a duplicate submission",
                    5  // Critical severity
            );
            warnings.add(warning);
        }

        // Check 2: URL format is invalid
        if (!isValidUrl(url)) {
            DriftWarning warning = new DriftWarning(
                    -1,
                    DriftWarning.WarningType.INVALID_URL,
                    "URL format appears to be invalid: " + url,
                    "Please verify the URL is properly formatted (e.g., https://example.com)",
                    4  // High severity
            );
            warnings.add(warning);
        }

        // Check 3: Suspicious URL patterns
        if (hasSuspiciousUrlPatterns(url)) {
            DriftWarning warning = new DriftWarning(
                    -1,
                    DriftWarning.WarningType.SUSPICIOUS_URL,
                    "URL contains suspicious patterns that may indicate phishing or malware",
                    "Consider verifying this URL is from a trusted source",
                    4  // High severity
            );
            warnings.add(warning);
        }

        // Check 4: Suspicious title patterns
        if (title != null && containsSuspiciousCharacters(title)) {
            DriftWarning warning = new DriftWarning(
                    -1,
                    DriftWarning.WarningType.SUSPICIOUS_TITLE,
                    "Title contains suspicious patterns (SQL injection, script injection, etc.)",
                    "Please review the title for malicious content",
                    3  // Medium severity
            );
            warnings.add(warning);
        }

        // Check 5: Similar URLs (near-duplicates)
        List<Resource> similarUrls = findSimilarUrls(url, existingResources);
        if (!similarUrls.isEmpty()) {
            StringBuilder urlList = new StringBuilder();
            for (Resource r : similarUrls) {
                if (urlList.length() > 0) urlList.append(", ");
                urlList.append(r.getUrl());
            }
            
            DriftWarning warning = new DriftWarning(
                    -1,
                    DriftWarning.WarningType.NEAR_DUPLICATE_URL,
                    "Similar URLs already exist: " + urlList,
                    "Verify this is not a duplicate with slight variations",
                    2  // Low severity
            );
            warnings.add(warning);
        }

        return warnings;
    }

    /**
     * Find URLs that are similar to the given URL (near-duplicates)
     */
    private List<Resource> findSimilarUrls(String url, List<Resource> resources) {
        List<Resource> similar = new ArrayList<>();
        
        if (url == null || resources == null) {
            return similar;
        }

        String normalizedUrl = normalizeUrl(url);
        
        for (Resource r : resources) {
            if (r.getUrl() != null) {
                String existingNormalized = normalizeUrl(r.getUrl());
                
                // Simple similarity check: same domain
                if (extractDomain(normalizedUrl).equals(extractDomain(existingNormalized))) {
                    similar.add(r);
                }
            }
        }
        
        return similar;
    }

    /**
     * Normalize URL by removing protocol and trailing slashes
     */
    private String normalizeUrl(String url) {
        if (url == null) return "";
        
        url = url.toLowerCase().trim();
        url = url.replaceAll("^https?://", "");
        url = url.replaceAll("/$", "");
        
        return url;
    }

    /**
     * Extract domain from normalized URL
     */
    private String extractDomain(String normalizedUrl) {
        if (normalizedUrl == null || normalizedUrl.isEmpty()) return "";
        
        // Remove path and query
        String[] parts = normalizedUrl.split("[/?#]");
        return parts[0];
    }

    /**
     * Validate a specific URL for submission
     */
    public DriftWarning validateSubmittedUrl(String url, List<Resource> existingResources) {
        if (url == null || url.trim().isEmpty()) {
            return new DriftWarning(
                    -1,
                    DriftWarning.WarningType.INVALID_URL,
                    "URL cannot be empty",
                    "Please provide a valid URL",
                    5
            );
        }

        // Check if URL already exists
        boolean exists = existingResources.stream()
                .anyMatch(r -> url.equalsIgnoreCase(r.getUrl()));
        
        if (exists) {
            return new DriftWarning(
                    -1,
                    DriftWarning.WarningType.DUPLICATE_URL,
                    "This URL already exists in the system",
                    "Consider using a different URL or check if this is a duplicate submission",
                    5
            );
        }

        // Check URL format
        if (!isValidUrl(url)) {
            return new DriftWarning(
                    -1,
                    DriftWarning.WarningType.INVALID_URL,
                    "URL format is invalid",
                    "Please verify the URL is properly formatted",
                    4
            );
        }

        return null;  // No warnings
    }
}
