package com.spikeaware.db;

import com.google.gson.*;
import com.spikeaware.config.ConfigurationManager;
import com.spikeaware.model.Resource;
import com.spikeaware.model.ResourceStatus;
import com.spikeaware.model.ResearchResource;
import com.spikeaware.model.PublicResource;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DatabaseManager class handles all file I/O operations and data persistence.
 * Uses Gson for JSON serialization with custom deserialization for polymorphic types.
 * Uses ConfigurationManager for environment-agnostic file path handling.
 */
public class DatabaseManager {
    private final Path dataFile;
    private final List<Resource> resources;
    private final Gson gson;

    /**
     * Custom deserializer for Resource polymorphism.
     */
    private static class ResourceDeserializer implements JsonDeserializer<Resource> {
        @Override
        public Resource deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();

            switch (type) {
                case "RESEARCH":
                    return context.deserialize(json, ResearchResource.class);
                case "PUBLIC":
                    return context.deserialize(json, PublicResource.class);
                default:
                    throw new JsonParseException("Unknown resource type: " + type);
            }
        }
    }

    /**
     * Constructor initializes the DatabaseManager and loads existing data from file.
     */
    public DatabaseManager() {
        // Initialize data file path from configuration
        this.dataFile = ConfigurationManager.getResourcesDataFile();

        // Configure Gson with custom deserializer for polymorphic serialization
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Resource.class, new ResourceDeserializer())
                .setPrettyPrinting()
                .create();

        this.resources = new ArrayList<>();
        loadFromFile();
    }

    /**
     * Loads resources from the JSON data file.
     */
    private void loadFromFile() {
        try {
            if (Files.exists(dataFile)) {
                String content = new String(Files.readAllBytes(dataFile));
                if (!content.isEmpty()) {
                    Type listType = com.google.gson.reflect.TypeToken
                            .getParameterized(List.class, Resource.class)
                            .getType();
                    List<Resource> loadedResources = gson.fromJson(content, listType);
                    if (loadedResources != null) {
                        resources.addAll(loadedResources);
                    }
                }
            }
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            System.err.println("Error loading data from file: " + e.getMessage());
            resources.clear();
        }
    }

    /**
     * Saves all resources to the JSON data file.
     */
    private void saveToFile() {
        try {
            String json = gson.toJson(resources);
            Files.write(dataFile, json.getBytes());
        } catch (IOException e) {
            System.err.println("Error saving data to file: " + e.getMessage());
        }
    }

    /**
     * Adds a new resource to the database and saves to file.
     *
     * @param resource the resource to add
     */
    public void addResource(Resource resource) {
        resources.add(resource);
        saveToFile();
    }

    /**
     * Updates an existing resource and saves to file.
     *
     * @param resource the resource to update
     */
    public void updateResource(Resource resource) {
        resources.stream()
                .filter(r -> r.getId() == resource.getId())
                .findFirst()
                .ifPresent(r -> {
                    r.setTitle(resource.getTitle());
                    r.setUrl(resource.getUrl());
                    r.setStatus(resource.getStatus());
                    r.setViewCount(resource.getViewCount());
                    r.setFlagCount(resource.getFlagCount());
                    saveToFile();
                });
    }

    /**
     * Retrieves all resources.
     *
     * @return a list of all resources
     */
    public List<Resource> getAllResources() {
        return new ArrayList<>(resources);
    }

    /**
     * Retrieves all published resources (status = "Published").
     *
     * @return a list of published resources
     */
    public List<Resource> getPublishedResources() {
        return resources.stream()
                .filter(r -> ResourceStatus.APPROVED.equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all pending resources (status = "Pending").
     *
     * @return a list of pending resources
     */
    public List<Resource> getPendingResources() {
        return resources.stream()
                .filter(r -> ResourceStatus.PENDING.equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Searches for resources by keyword. For public users, only searches published resources.
     * For moderators, searches all resources.
     *
     * @param keyword the search keyword
     * @param isModeratorMode whether the search is in moderator mode
     * @return a list of matching resources
     */
    public List<Resource> searchResources(String keyword, boolean isModeratorMode) {
        String lowerKeyword = keyword.toLowerCase();
        List<Resource> searchPool = isModeratorMode ? resources : getPublishedResources();

        return searchPool.stream()
                .filter(r -> {
                    String searchText = (r.getTitle() + " " + r.getId() + " " + r.getDetails()).toLowerCase();
                    return searchText.contains(lowerKeyword);
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a resource by its ID.
     *
     * @param id the resource ID
     * @return an Optional containing the resource, or empty if not found
     */
    public Optional<Resource> getResourceById(long id) {
        return resources.stream()
                .filter(r -> r.getId() == id)
                .findFirst();
    }

    /**
     * Increments the view count for a resource and saves to file.
     *
     * @param id the resource ID
     */
    public void incrementViewCount(long id) {
        resources.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .ifPresent(r -> {
                    r.incrementViewCount();
                    saveToFile();
                });
    }

    /**
     * Increments the flag count for a resource and saves to file.
     *
     * @param id the resource ID
     */
    public void incrementFlagCount(long id) {
        resources.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .ifPresent(r -> {
                    r.incrementFlagCount();
                    saveToFile();
                });
    }

    /**
     * Approves a resource by changing its status to "Published" and saves to file.
     *
     * @param id the resource ID
     */
    public void approveResource(long id) {
        resources.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .ifPresent(r -> {
                    r.setStatus(ResourceStatus.APPROVED);
                    saveToFile();
                });
    }

    /**
     * Rejects a resource by changing its status to "Rejected" and saves to file.
     *
     * @param id the resource ID
     */
    public void rejectResource(long id) {
        resources.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .ifPresent(r -> {
                    r.setStatus(ResourceStatus.REJECTED);
                    saveToFile();
                });
    }

    /**
     * Clears all data (for testing purposes).
     */
    public void clear() {
        resources.clear();
        saveToFile();
    }

    /**
     * Edits an existing resource with new values and saves to file.
     *
     * @param id    the resource ID
     * @param title the new title
     * @param url   the new URL
     */
    public void editResource(long id, String title, String url) {
        resources.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .ifPresent(r -> {
                    r.setTitle(title);
                    r.setUrl(url);
                    saveToFile();
                });
    }

    /**
     * Removes (deletes) a resource from the database and saves to file.
     *
     * @param id the resource ID to remove
     */
    public void removeResource(long id) {
        resources.removeIf(r -> r.getId() == id);
        saveToFile();
    }

    /**
     * Archives a resource by setting its archived flag and saves to file.
     *
     * @param id the resource ID
     */
    public void archiveResource(long id) {
        resources.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .ifPresent(r -> {
                    r.setStatus(ResourceStatus.ARCHIVED);
                    saveToFile();
                });
    }

    /**
     * Retrieves all archived resources.
     *
     * @return a list of archived resources
     */
    public List<Resource> getArchivedResources() {
        return resources.stream()
                .filter(r -> ResourceStatus.ARCHIVED.equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all flagged resources (those with flag count > 0).
     *
     * @return a list of flagged resources
     */
    public List<Resource> getFlaggedResources() {
        return resources.stream()
                .filter(r -> r.getFlagCount() > 0)
                .sorted((r1, r2) -> Long.compare(r2.getFlagCount(), r1.getFlagCount()))
                .collect(Collectors.toList());
    }

    /**
     * Generates system analytics about resources.
     *
     * @return a formatted string with analytics
     */
    public String getAnalytics() {
        long totalResources = resources.size();
        long published = resources.stream().filter(r -> ResourceStatus.APPROVED.equals(r.getStatus())).count();
        long pending = resources.stream().filter(r -> ResourceStatus.PENDING.equals(r.getStatus())).count();
        long rejected = resources.stream().filter(r -> ResourceStatus.REJECTED.equals(r.getStatus())).count();
        long archived = resources.stream().filter(r -> ResourceStatus.ARCHIVED.equals(r.getStatus())).count();
        long totalViews = resources.stream().mapToLong(Resource::getViewCount).sum();
        long totalFlags = resources.stream().mapToLong(Resource::getFlagCount).sum();

        return String.format(
                "/---------------------------------------------------\\\n" +
                "|                SYSTEM ANALYTICS                   |\n" +
                "\\---------------------------------------------------/\n" +
                "Total Resources:     %d\n" +
                "  - Published:       %d\n" +
                "  - Pending:         %d\n" +
                "  - Rejected:        %d\n" +
                "  - Archived:        %d\n" +
                "Total Views:         %d\n" +
                "Total Flags:         %d\n" +
                "---------------------------------------------------",
                totalResources, published, pending, rejected, archived, totalViews, totalFlags
        );
    }

    /**
     * Adds a new resource with explicit status control.
     * Used by moderators/admins to submit resources directly to "Published".
     *
     * @param resource the resource to add
     * @param publishDirectly whether to publish directly (true for moderators/admins, false for public)
     * @param creator the user who created the resource
     */
    public void addResource(Resource resource, boolean publishDirectly, String creator) {
        resource.setCreatedBy(creator);
        if (publishDirectly) {
            resource.setStatus(ResourceStatus.APPROVED);
        }
        resources.add(resource);
        saveToFile();
    }
}

