package com.spikeaware.service;

import com.spikeaware.db.DatabaseManager;
import com.spikeaware.model.Resource;
import com.spikeaware.model.ResourceStatus;
import com.spikeaware.model.ResearchResource;
import com.spikeaware.model.PublicResource;

import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * ResourceService handles all resource-related business logic.
 * This service encapsulates the operations on resources and coordinates with DatabaseManager.
 */
public class ResourceService {
    private final DatabaseManager database;
    private final AuthenticationService authService;

    /**
     * Constructor for ResourceService.
     *
     * @param database the DatabaseManager instance
     * @param authService the AuthenticationService instance
     */
    public ResourceService(DatabaseManager database, AuthenticationService authService) {
        this.database = database;
        this.authService = authService;
    }

    /**
     * Get all published resources (for public users).
     *
     * @return list of published resources
     */
    public List<Resource> getPublishedResources() {
        return database.getPublishedResources();
    }

    /**
     * Get all resources (for moderators/admins).
     *
     * @return list of all resources
     */
    public List<Resource> getAllResources() {
        return database.getAllResources();
    }

    /**
     * Search resources based on user's access level.
     *
     * @param keyword the search keyword
     * @return list of matching resources
     */
    public List<Resource> searchResources(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }
        return database.searchResources(keyword, authService.isModeratorMode());
    }

    /**
     * Get a resource by ID with access control.
     *
     * @param id the resource ID
     * @return Optional containing the resource if accessible
     */
    public Optional<Resource> getResource(long id) {
        Optional<Resource> resource = database.getResourceById(id);
        
        if (resource.isPresent()) {
            Resource r = resource.get();
            // Check access: public users can only see approved resources
            if (!authService.isModeratorMode() && !ResourceStatus.APPROVED.equals(r.getStatus())) {
                return Optional.empty();
            }
            // Increment view count for approved resources
            if (ResourceStatus.APPROVED.equals(r.getStatus())) {
                database.incrementViewCount(id);
            }
        }
        
        return resource;
    }

    /**
     * Add a research resource.
     *
     * @param title the resource title
     * @param url the resource URL
     * @param authors the research authors
     * @param year the publication year
     * @return the created resource
     */
    public ResearchResource addResearchResource(String title, String url, String authors, int year) {
        validateResourceInput(title, url);
        if (authors == null || authors.isEmpty()) {
            throw new IllegalArgumentException("Authors cannot be empty");
        }
        if (year < 1900 || year > Year.now().getValue()) {
            throw new IllegalArgumentException("Year must be between 1900 and " + Year.now().getValue());
        }

        ResearchResource resource = new ResearchResource(title, url, authors, year);
        boolean publishDirectly = authService.isModeratorMode();
        String creator = authService.getCurrentRole().getDisplayName();
        database.addResource(resource, publishDirectly, creator);
        
        return resource;
    }

    /**
     * Add a public resource.
     *
     * @param title the resource title
     * @param url the resource URL
     * @param organization the organization providing the resource
     * @param audience the target audience
     * @return the created resource
     */
    public PublicResource addPublicResource(String title, String url, String organization, String audience) {
        validateResourceInput(title, url);
        if (organization == null || organization.isEmpty()) {
            throw new IllegalArgumentException("Organization cannot be empty");
        }
        if (audience == null || audience.isEmpty()) {
            throw new IllegalArgumentException("Target audience cannot be empty");
        }

        PublicResource resource = new PublicResource(title, url, organization, audience);
        boolean publishDirectly = authService.isModeratorMode();
        String creator = authService.getCurrentRole().getDisplayName();
        database.addResource(resource, publishDirectly, creator);
        
        return resource;
    }

    /**
     * Flag a resource for moderation review.
     *
     * @param id the resource ID to flag
     * @throws IllegalArgumentException if resource doesn't exist or is not accessible
     */
    public void flagResource(long id) {
        Optional<Resource> resource = database.getResourceById(id);
        
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Resource not found");
        }
        
        Resource r = resource.get();
        if (!authService.isModeratorMode() && !ResourceStatus.APPROVED.equals(r.getStatus())) {
            throw new IllegalArgumentException("Only published resources can be flagged");
        }

        database.incrementFlagCount(id);
    }

    /**
     * Approve a resource (internal: requires moderator)
     *
     * @param id the resource ID
     */
    public void approveResource(long id) {
        if (!authService.isModeratorMode()) {
            throw new SecurityException("Only moderators can approve resources");
        }
        database.approveResource(id);
    }

    /**
     * Reject a resource (internal: requires moderator).
     *
     * @param id the resource ID
     */
    public void rejectResource(long id) {
        if (!authService.isModeratorMode()) {
            throw new SecurityException("Only moderators can reject resources");
        }
        database.rejectResource(id);
    }

    /**
     * Edit a resource's title and URL.
     *
     * @param id the resource ID
     * @param title the new title (null to keep current)
     * @param url the new URL (null to keep current)
     */
    public void editResource(long id, String title, String url) {
        if (!authService.isModeratorMode()) {
            throw new SecurityException("Only moderators can edit resources");
        }

        Optional<Resource> resource = database.getResourceById(id);
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Resource not found");
        }

        Resource r = resource.get();
        if (title != null && !title.isEmpty()) {
            r.setTitle(title);
        }
        if (url != null && !url.isEmpty()) {
            r.setUrl(url);
        }

        database.updateResource(r);
    }

    /**
     * Remove a resource permanently.
     *
     * @param id the resource ID
     */
    public void removeResource(long id) {
        if (!authService.isAdministratorMode()) {
            throw new SecurityException("Only administrators can remove resources");
        }

        Optional<Resource> resource = database.getResourceById(id);
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Resource not found");
        }

        database.removeResource(id);
    }

    /**
     * Archive a resource.
     *
     * @param id the resource ID
     */
    public void archiveResource(long id) {
        if (!authService.isModeratorMode()) {
            throw new SecurityException("Only moderators can archive resources");
        }

        Optional<Resource> resource = database.getResourceById(id);
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Resource not found");
        }

        database.archiveResource(id);
    }

    /**
     * Unarchive a resource (sets to pending for moderator review).
     *
     * @param id the resource ID
     */
    public void unarchiveResource(long id) {
        if (!authService.isModeratorMode()) {
            throw new SecurityException("Only moderators can unarchive resources");
        }

        Optional<Resource> resource = database.getResourceById(id);
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Resource not found");
        }

        database.updateResource(resource.get());
    }

    /**
     * Get all pending resources (requires moderator).
     *
     * @return list of pending resources
     */
    public List<Resource> getPendingResources() {
        if (!authService.isModeratorMode()) {
            throw new SecurityException("Only moderators can view pending resources");
        }
        return database.getPendingResources();
    }

    /**
     * Get all flagged resources sorted by flag count (requires moderator).
     *
     * @return list of flagged resources
     */
    public List<Resource> getFlaggedResources() {
        if (!authService.isModeratorMode()) {
            throw new SecurityException("Only moderators can view flagged resources");
        }
        return database.getFlaggedResources();
    }

    /**
     * Get system analytics (requires moderator).
     *
     * @return analytics report string
     */
    public String getAnalytics() {
        if (!authService.isModeratorMode()) {
            throw new SecurityException("Only moderators can view analytics");
        }
        return database.getAnalytics();
    }

    /**
     * Validate common resource input.
     *
     * @param title the title to validate
     * @param url the URL to validate
     * @throws IllegalArgumentException if inputs are invalid
     */
    private void validateResourceInput(String title, String url) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        if (title.length() > 500) {
            throw new IllegalArgumentException("Title is too long (max 500 characters)");
        }
        if (url.length() > 2048) {
            throw new IllegalArgumentException("URL is too long (max 2048 characters)");
        }
    }
}
