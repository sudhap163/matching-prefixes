package org.truecaller.prefixmatcher.service;

import lombok.extern.slf4j.Slf4j;
import org.truecaller.prefixmatcher.models.trie.PrefixTrie;

import java.util.List;

/**
 * Concrete implementation of the PrefixMatcher interface that uses a Trie data structure
 * (PrefixTrie) for efficient storage and lookup of prefixes.
 * This class is responsible for initializing the Trie and delegating matching operations to it.
 */
@Slf4j
public class PrefixMatcherUsingTrie implements PrefixMatcher {

    /** The internal Trie data structure used to store prefixes for fast lookups. */
    private final PrefixTrie trie;

    /**
     * Constructor. Initializes and builds the internal Trie based on the provided list of prefixes.
     * This setup is a key initialization step performed during service startup.
     *
     * @param prefixes The list of strings (prefixes) to be loaded into the Trie.
     */
    public PrefixMatcherUsingTrie(List<String> prefixes) {

        this.trie = new PrefixTrie();
        this.trie.buildTrie(prefixes);
    }

    /**
     * Finds the longest prefix in the internal Trie that matches the beginning of the input string.
     * This method implements the core requirement of the PrefixMatcher interface.
     *
     * @param input The string to search against the stored prefixes.
     * @return The longest matching prefix found in the Trie, or null if no match is found.
     */
    @Override
    public String matchPrefix(String input) {
        // Delegate the actual search logic to the Trie implementation
        return trie.findLongestPrefix(input);
    }
}
