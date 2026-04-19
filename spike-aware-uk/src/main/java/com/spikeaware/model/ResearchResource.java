package com.spikeaware.model;

/**
 * ResearchResource class representing academic or scientific research materials.
 * Extends the Resource base class with specific fields for authors and publication year.
 * Omitted Keywords field and added year field to better capture the nature of research resources.
 */
public class ResearchResource extends Resource {
    private String authors;
    private int year;
    private String type;

    /**
     * Constructor for ResearchResource.
     *
     * @param title   the title of the research resource
     * @param url     the URL of the resource
     * @param authors the authors of the research
     * @param year    the publication year
     */
    public ResearchResource(String title, String url, String authors, int year) {
        super(title, url);
        this.type = "RESEARCH";
        this.authors = authors;
        this.year = year;
    }

    /**
     * Default constructor for deserialization.
     */
    public ResearchResource() {
        super();
    }

    // Getters and Setters
    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Returns the type of the research resource.
     *
     * @return the type of the resource
     */
    @Override
    public String getType() {
        return this.type.toString();
    }

    /**
     * Returns a string representation of the research resource specific details for display.
     *
     * @return the string representation
     */
    @Override
    public String getDetails() {
        return "Year: " + year + " | Authors: " + authors ;
    }

    /**
     * Returns a string representation of the research resource.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (ID: %d)\n", this.getType(),  this.getTitle(), this.getDetails(), this.getId());
    }
}
