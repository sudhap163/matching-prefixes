package org.truecaller.prefixmatcher.service;

import org.truecaller.prefixmatcher.models.trie.PrefixTrie;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PrefixMatcherServiceUsingTrie implements PrefixMatching {

    private final PrefixTrie trie;
    private final ExecutorService executor;

    public PrefixMatcherServiceUsingTrie(PrefixTrie trie, int threadCount) {
        this.trie = trie;
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    public String matchSingle(String input) {
        return trie.findLongestPrefix(input);
    }

    @Override
    public Map<String, String> matchAll(Set<String> inputs) {
        List<Callable<Map.Entry<String, String>>> tasks = inputs.stream()
                .map(input -> (Callable<Map.Entry<String, String>>) () -> {
                    String match = trie.findLongestPrefix(input);
                    return new AbstractMap.SimpleEntry<>(input, match != null ? match : "No match");
                })
                .toList();

        try {
            return executor.invokeAll(tasks).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Matching interrupted", e);
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
