package com.spikeaware;

import com.spikeaware.db.DatabaseManager;
import com.spikeaware.team.TeamManager;
import com.spikeaware.team.TeamMember;
import com.spikeaware.team.UserRole;
import com.spikeaware.service.AuthenticationService;
import com.spikeaware.service.ResourceService;
import com.spikeaware.service.TeamService;
import com.spikeaware.model.*;

import java.util.List;
import java.util.Scanner;

/**
 * Main class - Acts as the CLI entry point for the Spike Aware UK Resource Aggregator.
 * Manages user interaction through a console interface, delegating business logic to service classes.
 */
public class Main {
    private static DatabaseManager db;
    private static TeamManager teamManager;
    private static AuthenticationService authService;
    private static ResourceService resourceService;
    private static TeamService teamService;
    private static Scanner scanner;

    public static void main(String[] args) {
        // Initialize services
        db = new DatabaseManager();
        teamManager = new TeamManager();
        authService = new AuthenticationService();
        resourceService = new ResourceService(db, authService);
        teamService = new TeamService(teamManager, authService);
        scanner = new Scanner(System.in);
        
        boolean running = true;

        System.out.println("/-------------------------------------------------------\\");
        System.out.println("|   Welcome to Spike Aware UK - Resource Aggregator     |");
        System.out.println("\\-------------------------------------------------------/");

        try {
            while (running) {
                printMenu();
                String input = scanner.nextLine().trim();
                running = handleUserInput(input);
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine(); // Pause before showing menu again
            }

            System.out.println("\nThank you for using Spike Aware UK. Goodbye!");
        } finally {
            scanner.close();
        }
    }

    /**
     * Prints the main menu with options based on user's current role.
     */
    private static void printMenu() {
        System.out.println("\n/-------------------------------------------------------\\");
        System.out.println("|              Current Role: " + String.format("%-23s", authService.getCurrentRole().getDisplayName()) + "    |");
        System.out.println("\\-------------------------------------------------------/");
        System.out.println("0. Exit");
        
        if (authService.isPublicMode()) {
            System.out.println("1. View All Published Resources");
        } else {
            System.out.println("1. View All Resources");
        }
        System.out.println("2. Search Resources");
        System.out.println("3. View Resource by ID");
        System.out.println("4. Add Research Resource");
        System.out.println("5. Add Public Resource");
        System.out.println("6. Flag a Resource");

        if (authService.isPublicMode()) {
            System.out.println("7. Login as Moderator/Administrator");
        } else {
            // Moderator and Administrator options
            System.out.println("7. Logout");
            System.out.println("8. Moderate (Review Pending Resources)");
            System.out.println("9. View Flagged Resources");
            System.out.println("10. View System Analytics");
            System.out.println("11. Edit Resource");
            System.out.println("12. Remove Resource");
            System.out.println("13. Archive Resource");
            
            if (authService.isAdministratorMode()) {
                System.out.println("14. Manage Team");
            }
        }

        System.out.print("\nSelect an option: ");
    }

    /**
     * Handles user input and routes to appropriate methods.
     *
     * @param input the user's menu selection
     * @return false if user chose to exit, true otherwise
     */
    private static boolean handleUserInput(String input) {
        if (authService.isPublicMode()) {
            return handlePublicUserInput(input);
        } else {
            return handleModeratorInput(input);
        }
    }

