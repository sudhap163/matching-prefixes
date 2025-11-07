package org.truecaller.prefixmatcher;

import org.truecaller.models.trie.TrieNode;

import java.util.List;

/**
 * Implements the PrefixMatcher interface using a Trie (Prefix Tree) for efficient lookup.
 * This is the default and preferred implementation for performance.
 */
public class TriePrefixMatcher implements PrefixMatcher {
    private final TrieNode root;

    public TriePrefixMatcher() {
        this.root = new TrieNode();
    }

    /**
     * Helper method to insert a single prefix.
     */
    private void insert(String prefix) {
        TrieNode current = root;
        for (char ch : prefix.toCharArray()) {
            current = current.getChildren().computeIfAbsent(ch, k -> new TrieNode());
        }
        current.setEndOfPrefix(true);
    }

    /**
     * Loads prefixes from a file, required by the PrefixMatcher interface.
     * @param prefixes - All the prefixes.
     */
    @Override
    public void loadPrefixes(List<String> prefixes) {
        for (String prefix : prefixes) {
            insert(prefix);
        }
    }

    /**
     * Finds the longest matching prefix using Trie traversal.
     * This is a read-only operation and supports concurrency.
     */
    @Override
    public String findLongestMatchingPrefix(String inputString) {
        TrieNode current = root;
        String longestMatch = "";
        StringBuilder currentPrefix = new StringBuilder();

        for (char ch : inputString.toCharArray()) {
            TrieNode nextNode = current.getChildren().get(ch);

            if (nextNode == null) {
                break;
            }

            current = nextNode;
            currentPrefix.append(ch);

            if (current.isEndOfPrefix()) {
                longestMatch = currentPrefix.toString();
            }
        }
        return longestMatch;
    }
}
