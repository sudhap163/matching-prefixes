package org.truecaller.prefixmatcher.io;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class PrefixLoader {

    public static List<String> loadPrefixesFromConfiguredFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            return br.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }
}