    /**
     * Handles input for public users.
     *
     * @param input the user's menu selection
     * @return false if user chose to exit, true otherwise
     */
    private static boolean handlePublicUserInput(String input) {
        switch (input) {
            case "0":
                return false;
            case "1":
                viewAllPublishedResources();
                break;
            case "2":
                searchResources();
                break;
            case "3":
                viewResourceById();
                break;
            case "4":
                addResearchResource();
                break;
            case "5":
                addPublicResource();
                break;
            case "6":
                flagResource();
                break;
            case "7":
                login();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return true;
    }

    /**
     * Handles input for moderators and administrators.
     *
     * @param input the user's menu selection
     * @return false if user chose to exit, true otherwise
     */
    private static boolean handleModeratorInput(String input) {
        switch (input) {
            case "0":
                return false;
            case "1":
                viewAllResources();
                break;
            case "2":
                searchResources();
                break;
            case "3":
                viewResourceById();
                break;
            case "4":
                addResearchResource();
                break;
            case "5":
                addPublicResource();
                break;
            case "6":
                flagResource();
                break;
            case "7":
                logout();
                break;
            case "8":
                moderateResources();
                break;
            case "9":
                viewFlaggedResources();
                break;
            case "10":
                viewSystemAnalytics();
                break;
            case "11":
                editResource();
                break;
            case "12":
                removeResource();
                break;
            case "13":
                archiveResource();
                break;
            case "14":
                if (authService.isAdministratorMode()) {
                    manageTeam();
                } else {
                    System.out.println("Invalid option. Please try again.");
                }
                break;           
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return true;
    }

    /**
     * Displays all published resources.
     */
    private static void viewAllPublishedResources() {
        List<Resource> published = resourceService.getPublishedResources();
        if (published.isEmpty()) {
            System.out.println("\nNo published resources available.");
            return;
        }

        System.out.println("\nPublished Resources");
        for (int i = 0; i < published.size(); i++) {
            System.out.println((i + 1) + ". " + published.get(i).toString());
        }
    }

    /**
     * Displays all resources.
     */
    private static void viewAllResources() {
        List<Resource> resources = resourceService.getAllResources();
        if (resources.isEmpty()) {
            System.out.println("\nNo resources available.");
            return;
        }

        System.out.println("\nAll Resources");
        for (int i = 0; i < resources.size(); i++) {
            System.out.println((i + 1) + ". " + resources.get(i).toString() + " (Status: " + resources.get(i).getStatus() + ")");
        }
    }

    /**
     * Searches for resources by keyword.
     */
    private static void searchResources() {
        System.out.print("\nEnter search keyword: ");
        String keyword = scanner.nextLine().trim();

        try {
            List<Resource> results = resourceService.searchResources(keyword);

            if (results.isEmpty()) {
                System.out.println("No resources found matching '" + keyword + "'.");
                return;
            }

            System.out.println("\nSearch Results");
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i).toString());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Views a resource by its ID.
     */
    private static void viewResourceById() {
        System.out.print("\nEnter Resource ID: ");
        long id = parseLongInput();

        if (id == -1) {
            System.out.println("Invalid ID format. Please enter a valid number.");
            return;
        }

        var resource = resourceService.getResource(id);
        if (resource.isEmpty()) {
            System.out.println("Resource not found or not accessible.");
            return;
        }

        resource.get().display();
    }

    /**
     * Adds a new research resource.
     */
    private static void addResearchResource() {
        System.out.println("\nAdd Research Resource");
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("URL: ");
        String url = scanner.nextLine().trim();
        System.out.print("Authors: ");
        String authors = scanner.nextLine().trim();
        System.out.print("Year: ");
        int year = (int) parseLongInput();

        try {
            ResearchResource resource = resourceService.addResearchResource(title, url, authors, year);
            String status = authService.isModeratorMode() ? ResourceStatus.APPROVED.getDisplayName() : ResourceStatus.PENDING.getDisplayName();
            System.out.println("Research resource added successfully! (ID: " + resource.getId() + ", Status: " + status + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Adds a new public resource.
     */
    private static void addPublicResource() {
        System.out.println("\nAdd Public Resource");
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("URL: ");
        String url = scanner.nextLine().trim();
        System.out.print("Organization: ");
        String org = scanner.nextLine().trim();
        System.out.print("Target Audience: ");
        String audience = scanner.nextLine().trim();

        try {
            PublicResource resource = resourceService.addPublicResource(title, url, org, audience);
            String status = authService.isModeratorMode() ? ResourceStatus.APPROVED.getDisplayName() : ResourceStatus.PENDING.getDisplayName();
            System.out.println("Public resource added successfully! (ID: " + resource.getId() + ", Status: " + status + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Flags a published resource for moderation review.
     */
    private static void flagResource() {
        System.out.print("\nEnter Resource ID to flag: ");
        long id = parseLongInput();

        if (id == -1) {
            System.out.println("Invalid ID format.");
            return;
        }

        try {
            resourceService.flagResource(id);
            System.out.println("Resource flagged successfully.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user as a moderator or administrator.
     */
    private static void login() {
        System.out.print("\nEnter password (mod123 for Moderator, admin123 for Administrator): ");
        String password = scanner.nextLine().trim();

        UserRole role = authService.authenticate(password);
        if (role != null) {
            System.out.println("Login successful! You are now in " + role.getDisplayName() + " mode.");
        } else {
            System.out.println("Invalid password. Login failed.");
        }
    }

    /**
     * Logs out a moderator or administrator and returns to public user mode.
     */
    private static void logout() {
        authService.logout();
        System.out.println("You have been logged out. Returning to Public User mode.");
    }

    /**
     * Moderator moderation workflow - review and approve/reject pending resources.
     */
    private static void moderateResources() {
        try {
            List<Resource> pendingResources = resourceService.getPendingResources();

            if (pendingResources.isEmpty()) {
                System.out.println("\nNo pending resources to moderate.");
                return;
            }

            System.out.println("\nModeration Queue");
            System.out.println("Total pending resources: " + pendingResources.size());

            for (Resource resource : pendingResources) {
                resource.display();

                System.out.println("Action: (a) Approve | (r) Reject | (s) Skip");
                System.out.print("Your decision: ");
                String decision = scanner.nextLine().trim().toLowerCase();

                switch (decision) {
                    case "a":
                        resourceService.approveResource(resource.getId());
                        System.out.println("Resource approved and published.\n");
                        break;
                    case "r":
                        resourceService.rejectResource(resource.getId());
                        System.out.println("Resource rejected.\n");
                        break;
                    case "s":
                        System.out.println("Skipped.\n");
                        break;
                    default:
                        System.out.println("Invalid input. Skipping...\n");
                }
            }

            System.out.println("Moderation session complete.");
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Views all flagged resources (those with flag count > 0).
     */
    private static void viewFlaggedResources() {
        try {
            List<Resource> flaggedResources = resourceService.getFlaggedResources();

            if (flaggedResources.isEmpty()) {
                System.out.println("\nNo flagged resources");
                return;
            }

            System.out.println("\nFlagged Resources (sorted by flag count)");
            for (int i = 0; i < flaggedResources.size(); i++) {
                System.out.println((i + 1) + ". " + flaggedResources.get(i).toString() + " (Flags: " + flaggedResources.get(i).getFlagCount() + ")");
            }
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Views system analytics.
     */
    private static void viewSystemAnalytics() {
        try {
            System.out.println("\n" + resourceService.getAnalytics());
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Edits a resource's title and URL.
     */
    private static void editResource() {
        System.out.print("\nEnter Resource ID to edit: ");
        long id = parseLongInput();

        if (id == -1) {
            System.out.println("Invalid ID format.");
            return;
        }

        var resource = resourceService.getResource(id);
        if (resource.isEmpty()) {
            System.out.println("Resource not found.");
            return;
        }

        resource.get().display();

        System.out.print("\nEnter new title (or press Enter to keep current): ");
        String newTitle = scanner.nextLine().trim();

        System.out.print("Enter new URL (or press Enter to keep current): ");
        String newUrl = scanner.nextLine().trim();

        try {
            resourceService.editResource(id, newTitle.isEmpty() ? null : newTitle, newUrl.isEmpty() ? null : newUrl);
            System.out.println("Resource edited successfully.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Removes (deletes) a resource from the system.
     */
    private static void removeResource() {
        System.out.print("\nEnter Resource ID to remove: ");
        long id = parseLongInput();

        if (id == -1) {
            System.out.println("Invalid ID format.");
            return;
        }

        var resource = resourceService.getResource(id);
        if (resource.isEmpty()) {
            System.out.println("Resource not found.");
            return;
        }

        resource.get().display();

        System.out.print("\nAre you sure you want to remove this resource? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirmation)) {
            try {
                resourceService.removeResource(id);
                System.out.println("Resource removed successfully.");
            } catch (SecurityException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Removal cancelled.");
        }
    }

    /**
     * Archives or unarchives a resource.
     */
    private static void archiveResource() {
        System.out.print("\nEnter Resource ID to archive: ");
        long id = parseLongInput();

        if (id == -1) {
            System.out.println("Invalid ID format.");
            return;
        }

        var resource = resourceService.getResource(id);
        if (resource.isEmpty()) {
            System.out.println("Resource not found.");
            return;
        }

        Resource r = resource.get();
        
        if (ResourceStatus.ARCHIVED.equals(r.getStatus())) {
            r.display();
            System.out.print("Resource is already archived. Unarchive? (y/N): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("y".equals(confirmation)) {
                try {
                    resourceService.unarchiveResource(id);
                    System.out.println("Resource unarchived successfully.");
                } catch (SecurityException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Unarchive cancelled.");
            }
            return;
        }

        r.display();
        System.out.print("\nArchive this resource? (y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if ("y".equals(confirmation)) {
            try {
                resourceService.archiveResource(id);
                System.out.println("Resource archived successfully.");
            } catch (SecurityException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Archive cancelled.");
        }
    }

    /**
     * Team management for administrators.
     */
    private static void manageTeam() {
        try {
            System.out.println("\nTeam Management");
            System.out.println("0. Back to Admin Menu");
            System.out.println("1. View Team Members");
            System.out.println("2. Add Member");
            System.out.println("3. Remove Member");
            System.out.println("4. Edit Team Member");
            System.out.println("5. Activate/Deactivate Team Member");
            System.out.print("\nSelect an option: ");
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllTeamMembers();
                    break;
                case "2":
                    addTeamMember();
                    break;
                case "3":
                    removeTeamMember();
                    break;
                case "4":
                    editTeamMember();
                    break;
                case "5":
                    toggleTeamMemberActiveStatus();
                    break;
                case "0":
                    System.out.print("Press Enter to continue...");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Adds a new team member.
     */
    private static void addTeamMember() {
        System.out.println("\nAdd Team Member");
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Role: 1) Admin | 2) Moderator: ");
        String choice = scanner.nextLine().trim();
        UserRole role;
        switch (choice) {
            case "1":
                role = UserRole.ADMINISTRATOR;
                break;
            case "2":
                role = UserRole.MODERATOR;
                break;
            default:
                System.out.println("Invalid role. Team member not added.");
                return;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        try {
            TeamMember member = teamService.addTeamMember(name, email, role);
            System.out.println("Team member added successfully! (ID: " + member.getId() + ")");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Displays full team member list.
     */
    private static void viewAllTeamMembers() {
        try {
            List<TeamMember> teamMembers = teamService.getAllTeamMembers();
            if (teamMembers.isEmpty()) {
                System.out.println("\nNo team members available.");
                return;
            }

            System.out.println("\nTeam Members");
            for (TeamMember member : teamMembers) {
                System.out.println(member.toString());
            }
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Removes a team member.
     */
    private static void removeTeamMember() {
        System.out.print("\nEnter Team Member ID to remove: ");
        long id = parseLongInput();

        if (id == -1) {
            System.out.println("Invalid ID format.");
            return;
        }

        try {
            var member = teamService.getTeamMember(id);
            if (member.isEmpty()) {
                System.out.println("Team member not found.");
                return;
            }

            member.get().toString();

            System.out.print("\nAre you sure you want to remove this team member? (yes/no): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("yes".equals(confirmation)) {
                teamService.removeTeamMember(id);
                System.out.println("Team member removed successfully.");
            } else {
                System.out.println("Removal cancelled.");
            }
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Edits a team member's name, email and role.
     */
    private static void editTeamMember() {
        System.out.print("\nEnter Team Member ID to edit: ");
        long id = parseLongInput();

        if (id == -1) {
            System.out.println("Invalid ID format.");
            return;
        }

        try {
            var member = teamService.getTeamMember(id);
            if (member.isEmpty()) {
                System.out.println("Team member not found.");
                return;
            }

            member.get().toString();

            System.out.print("\nEnter new name (or press Enter to keep current): ");
            String newName = scanner.nextLine().trim();

            System.out.print("Enter new email (or press Enter to keep current): ");
            String newEmail = scanner.nextLine().trim();

            System.out.print("Select a new role (1=Admin, 2=Moderator, or press Enter to keep current): ");
            String choice = scanner.nextLine().trim();
            UserRole newRole = null;
            
            if (!choice.isEmpty()) {
                newRole = "1".equals(choice) ? UserRole.ADMINISTRATOR : "2".equals(choice) ? UserRole.MODERATOR : null;
            }

            teamService.editTeamMember(id, 
                newName.isEmpty() ? null : newName, 
                newEmail.isEmpty() ? null : newEmail, 
                newRole);
            System.out.println("Team member edited successfully.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Toggles the active status of a team member.
     */
    private static void toggleTeamMemberActiveStatus() {
        System.out.print("\nEnter Team Member ID to activate/deactivate: ");
        long id = parseLongInput();

        if (id == -1) {
            System.out.println("Invalid ID format.");
            return;
        }

        try {
            var member = teamService.getTeamMember(id);
            if (member.isEmpty()) {
                System.out.println("Team member not found.");
                return;
            }

            System.out.println(member.get().toString());
            System.out.print("Proceed with toggling active status? (y/N): ");

            if ("y".equals(scanner.nextLine().trim().toLowerCase())) {
                teamService.toggleTeamMemberStatus(id);
                System.out.println("Team member status toggled successfully.");
            } else {
                System.out.println("Operation cancelled.");
            }
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Parses a long integer from user input.
     * Returns -1 if input is invalid.
     */
    private static long parseLongInput() {
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
