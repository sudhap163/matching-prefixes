package org.truecaller.prefixmatcher.service;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrefixMatcherUsingTrieTest {

    @Test
    void testMatcherDelegation() {
        // No need for a mock, as PrefixTrie is a simple POJO class without external dependencies
        List<String> prefixes = List.of("test", "testing");
        PrefixMatcherUsingTrie matcher = new PrefixMatcherUsingTrie(prefixes);

        // Verifying the delegation logic by using a known expected result
        // If the constructor works, the matchPrefix result is correct
        assertEquals("testing", matcher.matchPrefix("testing123"));
        assertEquals("test", matcher.matchPrefix("testxyz"));
    }
}
