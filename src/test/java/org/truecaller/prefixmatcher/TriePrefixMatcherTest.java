package org.truecaller.prefixmatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TriePrefixMatcher implementation of the PrefixMatcher interface.
 */
class TriePrefixMatcherTest {

    private PrefixMatcher matcher;

    // Prefixes used for testing: a, app, apple, application, bat, batter, tru, true, foo
    private static final List<String> SAMPLE_PREFIXES = Arrays.asList(
            "a", "app", "apple", "application",
            "bat", "batter",
            "tru", "true",
            "foo"
    );

    @BeforeEach
    void setUp() {
        // Initialize a fresh matcher for each test
        matcher = new TriePrefixMatcher();
        // Load the sample prefixes
        matcher.loadPrefixes(SAMPLE_PREFIXES);
    }

    // --- 1. Core Functionality Tests ---

    @Test
    @DisplayName("T1: Should return the longest matching prefix among multiple choices")
    void testLongestMatchAmongMultiple() {
        // 'application' is the longest match, beating 'app' and 'apple'
        assertEquals("application", matcher.findLongestMatchingPrefix("application_server"));

        // 'true' is the longest match, beating 'tru'
        assertEquals("true", matcher.findLongestMatchingPrefix("truecaller_id"));

        // 'batter' is the longest match, beating 'bat'
        assertEquals("batter", matcher.findLongestMatchingPrefix("batter_up_baseball"));
    }

    @Test
    @DisplayName("T2: Should return an exact match when input string equals a prefix")
    void testExactMatch() {
        assertEquals("apple", matcher.findLongestMatchingPrefix("apple"));
        assertEquals("foo", matcher.findLongestMatchingPrefix("foo"));
        assertEquals("a", matcher.findLongestMatchingPrefix("a"));
    }

    @Test
    @DisplayName("T3: Should return a shorter match when a longer one is only partial")
    void testShorterMatchFound() {
        // Input: "truc" stops after 'tru' because 'c' is not a child of 'tru' node.
        assertEquals("tru", matcher.findLongestMatchingPrefix("truc"));

        // Input: "appli" stops after 'app', because the next character 'l' does not exist in 'apple'/'application' path.
        // Wait, "appli" matches 'app', then 'l' is the next character.
        // If 'app' is a prefix, the search continues.
        // If 'appl' is NOT a prefix, and 'app' IS a prefix, then 'app' is the longest match *so far*.
        // Since 'apple' exists, the path should be 'a' -> 'p' -> 'p' -> 'l' -> 'e'.
        // Input: "applx"
        // Traverses: a (prefix) -> p -> p (prefix) -> l (not end) -> x (break).
        // The last valid prefix was 'app'.
        assertEquals("app", matcher.findLongestMatchingPrefix("applx_test"));
    }

    // --- 2. Edge Case Tests ---

    @Test
    @DisplayName("E1: Should return empty string for no match")
    void testNoMatch() {
        // Input starts with a character not in the Trie
        assertEquals("", matcher.findLongestMatchingPrefix("zebra"));
        // Input is short but path is not a prefix
        assertEquals("", matcher.findLongestMatchingPrefix("t"));
    }

    @Test
    @DisplayName("E2: Should return empty string for empty input")
    void testEmptyInputString() {
        assertEquals("", matcher.findLongestMatchingPrefix(""));
    }

    @Test
    @DisplayName("E3: Should return empty string if input is shorter than any prefix")
    void testInputShorterThanAnyPrefix() {
        // No prefix starts with 'fo'
        assertEquals("", matcher.findLongestMatchingPrefix("fo"));
        // Only 'a' is a prefix, not 'b'
        assertEquals("", matcher.findLongestMatchingPrefix("b"));
    }

    @Test
    @DisplayName("E4: Should handle single character prefixes correctly")
    void testSingleCharacterPrefix() {
        assertEquals("a", matcher.findLongestMatchingPrefix("a_longer_string"));
    }

    // --- 3. Trie State & Initialization Tests ---

    @Test
    @DisplayName("S1: Should handle an empty list of prefixes")
    void testLoadEmptyPrefixes() {
        PrefixMatcher emptyMatcher = new TriePrefixMatcher();
        emptyMatcher.loadPrefixes(Collections.emptyList());

        // Verify no match can be found
        assertEquals("", emptyMatcher.findLongestMatchingPrefix("test"));
    }

    @Test
    @DisplayName("S2: Should correctly handle prefixes that are subsets of each other (nesting)")
    void testNestedPrefixes() {
        // Test ensures 'app' is not mistaken for 'apple' on "appl..."
        assertEquals("app", matcher.findLongestMatchingPrefix("appliances")); // 'appl' is not a prefix, 'app' is
        assertEquals("a", matcher.findLongestMatchingPrefix("antelope"));
        assertEquals("apple", matcher.findLongestMatchingPrefix("applepie"));
    }

    @Test
    @DisplayName("S3: Should find the longest match even when it's the full input string")
    void testFullInputMatch() {
        assertEquals("batter", matcher.findLongestMatchingPrefix("batter"));
        assertEquals("application", matcher.findLongestMatchingPrefix("application"));
    }

    // --- 4. Concurrency (Simulated Read-Only Safety) Test ---

    @Test
    @DisplayName("C1: Should verify read-only safety under concurrent access (simulation)")
    void testConcurrentReadOnlySafety() throws InterruptedException {
        // The test ensures the results remain consistent when accessed by multiple threads
        // since the find operation is read-only.

        final int NUM_THREADS = 10;
        final String INPUT_STRING = "truecaller_concurrent_test";
        final String EXPECTED_RESULT = "true";

        Thread[] threads = new Thread[NUM_THREADS];
        String[] results = new String[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                // Accessing the shared, immutable-after-init Trie
                results[index] = matcher.findLongestMatchingPrefix(INPUT_STRING);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }

        // Verify all results are consistent
        for (String result : results) {
            assertEquals(EXPECTED_RESULT, result, "Concurrent reads produced an inconsistent result.");
        }
    }
}
