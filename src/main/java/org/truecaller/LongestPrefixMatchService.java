package org.truecaller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import lombok.extern.slf4j.Slf4j;
import org.truecaller.prefixmatcher.MatcherApproach;
import org.truecaller.prefixmatcher.PrefixMatcher;
import org.truecaller.prefixmatcher.TriePrefixMatcher;

/**
 * Service layer responsible for orchestrating the longest prefix matching.
 * It manages the PrefixTrie instance and handles both single and concurrent lookups.
 */
@Slf4j
public class LongestPrefixMatchService {
    private final PrefixMatcher matcher;
    private final ExecutorService executor;
    private static final int THREAD_POOL_SIZE = 4;
    private static final int EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 5;

    // Design Choice Justification: The Service constructor now performs all necessary initialization
    // (Trie creation and data loading). The CLI only needs to instantiate the Service, making their
    // coupling minimal and the CLI's role strictly I/O.
    public LongestPrefixMatchService(List<String> prefixes, MatcherApproach matcherApproach) {
        this.matcher = createMatcherInstance(matcherApproach);
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Load data immediately upon instantiation
        log.debug("Service initializing: Loading prefixes into the Trie...");
        this.matcher.loadPrefixes(prefixes);
        log.info("Service initialized and ready.");
    }

    /**
     * Helper factory method to create the appropriate matcher instance.
     */
    private PrefixMatcher createMatcherInstance(MatcherApproach approach) {
        return switch (approach) {
            case TRIE -> new TriePrefixMatcher();
            default -> new TriePrefixMatcher(); // Default to Trie, for the scope of this project
        };
    }

    /**
     * Finds the longest matching prefix for a single input string.
     * @param inputString The string to search.
     * @return The longest matching prefix.
     */
    public String matchSingleString(String inputString) {
        return matcher.findLongestMatchingPrefix(inputString);
    }

    /**
     * Finds the longest matching prefix for a list of strings concurrently.
     * * @param inputStrings The list of strings to search.
     * @return A Map where the key is the input string and the value is the longest matching prefix.
     */
    public Map<String, String> matchConcurrentStrings(List<String> inputStrings) {
        List<Callable<Map.Entry<String, String>>> tasks = new ArrayList<>();

        // 1. Create a Callable task that returns a single Map Entry (Input -> Match)
        for (String input : inputStrings) {
            tasks.add(() -> {
                String match = matcher.findLongestMatchingPrefix(input);
                // Return a simple Map Entry for the input and its match
                return Map.entry(input, match);
            });
        }

        Map<String, String> resultsMap = new HashMap<>();
        try {
            // 2. Submit all tasks and wait for results
            List<Future<Map.Entry<String, String>>> futures = executor.invokeAll(tasks);

            // 3. Extract results and populate the final map
            for (Future<Map.Entry<String, String>> future : futures) {
                Map.Entry<String, String> entry = future.get();
                resultsMap.put(entry.getKey(), entry.getValue());
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Matching failed", e);
        }

        return resultsMap;
    }

    /**
     * Shuts down the thread pool gracefully when the application exits.
     */
    public void shutdown() {
        log.info("Service shutting down ExecutorService...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
