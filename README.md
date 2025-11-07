# Longest Prefix Match Service

This project provides a high-performance, concurrent service for finding the **longest matching prefix** for a given input string or a list of input strings.  
The core algorithm relies on a highly optimized **Trie (Prefix Tree)** implementation.

---

## Salient Features

### 1. High-Performance Matching via Trie
- Uses a specialized **TriePrefixMatcher** implementation.
- Prefix lookup runs in **O(L)** time, where *L* is the length of the input string.
- Lookup speed is **independent** of the total number of loaded prefixes.

---

### 2. Built-in Concurrency Management
- Supports concurrent matching for large batches via `matchConcurrentStrings`.
- Uses a **Fixed Thread Pool** (`THREAD_POOL_SIZE = 4`) to manage CPU efficiently.
- Uses `ExecutorService.invokeAll()` to process tasks in parallel and wait for results.
- Greatly reduces processing time for batch workloads.

---

### 3. Immediate Initialization and Readiness
- Prefixes are fully loaded into the Trie **at construction time**.
- Ensures the service is immediately ready for requests.
- Keeps the CLI / caller decoupled from data-loading complexity.

---

### 4. Graceful Resource Shutdown
- Provides a robust `shutdown()` method to cleanly terminate internal threads.
- Attempts graceful termination first; forces shutdown if needed.

---

## Good Practices and Architectural Decisions

### 1. Separation of Concerns
| Component | Responsibility |
|----------|----------------|
| **LongestPrefixMatchService** | Orchestration, concurrency, logging, executor lifecycle |
| **PrefixMatcher / TriePrefixMatcher** | Data structure + longest-prefix search algorithm |

This separation enables easier testing and future extensibility.

---

### 2. Dependency Inversion & Factory Pattern
- The service depends on the **PrefixMatcher interface**, not the concrete Trie implementation.
- `createMatcherInstance()` encapsulates instantiation.
- Allows introducing new matcher strategies (e.g., Hash-based or Aho-Corasick) without changes to the service API.

---

### 3. Robust Concurrency Handling
- Uses `Executors.newFixedThreadPool(4)` to avoid resource exhaustion.
- Uses `Callable` + `Future` to retrieve match results cleanly.
- Exception handling is done safely with:
    - Catching `ExecutionException`
    - Logging root causes
    - `Thread.currentThread().interrupt()` to propagate interruption correctly

---

### 4. Proper Resource Lifecycle Management
Shutdown workflow:
```java
executor.shutdown();
executor.awaitTermination(5, TimeUnit.SECONDS);
executor.shutdownNow();
```

---

### 5. Structured Logging

The service uses `@Slf4j` to provide standardized and consistent logging for:

- Initialization events
- Prefix loading operations
- Service shutdown events
- Debug and diagnostic output for troubleshooting

---

## Future Improvements & Roadmap

### 1. Architectural Improvements
- Introduce **constructor-based dependency injection** for `PrefixMatcher` and `ExecutorService`.
- Move prefix loading out of the constructor into an explicit `init()` method to enable staged initialization.

### 2. Performance & Concurrency Enhancements
- Replace blocking `invokeAll` with **CompletableFuture** for **non-blocking parallel execution**.
- Make thread pool size configurable (via configuration property or adapt to `Runtime.getRuntime().availableProcessors()`).

### 3. Data Structure & Flexibility Enhancements
- Support **dynamic prefix updates**, such as:
    - `addPrefix(String prefix)`
    - `removePrefix(String prefix)`
- Add additional prefix-matching implementations, such as **Aho-Corasick**, for scenarios requiring streaming or multi-pattern search.

