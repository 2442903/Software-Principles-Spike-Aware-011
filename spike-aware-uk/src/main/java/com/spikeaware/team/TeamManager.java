package com.spikeaware.team;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spikeaware.config.ConfigurationManager;

/**
 * TeamManager class handles all team member file I/O operations and data persistence.
 * Uses Gson for JSON serialization and ConfigurationManager for environment-agnostic path handling.
 */
public class TeamManager {
    private final Path dataFile;
    private final List<TeamMember> teamMembers;
    private final Gson gson;


    /**
     * Constructor initializes the TeamManager and loads existing data from file.
     */
    public TeamManager() {
        this.dataFile = ConfigurationManager.getTeamDataFile();
        this.teamMembers = new ArrayList<>();
        this.gson = new GsonBuilder().create(); // Create a Gson instance for JSON operations
        loadFromFile();
    }

    /**
     * Loads team members from the JSON data file.
     */
    private void loadFromFile() {
        try {
            if (Files.exists(dataFile)) {
                String content = new String(Files.readAllBytes(dataFile)); // Read file content as string
                if (!content.isEmpty()) {
                    Type listType = com.google.gson.reflect.TypeToken
                            .getParameterized(List.class, TeamMember.class)
                            .getType(); // Define the type for List<TeamMember> to allow Gson to deserialize correctly
                    List<TeamMember> loadedTeamMembers = gson.fromJson(content, listType); // Deserialize JSON into List<TeamMember>
                    if (loadedTeamMembers != null) {
                        teamMembers.addAll(loadedTeamMembers);
                    }
                }
            }
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            System.err.println("Error loading data from file: " + e.getMessage());
            teamMembers.clear();
        }
    }

    /**
     * Saves all team members to the JSON data file.
     * Serializes the list of team members to JSON format and writes it to the file. Uses pretty printing for readability.
     */
    private void saveToFile() {
        try {
            String json = gson.toJson(teamMembers);
            Files.write(dataFile, json.getBytes());
        } catch (IOException e) {
            System.err.println("Error saving data to file: " + e.getMessage());
        }
    }  

    /**
     * Adds a new team member to the database and saves to file.
     *
     * @param teamMember the team member to add
     */
    public void addTeamMember(TeamMember teamMember) {
        teamMembers.add(teamMember);
        saveToFile();
    }

    /**
     * Retrieves all team members.
     *
     * @return a list of all team members
     */
    public List<TeamMember> getAllTeamMembers() {
        return new ArrayList<>(teamMembers);
    }

    /**
     * Retrieves a team member by their ID.
     *
     * @param id the team member ID
     * @return an Optional containing the team member, or empty if not found
     */
    public Optional<TeamMember> getTeamMemberById(long id) {
        return teamMembers.stream()
                .filter(t -> t.getId() == id)
                .findFirst();
    }

    /**
     * Edits an existing team member with new values and saves to file.
     *
     * @param id    the team member ID
     * @param name the new name
     * @param email the new email
     * @param role the new role
     */
    public void editTeamMember(long id, String name, String email, UserRole role) {
        teamMembers.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .ifPresent(t -> {
                    t.setName(name);
                    t.setEmail(email);
                    t.setRole(role);
                    saveToFile();
                });
    }

    /**
     * Removes (deletes) a team member from the database and saves to file.
     *
     * @param id the team member ID to remove
     */
    public void removeTeamMember(long id) {
        teamMembers.removeIf(t -> t.getId() == id);
        saveToFile();
    }

    /**
     * Marks a team member as inactive by setting their active flag and saves to file.
     *
     * @param id the team member ID
     */
    public void archiveTeamMember(long id) {
        teamMembers.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .ifPresent(t -> {
                    t.setActive(t.isActive() ? false : true); // Toggle active status
                    saveToFile();
                });
    }
}
