package org.truecaller.prefixmatcher.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.truecaller.prefixmatcher.config.AppConfig;
import org.truecaller.prefixmatcher.config.ConfigManager;
import org.truecaller.prefixmatcher.util.PrefixLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrefixMatchingServiceTest {

    // Mocking ConfigManager and PrefixLoader static methods
    private MockedStatic<ConfigManager> mockedConfigManager;
    private MockedStatic<PrefixLoader> mockedPrefixLoader;

    private AppConfig mockAppConfig;
    private ConfigManager mockConfigManagerInstance;

    @BeforeEach
    void setUp() throws IOException {
        // Setup Mocks
        mockAppConfig = new AppConfig();
        AppConfig.Matcher mockMatcher = new AppConfig.Matcher();
        mockMatcher.setStrategy("TRIE");
        mockMatcher.setPrefixFile("mock/path/prefixes.txt");
        mockAppConfig.setMatcher(mockMatcher);

        mockConfigManagerInstance = mock(ConfigManager.class);
        when(mockConfigManagerInstance.getConfig()).thenReturn(mockAppConfig);

        // Mock static calls
        mockedConfigManager = mockStatic(ConfigManager.class);
        mockedConfigManager.when(ConfigManager::getInstance).thenReturn(mockConfigManagerInstance);

        mockedPrefixLoader = mockStatic(PrefixLoader.class);
        // Define mock behavior for the PrefixLoader
        List<String> mockPrefixes = Arrays.asList("foo", "bar", "foobar");
        mockedPrefixLoader.when(() -> PrefixLoader.loadPrefixesFromConfiguredFile(anyString()))
                .thenReturn(mockPrefixes);
    }

    @AfterEach
    void tearDown() {
        // Close static mocks to prevent pollution
        if (mockedConfigManager != null) {
            mockedConfigManager.close();
        }
        if (mockedPrefixLoader != null) {
            mockedPrefixLoader.close();
        }
        // Ensure the Singleton state is reset or handled (often required for Singletons in tests)
        // Since the Singleton relies on the Initialization-on-demand Holder idiom, it's hard to reset.
        // For production code, rely on the fact that getInstance() is called only once per test run/VM.
    }

    // --- Test Initialization and Singleton ---

    @Test
    void testSingletonInitialization() {
        PrefixMatchingService instance1 = PrefixMatchingService.getInstance();
        PrefixMatchingService instance2 = PrefixMatchingService.getInstance();

        assertNotNull(instance1, "Singleton instance should not be null.");
        assertSame(instance1, instance2, "Both calls should return the exact same instance (Singleton).");

        // Verify configuration was loaded exactly once
        mockedConfigManager.verify(ConfigManager::getInstance, times(1));
        verify(mockConfigManagerInstance, times(1)).getConfig();
    }

    @Test
    void testInitializationFailure_IOException() throws IOException {
        // Force PrefixLoader to throw IOException
        mockedPrefixLoader.when(() -> PrefixLoader.loadPrefixesFromConfiguredFile(anyString()))
                .thenThrow(new IOException("Test IO Failure"));

        // Expect initialization to fail and wrap the IOException in a RuntimeException
        assertThrows(RuntimeException.class,
                PrefixMatchingService::getInstance,
                "Initialization should fail with RuntimeException on IO error.");
    }

    @Test
    void testInitializationFailure_InvalidStrategy() {
        // Force invalid strategy config
        mockAppConfig.getMatcher().setStrategy("INVALID_MATCHER");

        // Expect initialization to fail due to invalid strategy
        assertThrows(RuntimeException.class,
                PrefixMatchingService::getInstance,
                "Initialization should fail with RuntimeException on invalid strategy.");
    }

    // --- Test Functionality and Concurrency ---

    @Test
    void testMatchSingle() {
        PrefixMatchingService service = PrefixMatchingService.getInstance();

        // Mock data: prefixes = ["foo", "bar", "foobar"] (all lowercase)

        // Success: Exact case match
        assertEquals("foobar", service.matchSingle("foobarred"),
                "Should find the longest match 'foobar' (all lowercase).");

        // Failure: Case mismatch
        assertNull(service.matchSingle("Foo"),
                "Should return null because the input 'F' does not match stored 'f'.");
        assertNull(service.matchSingle("FOOBARRED"),
                "Should return null because the input is uppercase, but stored is lowercase.");
    }

    @Test
    void testMatchAll_ConcurrentExecution() {
        PrefixMatchingService service = PrefixMatchingService.getInstance();

        // Inputs are mixed case to test case sensitivity
        Set<String> inputs = Set.of("foo_input", "BAR_input", "Foobarred", "nomatch");

        Map<String, String> results = service.matchAll(inputs);

        assertEquals(4, results.size(), "Should return results for all inputs.");

        // Successful matches (input 'foo' is lowercase, matches prefix 'foo')
        assertEquals("foo", results.get("foo_input"), "Should find 'foo'.");

        // Failed matches due to case mismatch ('BAR' != 'bar' and 'F' != 'f')
        assertEquals("No matching prefix found", results.get("BAR_input"),
                "Should fail: 'B' does not match 'b'.");
        assertEquals("No matching prefix found", results.get("Foobarred"),
                "Should fail: 'F' does not match 'f'.");
        assertEquals("No matching prefix found", results.get("nomatch"),
                "Should return the defined 'No matching prefix found' message.");
    }

}
