package org.truecaller.models.trie;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single node in the Trie structure.
 */
public class TrieNode {

    // Map to store children nodes, keyed by the character leading to them.
    private final Map<Character, TrieNode> children;

    // Flag to mark if the path leading to this node forms a complete prefix.
    private boolean isEndOfPrefix;

    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfPrefix = false;
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isEndOfPrefix() {
        return isEndOfPrefix;
    }

    public void setEndOfPrefix(boolean endOfPrefix) {
        isEndOfPrefix = endOfPrefix;
    }
}
