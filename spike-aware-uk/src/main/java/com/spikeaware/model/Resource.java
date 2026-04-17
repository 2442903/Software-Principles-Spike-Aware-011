package com.spikeaware.model;

import java.util.Objects;

/**
 * Abstract base class for resources in the Spike Aware UK Resource Aggregator.
 * Contains common fields and behaviors for all resource types.
 */
public abstract class Resource {
    private long id;
    private String title;
    private String url;
    private ResourceStatus status;  // Pending, Published, Rejected, Archived
    private long viewCount;
    private long flagCount;
    private String createdBy;  // Track who submitted the resource

    /**
     * Constructor for Resource with automatic ID generation via identity hash.
     *
     * @param title the title of the resource
     * @param url   the URL of the resource
     */
    public Resource(String title, String url) {
        this.id = System.identityHashCode(this); // Generate a unique ID for each resource
        this.title = title;
        this.url = url;
        this.status = ResourceStatus.PENDING;  // Initial state
        this.viewCount = 0;
        this.flagCount = 0;
        this.createdBy = "Unknown";
    }

    /**
     * Default constructor for deserialization.
     */
    public Resource() {
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getFlagCount() {
        return flagCount;
    }

    public void setFlagCount(long flagCount) {
        this.flagCount = flagCount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Increments the view count by 1.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Increments the flag count by 1.
     */
    public void incrementFlagCount() {
        this.flagCount++;
    }

    /**
     * Abstract method to get the type of resource (e.g., "RESEARCH", "PUBLIC").
     * Must be implemented by subclasses.
     *
     * @return the type of the resource
     */
    public abstract String getType();

    /**
     * Abstract method to get resource-specific details for display.
     * Must be implemented by subclasses.
     *
     * @return formatted string of resource-specific details
     */
    public abstract String getDetails();

    /**
     * Displays the resource information in a formatted block for CLI output.
     */
    public void display() {
        System.out.println("---------------------------------------------------");
        System.out.println("ID:        " + id);
        System.out.println("Type:      " + getType());
        System.out.println("Title:     " + title);
        System.out.println("URL:       " + url);
        System.out.println("Status:    " + status);
        System.out.println("Views:     " + viewCount + " | Flags: " + flagCount);
        System.out.println("Details:   " + getDetails());
        System.out.println("---------------------------------------------------");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;
        Resource resource = (Resource) o;
        return id == resource.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (ID: %d)\n", this.getType(), this.getTitle(), this.getId());
    }
}
