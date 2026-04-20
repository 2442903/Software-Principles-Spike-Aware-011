package com.spikeaware.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Menu {
    private final Map<String, Runnable> options;
    private final Scanner scanner;

    /**
     * Constructor for Menu.
     *
     * @param scanner the Scanner instance for user input
     */
    public Menu(Scanner scanner) {
        // LinkedHashMap maintains the insertion order of your menu items
        this.options = new LinkedHashMap<>();
        this.scanner = scanner;
    }

    /**
     * Adds an option to the menu.
     * @param description The text displayed to the user
     * @param action The method/block of code to run when selected
     */
    public void addOption(String description, Runnable action) {
        options.put(description, action);
    }

    /**
     * Clears all options from the menu.
     */
    public void clearOptions() {
        options.clear();
    }

    /**
     * Displays the menu, handles user input cleanly, and runs the selected action.
     * @return false if the user chooses an exit action, true otherwise.
     */
    public void displayAndRun() {
        while (true) {
            int index = 1;
            
            for (String desc : options.keySet()) {
                System.out.println(index++ + ". " + desc);
            }
            System.out.print("\nSelect an option: ");

            // Handle non-integer inputs gracefully
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // Consume the bad input
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            if (choice > 0 && choice <= options.size()) {
                // Execute the chosen command
                int currentIndex = 1;
                for (Runnable action : options.values()) {
                    if (currentIndex == choice) {
                        action.run();
                        return; // Return control after execution
                    }
                    currentIndex++;
                }
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
