package org.truecaller.prefixmatcher;

import lombok.extern.slf4j.Slf4j;
import org.truecaller.prefixmatcher.service.PrefixMatchingService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main application class and entry point.
 * This class handles initialization, provides a simple command-line interface (CLI),
 * and drives the core logic of the PrefixMatchingService.
 */
@Slf4j
public class Main {

    /** Constant command used to terminate the application loop. */
    private static final String EXIT_CMD = "exit";

    /**
     * The main method, serving as the application's entry point.
     * It sets up the matching service and runs the interactive CLI loop.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        PrefixMatchingService matcherService;

        // --- 1. Initialization ---
        try {
            // Retrieve the single, fully initialized instance of the matching service (Singleton)
            matcherService = PrefixMatchingService.getInstance();
        } catch (Exception e) {
            // Fatal error during configuration or service initialization. Log the error and exit the application.
            log.error("Failed to initialize {}", e.getMessage());
            return;
        }

        // Display interface instructions to the user
        System.out.println("Prefix Matcher Ready.");
        System.out.println("Single match: type a string");
        System.out.println("Batch match: comma-separated values (e.g., foo,bar,baz)");
        System.out.println("Type '" + EXIT_CMD + "' to exit.");

        // --- 2. Interactive Loop ---
        while (true) {

            System.out.print("> ");
            String input = scanner.nextLine().trim();

            // Check for termination command
            if (input.equalsIgnoreCase(EXIT_CMD)) {
                break;
            }

            // Batch mode: Activated if the input contains a comma
            if (input.contains(",")) {
                // Split input by comma, clean up spaces, filter empty strings, and collect into a Set
                Set<String> stringsToMatch = Arrays.stream(input.split(","))
                        .map(String::trim)
                        .filter(str -> !str.isEmpty())
                        .collect(Collectors.toSet());

                if (stringsToMatch.isEmpty()) {
                    log.warn("No valid strings provided for matching.");
                    continue;
                }

                log.debug("Processing batch request: {}", stringsToMatch);

                // Delegate batch matching to the service
                Map<String, String> results = matcherService.matchAll(stringsToMatch);

                // Output batch results
                System.out.println("Batch Results:");
                results.forEach((str, match) ->
                        System.out.println("  " + str + " → " + match));

                continue;
            }

            // Single string mode
            if (input.isEmpty()) {
                log.warn("Empty input. Please provide a valid string.");
                continue;
            }

            log.debug("Processing single request: {}", input);

            // Delegate single matching to the service
            String result = matcherService.matchSingle(input);

            // Output single result
            System.out.println("Result:");
            System.out.println("  " + input + " → " + result);
        }

        // --- 3. Shutdown ---
        // Call the service's shutdown method to gracefully terminate internal resources (e.g., ExecutorService).
        matcherService.shutdown();
        log.info("Application terminated. Goodbye!");
    }
}
