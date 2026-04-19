package com.spikeaware.team;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team member in the system with their role and contact information.
 */
public class TeamMember {
    private long id; // Unique identifier for each team member, public users are never assigned an ID as their information is never stored. As pointed out in feeback.
    private String name;
    private UserRole role;
    private String email;
    private boolean active;

    /**
     * Constructor for TeamMember.
     *
     * @param name   the name of the team member
     * @param role   the role of the team member
     * @param email  the email address
     * @param active whether the member is active
     */
    public TeamMember(String name, UserRole role, String email, boolean active) {
        this.id = System.identityHashCode(this); // Generate a unique ID for each member
        this.name = name;
        this.role = role;
        this.email = email;
        this.active = active;
    }

    /**
     * Default constructor for deserialization.
     */
    public TeamMember() {
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns a string representation of the team member.
     * Formats the team member's details in a tabular* format for display.
     * *(Needs to be revised for better formatting in CLI.)
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        values.add(name);
        values.add(role.getDisplayName());
        values.add(email);
        values.add(active ? "X" : " ");
        
        String[] headers = {"ID", "Name", "Role", "Email", "Active"};
        int[] lengths = new int[values.size()];
        
        // Calculate max length for each column
        for (int i = 0; i < values.size(); i++) {
            lengths[i] = Math.max(headers[i].length(), values.get(i).length());
        }
        
        // Print header
        System.out.printf("| %-" + lengths[0] + "s | %-" + lengths[1] + "s | %-" + lengths[2] + "s | %-" + lengths[3] + "s | %-" + lengths[4] + "s |%n", headers[0], headers[1], headers[2], headers[3], headers[4]);
        
        // Return formatted row
        return String.format("| %-" + lengths[0] + "d | %-" + lengths[1] + "s | %-" + lengths[2] + "s | %-" + lengths[3] + "s |  [%s]   |%n", id, name, role.getDisplayName(), email, active ? "X" : " ");
    }
}
