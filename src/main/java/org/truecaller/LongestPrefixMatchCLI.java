package org.truecaller;

import org.truecaller.prefixmatcher.MatcherApproach;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The Command-Line Interface (CLI) application.
 * It acts as a pure client, delegating all business logic to the LongestPrefixMatchService.
 */
public class LongestPrefixMatchCLI {

    private static LongestPrefixMatchService service;

    private static void displayHelp() {
        System.out.println("\n--- Available Commands ---");
        System.out.println("**[string]** : Match a single string (e.g., truecaller).");
        System.out.println("**concurrent [s1,s2,s3]**: Match multiple strings concurrently (comma-separated, e.g., concurrent [applepie, foobar]).");
        System.out.println("**help** : Show this help message.");
        System.out.println("**exit** : Quit the application.");
        System.out.println("--------------------------");
    }

    private static void processInput(String input) {
        if (input.equalsIgnoreCase("exit")) {
            System.out.println("Exiting the application. Goodbye!");
            service.shutdown();
            System.exit(0);
        }

        if (input.equalsIgnoreCase("help")) {
            displayHelp();
            return;
        }

        if (input.toLowerCase().contains(",")) {
            String listString = input.trim();

            List<String> inputs = Arrays.stream(listString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (inputs.isEmpty()) {
                System.out.println("Error: Please provide strings for concurrent match.");
                return;
            }

            System.out.printf("Requesting concurrent match for **%d** strings from service...\n", inputs.size());
            // Receive a map of Input -> Match
            Map<String, String> results = service.matchConcurrentStrings(inputs);

            // --- CONCURRENT RESULT FORMATTING ---
            System.out.println("\n--- ⚡ Concurrent Results (Input -> Match) ---");


            // Determine column widths
            int maxInputLen = results.keySet().stream().mapToInt(String::length).max().orElse(10);
            int maxMatchLen = results.values().stream().mapToInt(String::length).max().orElse(10);

            int inputWidth = Math.max(20, maxInputLen + 2);
            int matchWidth = Math.max(20, maxMatchLen + 2);

            String headerFormat = String.format("%%-%ds | %%-%ds\n", inputWidth, matchWidth);
            String separator = "-".repeat(inputWidth + matchWidth + 3);

            System.out.printf(headerFormat, "Input String", "Matched Prefix");
            System.out.println(separator);

            // Print results
            results.forEach((inputStr, matchStr) -> {
                String formattedMatch = matchStr.isEmpty() ? "<none>" : matchStr;
                System.out.printf(headerFormat, "'" + inputStr + "'", "'" + formattedMatch + "'");
            });
            System.out.println(separator);

        } else if (!input.isEmpty()) {
            // --- SINGLE RESULT FORMATTING ---
            String result = service.matchSingleString(input);
            String match = result.isEmpty() ? "**<none>**" : "**'" + result + "'**";
            System.out.printf("   Single Match Result for **'%s'**: Longest prefix found is %s\n", input, match);
        }
    }

    public static void main(String[] args) {
        // 1. Setup Data
        List<String> samplePrefixes = Arrays.asList("foo", "tru", "true", "apple", "app", "a", "mobile");

        // 2. Initialize Service Layer with the DEFAULT approach (Trie)
        // To use another approach (if implemented)
        // service = new LongestPrefixMatchService(MatcherApproach.LINEAR_SCAN);
        service = new LongestPrefixMatchService(samplePrefixes, MatcherApproach.TRIE);

        // 3. Start CLI Loop
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- Longest Prefix Match CLI Mode ---");
        displayHelp();

        while (true) {
            System.out.print("\ncli > ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            processInput(input);
        }
    }
}
