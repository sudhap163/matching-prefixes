package org.truecaller.prefixmatcher.models.trie;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single node within the PrefixTrie data structure.
 * Each node holds references to its children and a flag indicating if it marks the end of a valid prefix.
 *
 */
public class TrieNode {

    /**
     * A map storing links to child nodes.
     * The key is the character that the edge represents, and the value is the next TrieNode in the sequence.
     * This map is the mechanism that allows traversal through the Trie based on the input string's characters.
     */
    Map<Character, TrieNode> children = new HashMap<>();

    /**
     * A boolean flag indicating whether the path from the root to this node constitutes a complete, valid prefix.
     * This is essential for distinguishing between an intermediate path and a stored prefix.
     */
    boolean isEndOfPrefix;
}

