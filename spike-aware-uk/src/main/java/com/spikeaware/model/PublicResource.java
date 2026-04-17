package com.spikeaware.model;

/**
 * PublicResource class representing broader educational materials.
 * Extends the Resource base class with specific fields for organization and target audience.
 */
public class PublicResource extends Resource {
    private String org;      // Organization
    private String audience; // Target audience
    private String type;

    /**
     * Constructor for PublicResource.
     *
     * @param title    the title of the public resource
     * @param url      the URL of the resource
     * @param org      the organization publishing the resource
     * @param audience the target audience for the resource
     */
    public PublicResource(String title, String url, String org, String audience) {
        super(title, url);
        this.type = "PUBLIC";
        this.org = org;
        this.audience = audience;
    }

    /**
     * Default constructor for deserialization.
     */
    public PublicResource() {
        super();
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    @Override
    public String getType() {
        return type.toString();
    }

    @Override
    public String getDetails() {
        return "Organization: " + org + " | Audience: " + audience;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (ID: %d)\n", this.getType(),  this.getTitle(), this.getDetails(), this.getId());
    }
}
