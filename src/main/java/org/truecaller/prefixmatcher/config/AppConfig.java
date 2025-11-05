package org.truecaller.prefixmatcher.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class AppConfig {
    @Data
    public static class Matcher {
        @Getter @Setter
        private String prefixFile;
        @Getter @Setter
        private String strategy;
    }
    @Getter @Setter
    private Matcher matcher;
}
