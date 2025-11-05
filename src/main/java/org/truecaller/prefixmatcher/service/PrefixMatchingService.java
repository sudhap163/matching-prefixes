package org.truecaller.prefixmatcher.service;

import lombok.extern.slf4j.Slf4j;
import org.truecaller.prefixmatcher.config.AppConfig;
import org.truecaller.prefixmatcher.config.ConfigManager;
import org.truecaller.prefixmatcher.util.PrefixLoader;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Singleton service responsible for initializing, managing, and executing prefix matching operations.
 * It uses the Initialization-on-demand holder idiom for thread-safe, lazy initialization.
 * This service manages an internal ExecutorService for concurrent 'matchAll' processing.
 */
@Slf4j
public class PrefixMatchingService {

    private final int EXECUTOR_SERVICE_GRACE_FUL_SHUTDOWN_DURATION_IN_SECONDS = 60;
    private final String TRIE_BASED_MATCHING_STRATEGY = "TRIE";

    /** The immutable, underlying prefix matching data structure (e.g., Trie). */
    private final PrefixMatcher prefixMatcher;

    /** Executor service used for managing concurrent batch matching tasks. */
    private final ExecutorService executor;

    // Initialization-on-demand holder idiom for thread-safe lazy loading
    private static class SingletonHelper {
        // The instance is initialized only when getInstance() is called
        private static final PrefixMatchingService INSTANCE = new PrefixMatchingService();
    }

    /**
     * Private constructor to enforce Singleton pattern.
     * This method performs configuration loading, data structure instantiation, and executor setup.
     */
    private PrefixMatchingService() {

        try {
            // Get configuration details once from the ConfigManager singleton
            AppConfig config = ConfigManager.getInstance().getConfig();
            String filePath = config.getMatcher().getPrefixFile();
            String strategy = config.getMatcher().getStrategy().toUpperCase();

            log.info("PrefixMatcher Initializing...");
            log.debug("File path: {}", filePath);
            log.debug("Strategy : {}", strategy);

            // Load prefixes from the configured file (may throw IOException)
            List<String> prefixes = PrefixLoader.loadPrefixesFromConfiguredFile(filePath);

            log.info("PrefixMatcher successfully initialized with {} prefixes.", prefixes.size());

            // Instantiate the correct matcher implementation based on the configuration strategy
            switch (strategy) {
                case TRIE_BASED_MATCHING_STRATEGY:
                    prefixMatcher = new PrefixMatcherUsingTrie(prefixes);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid matching strategy: " + strategy);
            }

            // Initialize the executor service using a fixed pool size based on available CPU cores
            this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        } catch (IOException e) {
            // Convert checked exception to unchecked RuntimeException for cleaner Singleton usage
            throw new RuntimeException("Failed to initialize PrefixMatchingService due to file loading error.", e);
        } catch (IllegalArgumentException e) {
            // Handle invalid configuration strategy cleanly
            throw new RuntimeException("Configuration Error: " + e.getMessage(), e);
        }
    }

    /**
     * Global access point to get the single instance of the class.
     * Accessing this method triggers the initialization defined in the private constructor.
     * @return The single instance of PrefixMatchingService.
     */
    public static PrefixMatchingService getInstance() {
        return SingletonHelper.INSTANCE;
    }

    // --- Public Functionality Methods ---

    /**
     * Matches a batch of input strings concurrently using the internal ExecutorService.
     * Each string is processed as a separate Callable task.
     * @param stringsToMatch A set of strings to find matching prefixes for.
     * @return A map where keys are the input strings and values are the matched prefixes (or a default message).
     */
    public Map<String, String> matchAll(Set<String> stringsToMatch) {

        // Convert each input string into a Callable task for concurrent execution
        List<Callable<Map.Entry<String, String>>> tasks = stringsToMatch.stream()
                .map(input -> (Callable<Map.Entry<String, String>>) () -> {
                    String match = prefixMatcher.matchPrefix(input);
                    return new AbstractMap.SimpleEntry<>(input, match != null ? match : "No matching prefix found");
                })
                .toList();
        //

        try {
            // Execute all tasks and wait for them to complete
            return executor.invokeAll(tasks).stream()
                    .map(future -> {
                        try {
                            // Get the result from the Future, blocking if necessary
                            return future.get();
                        } catch (Exception e) {
                            // Wrap and rethrow any exceptions that occurred during task execution
                            throw new RuntimeException(e);
                        }
                    })
                    // Collect results into the final map format
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        } catch (InterruptedException e) {
            // Clean up if the thread waiting for results is interrupted
            Thread.currentThread().interrupt();
            throw new RuntimeException("Matching interrupted", e);
        }
    }

    /**
     * Matches a single input string against the prefix data structure.
     * This is a synchronous call directly to the underlying matcher.
     * @param stringToMatch The single string to check for a prefix match.
     * @return The longest matching prefix found, or null if no match exists.
     */
    public String matchSingle(String stringToMatch) {
        return prefixMatcher.matchPrefix(stringToMatch);
    }

    // --- Lifecycle Management Method ---

    /**
     * Shuts down the internal ExecutorService gracefully.
     * It initiates an orderly shutdown and waits for a fixed period for termination.
     * If termination is not achieved within the timeout, it forces an immediate shutdown.
     */
    public void shutdown() {
        log.info("PrefixMatchingService Executor shutdown initiated. Attempting graceful termination...");

        // 1. Initiate orderly shutdown: stops accepting new tasks.
        this.executor.shutdown();

        try {
            // 2. Wait for currently executing tasks to finish within a timeout (e.g., 60 seconds).
            if (!this.executor.awaitTermination(EXECUTOR_SERVICE_GRACE_FUL_SHUTDOWN_DURATION_IN_SECONDS, TimeUnit.SECONDS)) {

                log.warn("Executor did not terminate gracefully within 60 seconds. Forcing shutdown now.");

                // 3. Force shutdown: interrupts all currently executing tasks.
                List<Runnable> outstandingTasks = this.executor.shutdownNow();
                log.warn("{} tasks were forcefully stopped.", outstandingTasks.size());

                // 4. Wait a short time for the forcefully interrupted tasks to acknowledge and stop.
                if (!this.executor.awaitTermination(EXECUTOR_SERVICE_GRACE_FUL_SHUTDOWN_DURATION_IN_SECONDS, TimeUnit.SECONDS)) {
                    // Log an error if the service still hasn't shut down, though rare.
                    log.error("Executor service failed to terminate completely.");
                }
            }
        } catch (InterruptedException ie) {
            // 5. Handle interruption during waiting: force shutdown immediately.
            log.warn("Shutdown process was interrupted. Forcing immediate shutdown.");
            this.executor.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }

        log.info("PrefixMatchingService Executor shut down successfully.");
    }
}
