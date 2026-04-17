package com.spikeaware.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ConfigurationManager handles all application configuration including file paths.
 * Provides environment-agnostic path resolution.
 */
public class ConfigurationManager {
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String DATA_DIR = "data";
    private static final String RESOURCES_FILE = "data.json";
    private static final String TEAM_FILE = "team_data.json";

    /**
     * Get the data directory path, creating it if it doesn't exist.
     *
     * @return the Path to the data directory
     */
    public static Path getDataDirectory() {
        Path dataDir = Paths.get(PROJECT_ROOT).resolve(DATA_DIR);
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
            } catch (Exception e) {
                System.err.println("Error creating data directory: " + e.getMessage());
            }
        }
        return dataDir;
    }

    /**
     * Get the resources data file path.
     *
     * @return the Path to the resources JSON file
     */
    public static Path getResourcesDataFile() {
        return getDataDirectory().resolve(RESOURCES_FILE);
    }

    /**
     * Get the team data file path.
     *
     * @return the Path to the team data JSON file
     */
    public static Path getTeamDataFile() {
        return getDataDirectory().resolve(TEAM_FILE);
    }

    /**
     * Get the project root directory.
     *
     * @return the Path to the project root
     */
    public static Path getProjectRoot() {
        return Paths.get(PROJECT_ROOT);
    }

    /**
     * Print configuration information for debugging.
     */
    public static void printConfiguration() {
        System.out.println("\n--- Configuration ---");
        System.out.println("Project Root: " + PROJECT_ROOT);
        System.out.println("Data Directory: " + getDataDirectory());
        System.out.println("Resources File: " + getResourcesDataFile());
        System.out.println("Team File: " + getTeamDataFile());
        System.out.println("---------------------\n");
    }
}
