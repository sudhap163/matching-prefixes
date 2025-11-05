package org.truecaller.prefixmatcher.service;

/**
 * Core interface defining the contract for prefix matching functionality.
 * Any concrete class implementing this interface must provide a mechanism
 * to search for the longest prefix match within a given input string.
 */
public interface PrefixMatcher {

    /**
     * Searches for the longest prefix that matches the beginning of the input string
     * within the underlying data structure (e.g., a Trie or a Set).
     * * @param input The string to be analyzed for a prefix match.
     * @return The longest matching prefix found, or null if no prefix match is available.
     *
     */
    String matchPrefix(String input);
}
