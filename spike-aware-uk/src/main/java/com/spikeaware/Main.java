/**
 *  Author: 2442903
 *  Module: MOD003484 - Software Principles Project
 *  Team: William Software House
 *  Description: Main class for Spike Aware UK Resource Aggregator CLI application.
 *  Handles user interaction and delegates business logic to service classes.
 *  Version: 1.0
 *  Date: 20-04-2026
 */
package com.spikeaware;

import com.spikeaware.db.DatabaseManager;
import com.spikeaware.team.TeamManager;
import com.spikeaware.team.TeamMember;
import com.spikeaware.team.UserRole;
import com.spikeaware.ui.Menu;
import com.spikeaware.service.AuthenticationService;
import com.spikeaware.service.ResourceService;
import com.spikeaware.service.TeamService;
import com.spikeaware.model.*;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class - Acts as the CLI entry point for the Spike Aware UK Resource Aggregator.
 * Manages user interaction through a console interface, delegating business logic to service classes.
 */
public class Main {
    private static DatabaseManager dataManger;
    private static TeamManager teamManager;
    private static AuthenticationService authService;
    private static ResourceService resourceService;
    private static TeamService teamService;
    private static Scanner scanner;
    private static Menu mainMenu;
    private static boolean running; 
    private static UserRole userRole;  

    public static void main(String[] args) {
        // Initialize services
        dataManger = new DatabaseManager();
        teamManager = new TeamManager();
        authService = new AuthenticationService();
        resourceService = new ResourceService(dataManger, authService);
        teamService = new TeamService(teamManager, authService);
        scanner = new Scanner(System.in);
        mainMenu = new Menu(scanner);
        running = true;

        System.out.println("/-------------------------------------------------------\\");
        System.out.println("|   Welcome to Spike Aware UK - Resource Aggregator     |");
        System.out.println("\\-------------------------------------------------------/");

        try {
            while (running) {
                showMainMenu();
            }

            System.out.println("\nThank you for using Spike Aware UK. Goodbye!");
        } finally {
            scanner.close();
        }
    }

    /**
     * Displays the main menu and handles user input to navigate through the application.
     * The menu options change dynamically based on the user's role (public, moderator, administrator).
     * This method serves as the central hub for user interaction, allowing access to all features of the application.
     * It also demonstrates how role-based access control is implemented, with different options available for different user roles.
     * The menu is displayed in a loop until the user chooses to exit, allowing for continuous interaction without restarting the application.
     */
    private static void showMainMenu() {    
        userRole = authService.getCurrentRole();
        mainMenu.clearOptions(); // Clear previous options to avoid duplicates when role changes

        System.out.println("\n/-------------------------------------------------------\\");
        System.out.println("|              Current Role: " + String.format("%-23s", userRole.getDisplayName()) + "    |");
        System.out.println("\\-------------------------------------------------------/");
        
        mainMenu.addOption("Exit", () -> running = false);
        mainMenu.addOption(authService.isPublicMode() ? "View Published Resources" : "View All Resources", () -> {
            if (authService.isPublicMode()) {
                viewAllPublishedResources();
            } else {
                viewAllResources();
            }
        });
        mainMenu.addOption("Search Resources", () -> searchResources());
        mainMenu.addOption("View by ID", () -> viewResourceById());
        mainMenu.addOption("Submit a Public Resource", () -> addPublicResource());
        mainMenu.addOption("Submit a Research Resource", () -> addResearchResource());
        mainMenu.addOption("Flag Resource", () -> flagResource());
        if (!authService.isPublicMode()) {
            showModeratorMenu();              
        } else {       
            mainMenu.addOption("Staff Login", () -> showStaffLogin());
            mainMenu.displayAndRun();
        }
    }

