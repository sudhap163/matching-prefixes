package org.truecaller.prefixmatcher.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class responsible for reading prefix data from a file into a List of Strings.
 * This class uses static methods and standard Java I/O streams for file handling.
 */
public class PrefixLoader {

    /**
     * Loads prefixes from the file specified by the given path.
     * This method is designed to be called during application startup (initialization phase).
     *
     * @param filePath The absolute or relative path to the file containing prefixes.
     * @return A List of cleaned (trimmed and non-empty) prefix strings.
     * @throws IOException If the file is not found, cannot be read, or an I/O error occurs.
     */
    public static List<String> loadPrefixesFromConfiguredFile(String filePath) throws IOException {

        // Use try-with-resources to ensure the BufferedReader and FileReader are automatically closed.
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            // Read all lines from the file and process them as a Stream
            return br.lines()
                    // Remove leading/trailing whitespace from each line
                    .map(String::trim)
                    // Filter out any lines that are empty after trimming
                    .filter(s -> !s.isEmpty())
                    // Collect the resulting valid prefixes into a List
                    .collect(Collectors.toList());
        }
    }
}
