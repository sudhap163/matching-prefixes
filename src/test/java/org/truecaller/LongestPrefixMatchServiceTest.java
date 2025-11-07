package org.truecaller;

import org.junit.jupiter.api.*;
import org.truecaller.prefixmatcher.MatcherApproach;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LongestPrefixMatchServiceTest {

    private LongestPrefixMatchService service;

    @BeforeEach
    void setup() {
        // Using realistic alphanumeric prefixes like SKU patterns, service labels, routing tags.
        List<String> prefixes = List.of(
                "AB",
                "ABC",
                "ABCD",
                "XYZ",
                "XY",
                "USER",
                "USER123",
                "PRD-",
                "PRD-ALPHA",
                "APP_V1",
                "APP"
        );

        service = new LongestPrefixMatchService(prefixes, MatcherApproach.TRIE);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    @DisplayName("matchSingleString returns the *longest* prefix when multiple match")
    void testMatchSingleString_LongestPrefix() {
        String result = service.matchSingleString("ABCD999");
        assertEquals("ABCD", result);
    }

    @Test
    @DisplayName("matchSingleString returns exact match when input equals a prefix")
    void testMatchSingleString_ExactMatch() {
        assertEquals("XYZ", service.matchSingleString("XYZ"));
    }

    @Test
    @DisplayName("matchSingleString returns shorter prefix when longer one does not match fully")
    void testMatchSingleString_PartialButNotLongest() {
        assertEquals("ABC", service.matchSingleString("ABC9Z"));
    }

    @Test
    @DisplayName("matchSingleString returns empty string when no prefix applies")
    void testMatchSingleString_NoMatch() {
        assertEquals("", service.matchSingleString("NOPE123"));
    }

    @Test
    @DisplayName("matchSingleString throws NullPointerException on null input")
    void testMatchSingleString_NullInput() {
        assertThrows(NullPointerException.class, () -> service.matchSingleString(null));
    }

    @Test
    @DisplayName("matchConcurrentStrings resolves multiple matches correctly in parallel")
    void testMatchConcurrentStrings_Batch() {
        List<String> inputs = List.of(
                "ABCD1000",     // → ABCD
                "AB999",        // → AB
                "USER123XYZ",   // → USER123
                "USERZZZ",      // → USER
                "PRD-ALPHA99",  // → PRD-ALPHA
                "PRD-BETA",     // → PRD-
                "APP_V1_BUILD", // → APP_V1
                "APPV2"         // → APP (partial prefix match)
        );

        Map<String, String> result = service.matchConcurrentStrings(inputs);

        assertEquals("ABCD", result.get("ABCD1000"));
        assertEquals("AB", result.get("AB999"));
        assertEquals("USER123", result.get("USER123XYZ"));
        assertEquals("USER", result.get("USERZZZ"));
        assertEquals("PRD-ALPHA", result.get("PRD-ALPHA99"));
        assertEquals("PRD-", result.get("PRD-BETA"));
        assertEquals("APP_V1", result.get("APP_V1_BUILD"));
        assertEquals("APP", result.get("APPV2"));
    }

    @Test
    @DisplayName("matchConcurrentStrings returns empty map for empty input list")
    void testMatchConcurrentStrings_EmptyInputList() {
        assertTrue(service.matchConcurrentStrings(List.of()).isEmpty());
    }

    @Test
    @DisplayName("matchConcurrentStrings handles large input set consistently in parallel")
    void testMatchConcurrentStrings_LargeInput() {
        List<String> large = java.util.stream.IntStream.range(0, 200)
                .mapToObj(i -> "USER123_" + i)
                .toList();

        Map<String, String> result = service.matchConcurrentStrings(large);

        large.forEach(key -> assertEquals("USER123", result.get(key)));
    }

    @Test
    @DisplayName("shutdown gracefully stops executor without exceptions")
    void testShutdown() {
        assertDoesNotThrow(() -> service.shutdown());
    }

    @Test
    @DisplayName("constructor correctly loads prefixes at initialization")
    void testConstructorLoadsPrefixes() {
        LongestPrefixMatchService svc = new LongestPrefixMatchService(
                List.of("AAA", "AAAA", "AAAAA"), MatcherApproach.TRIE
        );
        assertEquals("AAAAA", svc.matchSingleString("AAAAAX"));
        svc.shutdown();
    }
}
