package org.truecaller.prefixmatcher.io;

import org.truecaller.prefixmatcher.models.trie.PrefixTrie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PrefixLoader {

    public static PrefixTrie loadFromConfiguredFile(String filePath) throws IOException {
        PrefixTrie trie = new PrefixTrie();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    trie.insert(line);
                }
            }
        }
        return trie;
    }
}

