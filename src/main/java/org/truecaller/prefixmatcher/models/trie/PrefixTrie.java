package org.truecaller.prefixmatcher.models.trie;

import java.util.List;

/**
 * Implements the core Trie (Prefix Tree) data structure logic.
 * This class is responsible for building the tree from a list of prefixes and
 * efficiently finding the longest matching prefix for any given input string.
 */
public class PrefixTrie {

    /** The starting point of the Trie structure, an empty node representing the conceptual start of all prefixes. */
    private final TrieNode root = new TrieNode();

    /**
     * Constructs the Trie by inserting a list of prefixes.
     * During insertion, each character traverses an edge, and the final node is marked as the end of a prefix.
     *
     * @param prefixes The list of strings to be inserted into the Trie structure.
     */
    public void buildTrie(List<String> prefixes) {
        //

        for (String prefix : prefixes) {
            TrieNode current = root;

            for (char ch : prefix.toCharArray()) {
                // Ignore non-alphanumeric characters for clean data structure
                if (!Character.isLetterOrDigit(ch)) continue;

//                ch = Character.toLowerCase(ch); // Normalize to lowercase for case-insensitive matching

                // If the character node doesn't exist, create a new one
                current.children.putIfAbsent(ch, new TrieNode());

                // Move down the tree
                current = current.children.get(ch);
            }
            // Mark the final node reached as the end of a valid prefix
            current.isEndOfPrefix = true;
        }
    }

    /**
     * Traverses the Trie using the input string's characters to find the longest stored prefix that matches
     * the beginning of the input string.
     *
     * @param input The string to search within.
     * @return The longest prefix found in the Trie that matches the beginning of the input, or null.
     */
    public String findLongestPrefix(String input) {

        TrieNode current = root;
        StringBuilder matched = new StringBuilder();
        String longestMatch = null;

        for (char ch : input.toCharArray()) {
            // Stop traversal if a non-alphanumeric character is encountered
            if (!Character.isLetterOrDigit(ch)) break;

            // Normalize the character (must match the normalization used in buildTrie)
//            ch = Character.toLowerCase(ch);

            // Attempt to move to the next node based on the current character
            TrieNode next = current.children.get(ch);

            // If the character path doesn't exist, we can't find a longer match, so stop
            if (next == null) break;

            // Append the matched character and move to the next node
            matched.append(ch);
            current = next;

            // If the current node marks the end of a prefix, record it as the longest match found so far
            if (current.isEndOfPrefix) {
                longestMatch = matched.toString();
            }
        }
        return longestMatch;
    }
}
