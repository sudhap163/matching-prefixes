package org.truecaller.prefixmatcher.models.trie;

public class PrefixTrie {

    private final TrieNode root = new TrieNode();

    public void insert(String prefix) {
        TrieNode current = root;

        for (char ch : prefix.toCharArray()) {
            if (!Character.isLetterOrDigit(ch)) continue;

            ch = Character.toLowerCase(ch); // normalize
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
        }
        current.isEndOfPrefix = true;
    }

    public String findLongestPrefix(String input) {
        TrieNode current = root;
        StringBuilder matched = new StringBuilder();
        String longestMatch = null;

        for (char ch : input.toCharArray()) {
            if (!Character.isLetterOrDigit(ch)) break;

            ch = Character.toLowerCase(ch);
            TrieNode next = current.children.get(ch);
            if (next == null) break;

            matched.append(ch);
            current = next;

            if (current.isEndOfPrefix) {
                longestMatch = matched.toString();
            }
        }
        return longestMatch;
    }
}

