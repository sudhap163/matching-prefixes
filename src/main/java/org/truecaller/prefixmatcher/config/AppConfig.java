package org.truecaller.prefixmatcher.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Top-level application configuration holder.
 * Typically loaded from a configuration file (e.g., application.properties/yaml).
 */
@Data
public class AppConfig {

    /**
     * Configuration group for the prefix matching service.
     */
    @Data
    public static class Matcher {

        /** File path containing the list of prefixes. */
        @Getter @Setter
        private String prefixFile;

        /** Strategy used for matching (e.g., "TRIE"). */
        @Getter @Setter
        private String strategy;
    }

    /** Configuration instance for the prefix matcher component. */
    @Getter @Setter
    private Matcher matcher;
}
