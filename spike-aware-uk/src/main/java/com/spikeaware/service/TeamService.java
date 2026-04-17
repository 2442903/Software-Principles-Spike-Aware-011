package com.spikeaware.service;

import com.spikeaware.team.TeamManager;
import com.spikeaware.team.TeamMember;
import com.spikeaware.team.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * TeamService handles all team management business logic.
 * Requires administrator privileges for most operations.
 */
public class TeamService {
    private final TeamManager teamManager;
    private final AuthenticationService authService;

    /**
     * Constructor for TeamService.
     *
     * @param teamManager the TeamManager instance
     * @param authService the AuthenticationService instance
     */
    public TeamService(TeamManager teamManager, AuthenticationService authService) {
        this.teamManager = teamManager;
        this.authService = authService;
    }

    /**
     * Get all team members (requires administrator).
     *
     * @return list of all team members
     */
    public List<TeamMember> getAllTeamMembers() {
        if (!authService.isAdministratorMode()) {
            throw new SecurityException("Only administrators can view team members");
        }
        return teamManager.getAllTeamMembers();
    }

    /**
     * Get a team member by ID (requires administrator).
     *
     * @param id the team member ID
     * @return Optional containing the team member if found
     */
    public Optional<TeamMember> getTeamMember(long id) {
        if (!authService.isAdministratorMode()) {
            throw new SecurityException("Only administrators can view team members");
        }
        return teamManager.getTeamMemberById(id);
    }

    /**
     * Add a new team member (requires administrator).
     *
     * @param name the team member's name
     * @param email the team member's email
     * @param role the team member's role
     * @return the created team member
     */
    public TeamMember addTeamMember(String name, String email, UserRole role) {
        if (!authService.isAdministratorMode()) {
            throw new SecurityException("Only administrators can add team members");
        }

        validateTeamMemberInput(name, email, role);

        TeamMember member = new TeamMember(name, role, email, true);
        teamManager.addTeamMember(member);
        
        return member;
    }

    /**
     * Edit a team member (requires administrator).
     *
     * @param id the team member ID
     * @param name the new name (null to keep current)
     * @param email the new email (null to keep current)
     * @param role the new role (null to keep current)
     */
    public void editTeamMember(long id, String name, String email, UserRole role) {
        if (!authService.isAdministratorMode()) {
            throw new SecurityException("Only administrators can edit team members");
        }

        Optional<TeamMember> member = teamManager.getTeamMemberById(id);
        if (member.isEmpty()) {
            throw new IllegalArgumentException("Team member not found");
        }

        TeamMember m = member.get();
        
        if (name != null && !name.isEmpty()) {
            validateMemberName(name);
            m.setName(name);
        }
        
        if (email != null && !email.isEmpty()) {
            validateMemberEmail(email);
            m.setEmail(email);
        }
        
        if (role != null) {
            m.setRole(role);
        }

        teamManager.editTeamMember(id, m.getName(), m.getEmail(), m.getRole());
    }

    /**
     * Remove a team member (requires administrator).
     *
     * @param id the team member ID
     */
    public void removeTeamMember(long id) {
        if (!authService.isAdministratorMode()) {
            throw new SecurityException("Only administrators can remove team members");
        }

        Optional<TeamMember> member = teamManager.getTeamMemberById(id);
        if (member.isEmpty()) {
            throw new IllegalArgumentException("Team member not found");
        }

        teamManager.removeTeamMember(id);
    }

    /**
     * Toggle the active status of a team member (requires administrator).
     *
     * @param id the team member ID
     */
    public void toggleTeamMemberStatus(long id) {
        if (!authService.isAdministratorMode()) {
            throw new SecurityException("Only administrators can toggle team member status");
        }

        Optional<TeamMember> member = teamManager.getTeamMemberById(id);
        if (member.isEmpty()) {
            throw new IllegalArgumentException("Team member not found");
        }

        teamManager.archiveTeamMember(id);
    }

    /**
     * Validate team member input.
     *
     * @param name the member's name
     * @param email the member's email
     * @param role the member's role
     * @throws IllegalArgumentException if inputs are invalid
     */
    private void validateTeamMemberInput(String name, String email, UserRole role) {
        validateMemberName(name);
        validateMemberEmail(email);
        
        if (role == null || role == UserRole.PUBLIC_USER) {
            throw new IllegalArgumentException("Team member role must be MODERATOR or ADMINISTRATOR");
        }
    }

    /**
     * Validate member name.
     *
     * @param name the name to validate
     * @throws IllegalArgumentException if name is invalid
     */
    private void validateMemberName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name is too long (max 100 characters)");
        }
    }

    /**
     * Validate member email.
     *
     * @param email the email to validate
     * @throws IllegalArgumentException if email is invalid
     */
    private void validateMemberEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email format is invalid");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email is too long");
        }
    }
}
