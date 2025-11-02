package org.truecaller.prefixmatcher;

import lombok.extern.slf4j.Slf4j;
import org.truecaller.prefixmatcher.io.PrefixLoader;
import org.truecaller.prefixmatcher.service.PrefixMatcherServiceUsingTrie;
import org.truecaller.prefixmatcher.models.trie.PrefixTrie;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    private static final String EXIT_CMD = "exit";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter prefix file path: ");
        String filePath = scanner.nextLine().trim();

        PrefixTrie trie;
        try {
            trie = PrefixLoader.loadFromConfiguredFile(filePath);
        } catch (Exception e) {
            log.error("Failed to load prefixes from file '{}': {}", filePath, e.getMessage());
            return;
        }

        int threadCount = Runtime.getRuntime().availableProcessors();
        PrefixMatcherServiceUsingTrie matcherService = new PrefixMatcherServiceUsingTrie(trie, threadCount);

        System.out.println("Prefix Matcher Ready.");
        System.out.println("Single match: type a string");
        System.out.println("Batch match: comma-separated values (e.g., foo,bar,baz)");
        System.out.println("Type '" + EXIT_CMD + "' to exit.");

        while (true) {

            System.out.println("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase(EXIT_CMD)) {
                break;
            }

            // Batch mode
            if (input.contains(",")) {
                Set<String> stringsToMatch = Arrays.stream(input.split(","))
                        .map(String::trim)
                        .filter(str -> !str.isEmpty())
                        .collect(Collectors.toSet());

                if (stringsToMatch.isEmpty()) {
                    log.warn("No valid strings provided for matching.");
                    continue;
                }

                log.info("Processing batch: {}", stringsToMatch);

                Map<String, String> results = matcherService.matchAll(stringsToMatch);

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

            String result = matcherService.matchSingle(input);
            System.out.println("Result:");
            System.out.println("  " + input + " → " + result);
        }

        matcherService.shutdown();
        System.out.println("Application terminated. Goodbye!");
    }
}
