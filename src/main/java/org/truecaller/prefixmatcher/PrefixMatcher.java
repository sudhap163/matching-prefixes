package org.truecaller.prefixmatcher;

import java.util.List;

/**
 * Interface defining the contract for any prefix matching algorithm.
 * This adheres to the Dependency Inversion Principle (DIP).
 */
public interface PrefixMatcher {
    /**
     * Loads the configuration (list of prefixes) from a specified source.
     * @param prefixes List of actual prefixes.
     */
    void loadPrefixes(List<String> prefixes);

    /**
     * Finds the longest matching prefix for a given input string.
     * @param inputString The string to match against.
     * @return The longest matching prefix, or an empty string if none is found.
     */
    String findLongestMatchingPrefix(String inputString);
}