    /**
     * Authenticates a user as a moderator or administrator.
     * Lacking a GUI, this replaces the proposed modal.
     * The authentication process is simplified for this CLI application, using hardcoded passwords for demonstration purposes.
     * In a real application, this would involve secure password handling and user management.
     * The AuthenticationService class handles the logic for verifying credentials and managing user roles.
     * After successful login, the menu options will change to reflect the user's new role (moderator or administrator).
     * If login fails, the user remains in public mode with limited access to features.
     * This method also demonstrates how role-based access control is implemented in the application, allowing for different functionalities based on user roles.
     * The login credentials are as follows:
     * - Moderator: mod123
     * - Administrator: admin123
     */
    private static void showStaffLogin() {
        System.out.print("\nEnter password (mod123 for Moderator, admin123 for Administrator): ");
        String password = scanner.nextLine().trim();

        try {
            UserRole role = authService.authenticate(password);
            if (role != null) {
                System.out.println("Login successful! You are now in " + role.getDisplayName() + " mode.");
            } else {
                System.out.println("Invalid password. Login failed.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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
     * Displays the moderator menu.
     */
    private static void showModeratorMenu() {

        mainMenu.addOption("Logout", () -> logout());
        mainMenu.addOption("Moderate (Review Pending Resources)", () -> moderateResources());
        mainMenu.addOption("View Flagged Resources", () -> viewFlaggedResources());
        mainMenu.addOption("View System Analytics", () -> viewSystemAnalytics());
        mainMenu.addOption("Edit Resource", () -> editResource());
        mainMenu.addOption("Archive Resource", () -> archiveResource());
        mainMenu.addOption("Data Drift Detection", () -> showDataDriftMenu());
        if (authService.isAdministratorMode()) {
            showAdminMenu();
        } else {
            mainMenu.displayAndRun();
        }
    }

    /**
     * Team management for administrators.
     */
    private static void manageTeam() {
        Menu teamMenu = new Menu(scanner);
        teamMenu.addOption("\nView Team Members", () -> viewAllTeamMembers());
        teamMenu.addOption("Add Member", () -> addTeamMember());
        teamMenu.addOption("Remove Member", () -> removeTeamMember());
        teamMenu.addOption("Edit Team Member", () -> editTeamMember());
        teamMenu.addOption("Activate/Deactivate Team Member", () -> toggleTeamMemberActiveStatus());

        teamMenu.displayAndRun();
    }

    /**
     * Displays the administrator menu.
     */
    private static void showAdminMenu() {

        mainMenu.addOption("Remove Resource", () -> removeResource());
        mainMenu.addOption("Manage Team Members", () -> manageTeam());

        mainMenu.displayAndRun();
    }

    /**
     * Displays all published resources.
     * Only resources with status "PUBLISHED" are shown to public users.
     * This would be the Home Page view for the web application, 
     * as suggested in the user flow diagram, but a lack of a GUI limits usabiltity.
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
     * Moderators and administrators can see all resources regardless of status.
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
     * Searches for resources by keyword/ID.
     * The filtering of the search results are limited by the database structure,
     * multiple searches in one query to narrow results has not been implimented for simplicty.
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
     * This will also increment the view count for the resource if the user is not a moderator or administrator, as defined in the ResourceService and DatabaseManager classes.
     * This would be the "Viewing Page" for the web application, as suggested in the user flow diagram, but a lack of a GUI limits usabiltity.
     */
    private static void viewResourceById() {
        System.out.print("\nEnter Resource ID: ");
        long id = parseLongInput();

        if (id == -1) {
            System.out.println("Invalid ID format. Please enter a valid number.");
            return;
        }

        try {
            var resource = resourceService.getResource(id); // This will also increment the view count
            if (resource.isEmpty()) {
                System.out.println("Resource not found or not accessible.");
                return;
            }

            resource.get().display();
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Adds a new research resource.
     * Lacking a GUI, this replaces the proposed modal.
     * There is no method implimented to check for dulicate resources,
     * but proper error handling is included to manage cases where there are issues with resource creation.
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
            SubmissionResult result = resourceService.addResearchResource(title, url, authors, year);
            String status = authService.isModeratorMode() ? ResourceStatus.APPROVED.getDisplayName() : ResourceStatus.PENDING.getDisplayName();
            System.out.println("Research resource added successfully! (ID: " + result.getResource().getId() + ", Status: " + status + ")");
            
            // Display drift warnings if any
            if (result.hasWarnings()) {
                displaySubmissionWarnings(result);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Adds a new public resource.
     * Lacking a GUI, this replaces the proposed modal.
     * There is no method implimented to check for dulicate resources,
     * but proper error handling is included to manage cases where there are issues with resource creation.
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
            SubmissionResult result = resourceService.addPublicResource(title, url, org, audience);
            String status = authService.isModeratorMode() ? ResourceStatus.APPROVED.getDisplayName() : ResourceStatus.PENDING.getDisplayName();
            System.out.println("Public resource added successfully! (ID: " + result.getResource().getId() + ", Status: " + status + ")");
            
            // Display drift warnings if any
            if (result.hasWarnings()) {
                displaySubmissionWarnings(result);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Flags a published resource for moderation review.
     * No process currently exists for privilaged users to unflag resources or for moderators to "fix" the flagged issue(s),
     * but this would allow users to report issues with resources and moderators to view them in descending order of number of flags.
     * Object based flagging was not implimented, but the flag count is tracked for each resource and can be used to generate analytics data in a more fully featured application.
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
     * Moderator moderation workflow - review and approve/reject pending resources.
     * Edit is not inclued in this workflow to simplify the menus.
     * This would be the "View Pending Resources" for the web application, as suggested in the user flow diagram, but a lack of a GUI limits usabiltity.
     * Moderators can view all pending resources and choose to approve, reject, or skip each one.
     * Approved resources are published immediately, while rejected resources are removed from the pending queue.
     * Skipped resources remain in the pending queue for later review.
     * The data drift feature was not implimented as there is no method to check for working links other than ensuring proper format.
     * The ResourceService class handles the business logic for approving and rejecting resources, while the DatabaseManager class manages the underlying data storage and retrieval.
     * Proper error handling is included to manage cases where there are no pending resources or if there are issues with resource retrieval or status updates.
     * This method also demonstrates how role-based access control is enforced, as only users with moderator or administrator roles can access this functionality.
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

                Menu decisionMenu = new Menu(scanner);
                decisionMenu.addOption("Approve", () -> {
                    resourceService.approveResource(resource.getId());
                    System.out.println("Resource approved and published.\n");
                });
                decisionMenu.addOption("Reject", () -> {
                    resourceService.rejectResource(resource.getId());
                    System.out.println("Resource rejected.\n");
                });
                decisionMenu.addOption("Skip", () -> {
                    System.out.println("Skipped.\n");
                });
                
                System.out.println("Action:");
                decisionMenu.displayAndRun();
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
     * This would be the "Analytics Dashboard" for the web application, as suggested in the user flow diagram, but a lack of a GUI limits usabiltity.
     * Moderators and administrators can view analytics data such as total resources, published resources, flagged resources, etc.
     * Click-through rates and data drift analytics were not implimented due to the limitations of a CLI application and the scope of this project, 
     * but the ResourceService class includes methods to track view counts and flag counts for each resource, which could be used to generate analytics data in a more fully featured application.
     * The ResourceService class handles the business logic for generating analytics data, while the DatabaseManager class manages the underlying data storage and retrieval.
     * Proper error handling is included to manage cases where there are issues with data retrieval or if the user does not have permission to access analytics.
     * This method also demonstrates how role-based access control is enforced, as only users with moderator or administrator roles can access this functionality.
     * The analytics data is displayed in a simple text format suitable for CLI output.
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

        try {
            var resource = resourceService.getResource(id);
            if (resource.isEmpty()) {
                System.out.println("Resource not found.");
                return;
            }

            resource.get().display();

            System.out.print("\nAre you sure you want to remove this resource? (yes/no): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("yes".equals(confirmation)) {
                resourceService.removeResource(id);
                System.out.println("Resource removed successfully.");
            } else {
                System.out.println("Removal cancelled.");
            }
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
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

        try {
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
                    resourceService.unarchiveResource(id);
                    System.out.println("Resource unarchived successfully.");
                } else {
                    System.out.println("Unarchive cancelled.");
                }
                return;
            }

            r.display();
            System.out.print("\nArchive this resource? (y/N): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("y".equals(confirmation)) {
                resourceService.archiveResource(id);
                System.out.println("Resource archived successfully.");
            } else {
                System.out.println("Archive cancelled.");
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

    /**
     * Displays the data drift detection submenu for moderators.
     * Allows moderators to perform various drift detection operations on resources.
     */
    private static void showDataDriftMenu() {
        Menu driftMenu = new Menu(scanner);
        driftMenu.addOption("Back", () -> {});
        driftMenu.addOption("Perform Full Drift Detection", () -> performFullDriftDetection());
        driftMenu.addOption("Check for Duplicate URLs", () -> checkDuplicateUrls());
        driftMenu.addOption("Check for Invalid URLs", () -> checkInvalidUrls());
        driftMenu.addOption("Check for Suspicious Patterns", () -> checkSuspiciousPatterns());
        driftMenu.addOption("Check URL Duplicates", () -> checkSpecificUrlDuplicates());

        driftMenu.displayAndRun();
    }

    /**
     * Performs comprehensive data drift detection on all resources.
     */
    private static void performFullDriftDetection() {
        try {
            System.out.println("\nPerforming comprehensive data drift detection...\n");
            DriftReport report = resourceService.performDataDriftDetection();
            
            System.out.println(resourceService.getDriftAnalysisSummary(report));
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Detects and displays duplicate URLs in the system.
     */
    private static void checkDuplicateUrls() {
        try {
            System.out.println("\nScanning for duplicate URLs...\n");
            Map<String, List<Resource>> duplicates = resourceService.detectDuplicateUrls();

            if (duplicates.isEmpty()) {
                System.out.println("No duplicate URLs found. System is clean.");
                return;
            }

            System.out.println("Found " + duplicates.size() + " duplicate URL(s):\n");
            
            for (Map.Entry<String, List<Resource>> entry : duplicates.entrySet()) {
                System.out.println("URL: " + entry.getKey());
                System.out.println("Appears in " + entry.getValue().size() + " resource(s):");
                for (int i = 0; i < entry.getValue().size(); i++) {
                    Resource r = entry.getValue().get(i);
                    System.out.println("  " + (i + 1) + ". ID: " + r.getId() + " | Title: " + r.getTitle() + " | Status: " + r.getStatus());
                }
                System.out.println();
            }
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Detects and displays resources with invalid URL formats.
     */
    private static void checkInvalidUrls() {
        try {
            System.out.println("\nScanning for invalid URL formats...\n");
            List<Resource> invalidUrls = resourceService.detectInvalidUrls();

            if (invalidUrls.isEmpty()) {
                System.out.println("No invalid URLs found. All URLs have valid format.");
                return;
            }

            System.out.println("Found " + invalidUrls.size() + " resource(s) with invalid URL(s):\n");
            
            for (int i = 0; i < invalidUrls.size(); i++) {
                Resource r = invalidUrls.get(i);
                System.out.println((i + 1) + ". ID: " + r.getId() + " | Title: " + r.getTitle());
                System.out.println("   URL: " + r.getUrl());
                System.out.println("   Status: " + r.getStatus() + "\n");
            }
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Detects and displays resources with suspicious patterns in titles or URLs.
     */
    private static void checkSuspiciousPatterns() {
        try {
            System.out.println("\nScanning for suspicious patterns...\n");
            List<Resource> suspicious = resourceService.detectSuspiciousPatterns();

            if (suspicious.isEmpty()) {
                System.out.println("No suspicious patterns detected. System appears clean.");
                return;
            }

            System.out.println("Found " + suspicious.size() + " resource(s) with suspicious patterns:\n");
            
            for (int i = 0; i < suspicious.size(); i++) {
                Resource r = suspicious.get(i);
                System.out.println((i + 1) + ". ID: " + r.getId() + " | Title: " + r.getTitle());
                System.out.println("   URL: " + r.getUrl());
                System.out.println("   Status: " + r.getStatus());
                System.out.println("   ⚠️ Review this resource for potential security issues.\n");
            }
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Allows moderators to check if a specific URL has duplicates.
     */
    private static void checkSpecificUrlDuplicates() {
        System.out.print("\nEnter URL to check: ");
        String url = scanner.nextLine().trim();

        if (url.isEmpty()) {
            System.out.println("URL cannot be empty.");
            return;
        }

        try {
            List<Resource> duplicates = resourceService.checkUrlDuplicates(url);

            if (duplicates.isEmpty()) {
                System.out.println("\nNo duplicates found for: " + url);
                return;
            }

            System.out.println("\nFound " + duplicates.size() + " resource(s) with URL: " + url + "\n");
            
            for (int i = 0; i < duplicates.size(); i++) {
                Resource r = duplicates.get(i);
                System.out.println((i + 1) + ". ID: " + r.getId() + " | Title: " + r.getTitle() + " | Status: " + r.getStatus());
            }
        } catch (SecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Display submission warnings (drift issues detected during resource creation).
     * Shows warnings to the user but does NOT prevent submission.
     */
    private static void displaySubmissionWarnings(SubmissionResult result) {
        System.out.println(result.formatForDisplay());
        
        List<DriftWarning> critical = result.getCriticalWarnings();
        if (!critical.isEmpty()) {
            System.out.println("\n⚠️  CRITICAL WARNINGS - PLEASE REVIEW:");
            for (DriftWarning w : critical) {
                System.out.println("  - " + w.getType() + ": " + w.getMessage());
            }
            System.out.println("\nNote: Resource was submitted despite warnings. Moderators will review.");
        }
    }
}

