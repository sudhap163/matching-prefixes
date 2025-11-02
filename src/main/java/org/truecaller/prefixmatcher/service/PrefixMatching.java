package org.truecaller.prefixmatcher.service;

import java.util.Map;
import java.util.Set;

public interface PrefixMatching {
    String matchSingle(String input);

    Map<String, String> matchAll(Set<String> inputs);
}
