package org.truecaller.prefixmatcher.models.trie;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PrefixTrieTest {

    // --- Test Data ---
    // Includes mixed case prefixes
    private final List<String> testPrefixes = Arrays.asList(
            "Apple", "app", "APIkey", "BAN", "banana", "123Code"
    );

    // --- Test Build Logic ---

    @Test
    void testBuildTrieAndFindExactMatch() {
        PrefixTrie trie = new PrefixTrie();
        trie.buildTrie(testPrefixes);

        // Success: Case matches exactly
        assertEquals("Apple", trie.findLongestPrefix("ApplePie"),
                "Should find 'Apple' when case matches.");

        // Failure: Case mismatch
        assertNull(trie.findLongestPrefix("applepie"),
                "Should return null for 'applepie' because it requires 'A'.");
        assertNull(trie.findLongestPrefix("Ban"),
                "Should return null for 'Ban' because the stored prefix is 'BAN'.");
    }

    @Test
    void testLongestMatchPreference() {
        PrefixTrie trie = new PrefixTrie();
        trie.buildTrie(Arrays.asList("a", "abc", "aBc"));

        // Match must follow the exact case path
        assertEquals("abc", trie.findLongestPrefix("abcd"),
                "Should find the longest path matching the case ('abc').");
        assertEquals("aBc", trie.findLongestPrefix("aBcd"),
                "Should find 'aBc'.");

        // Should fail because the case does not match the stored prefix 'a' or 'abc'
        assertNull(trie.findLongestPrefix("Abc"),
                "Should return null for case mismatch.");
    }

    // --- Test Normalization and Edge Cases ---

    @Test
    void testNonAlphanumericStop() {
        PrefixTrie trie = new PrefixTrie();
        // Stored prefix is "APIkey"
        trie.buildTrie(testPrefixes);

        // Input "API-KEY" should find "APIkey" (if the prefix was stored as "APIkey")
        // Since the current PrefixTrie implementation ignores non-alphanumeric chars during insertion:
        // Stored: A -> P -> I -> k -> e -> y (only 'y' is end of prefix)

        // Input: "API-KEY"
        // Traverses A, P, I. Stops at '-' because it is ignored in findLongestPrefix.
        // Wait, the original code in findLongestPrefix only breaks, it doesn't ignore.

        // Original logic for findLongestPrefix: if (!Character.isLetterOrDigit(ch)) break;

        // If the prefix is "APIkey" and input is "API-KEY":
        assertEquals("APIkey", trie.findLongestPrefix("APIkeyExtra"),
                "Should match the exact case path.");

        assertNull(trie.findLongestPrefix("API-KEY-123"),
                "Should return null because traversal stops at the first non-alphanumeric character ('-').");
    }

    @Test
    void testEmptyAndNoMatch() {
        PrefixTrie trie = new PrefixTrie();
        trie.buildTrie(testPrefixes);

        assertNull(trie.findLongestPrefix("zoo"),
                "Should return null for no match.");
        assertNull(trie.findLongestPrefix("aPple"),
                "Should return null due to case mismatch on the first character ('a' vs 'A').");
    }
}