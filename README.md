# Prefix Matching Service: High-Performance and Scalable

## Introduction

The **Prefix Matching Service** is a high-performance, modular Java application designed for the core task of **efficiently finding the longest available prefix** for any given input string(s).

The system prioritizes **scalability** and **reliability** by using proven design patterns, configurable initialization, and concurrent processing:

* **Performance:** Achieved via the **Trie (Prefix Tree)** data structure, ensuring near $O(L)$ lookup time complexity, where $L$ is the length of the input string.
* **Scalability:** Achieved by leveraging **multithreading** (ExecutorService) for concurrent batch processing and initializing resources (like the Trie and the Executor) based on configuration parameters (e.g., number of available CPU cores).
* **Best Practices:** The code strictly adheres to good coding practices, including **Singletons** for resource management, **modular design**, robust **logging (`@Slf4j`)**, and clear separation of concerns.

The Command Line Interface (CLI) is provided as an integration sample, demonstrating both single-string and highly concurrent batch matching capabilities.

---

## Architectural Design and Scalability

The architecture is built on five core components, ensuring clear responsibilities and easy maintenance.

| Component | Design Pattern / Focus | Scalability & Practices | Description |
| :--- | :--- | :--- | :--- |
| **`ConfigManager`** | **Singleton** (Initialization-on-demand) | **Thread-Safe Resource Loading** | Loads and centralizes application configuration (`application.yml`) only once, preventing race conditions and redundant I/O. |
| **`PrefixLoader`** | **Utility Class (Modular)** | **Modular, I/O Separation** | Handles robust file I/O using Java Streams and `try-with-resources`, ensuring clean data processing and separation from business logic. |
| **`PrefixMatchingService`** | **Singleton + Factory Pattern** | **Concurrency & Configurable Initialization** | Manages the **ExecutorService** initialized based on CPU cores (`Runtime.getRuntime().availableProcessors()`) for scalable, parallel batch processing. |
| **`PrefixMatcher`** | **Interface (Abstraction)** | **Modular, Open for Extension** | Defines the contract for matching, decoupling the service from the specific data structure used (e.g., `PrefixTrie`). |
| **`PrefixTrie`** | **Data Structure** | **Performance & Efficiency** | Implements the fast $O(L)$ longest prefix lookup algorithm. |

---

## Setup and Execution

### Prerequisites

* Java Development Kit (JDK) **8+** (Recommended: JDK 17 or higher)
* Gradle (for building)

### 1. Project Build

Clone the repository and build the executable JAR:

```bash
git clone https://github.com/sudhap163/matching-prefixes
cd matching-prefixes
./gradlew clean build
```

### 2. Configuration (`application.yml`)

The application's runtime behavior is configured via `src/main/resources/application.yml`. All service instances are initialized based on these config variables.

```bash
# application.yml
matcher:
  # Path to the file containing one prefix per line
  prefixFile: "/Users/XYZ/Downloads/matching-prefixes/src/main/resources/application.yml"
  
  # The strategy implementation to use (currently only "TRIE" is supported)
  strategy: "TRIE"
```

### 3. Prefix Data File

The file specified in `prefixFile` must exist. The system handles prefix extraction, cleaning (trimming/filtering), and then builds the Trie data structure during service initialization.

### 4. Running the Application (CLI Mode)

Execute the compiled JAR file. The initialization process (Config loading, Trie building, Executor creation) happens transparently upon the first call to `PrefixMatchingService.getInstance()`.

```bash
java -jar target/prefix-matching-service-1.0.0.jar
```

## How to Run (CLI Mode)

This section details how to execute the application and start the interactive Command Line Interface.

### Running the Application

Execute the compiled JAR file located in the `build/libs/` directory. The initialization process (Config loading, Trie building, Executor creation) happens transparently upon the first call to `PrefixMatchingService.getInstance()`.

```bash
# Execute the application JAR file
java -jar build/libs/matching-prefixes-1.0.0.jar
```

The application will start in interactive CLI mode:

```
Prefix Matcher Ready.
Single match: type a string
Batch match: comma-separated values (e.g., foo,bar,baz)
Type 'exit' to exit.
>
```

## CLI Usage and Demonstration

The interactive interface demonstrates the service capabilities.

### 1. Single Match Mode (Synchronous)

Input a single string for immediate lookup.

```
Prefix Matcher Ready.
...
> prefixbatchtest
Result:
  prefixbatchtest → PrefixA
```

### 2. Batch Match Mode (Concurrent and Scalable)

Input multiple strings separated by a comma. The `matchAll()` method delegates each input to the ExecutorService, utilizing multiple threads to maximize throughput.

```
> apple, Banana, anOtherstring, query_1
Processing batch: [Banana, apple, anOtherstring, query_1]
Batch Results:
  Banana → preFixB
  apple → PrefixA
  query_1 → No matching prefix found
  anOtherstring → Another
```

## Application Integration Snippet

To utilize the service within your application, retrieve the single `PrefixMatchingService` instance and call the necessary methods. `shutdown()` method needs to be called when your application exits.

```
import java.util.Set;
import java.util.Map;

public class AppIntegration {
    
    public static void main(String[] args) {
        // 1. Get the single, initialized instance
        PrefixMatchingService service = PrefixMatchingService.getInstance();

        // 2. Define the inputs
        Set<String> stringsToFind = Set.of(
            "test_prefix_input", "another_query"
        );

        try {
            // 3. Call the scalable, concurrent batch matching method
            Map<String, String> results = service.matchAll(stringsToFind);

            System.out.println("Integration Results:");
            results.forEach((input, match) -> 
                System.out.println("Input: " + input + ", Match: " + match));
            
        } catch (RuntimeException e) {
            // Handle initialization or execution errors
            System.err.println("Matching failed: " + e.getMessage());
        } finally {
            // 4. Always ensure resource cleanup on exit
            service.shutdown();
        }
    }
}
```

## Graceful Shutdown and Resource Management

The application ensures all internal resources are cleanly released upon exit.

When the user types `exit`, the `PrefixMatchingService.shutdown()` method is called.

 - This method initiates a graceful shutdown of the internal `ExecutorService`.

 - The system waits up to 60 seconds (configurable) for active matching tasks to complete.

 - If the timeout is reached, the Executor is forcefully shut down (`shutdownNow()`), preventing thread leaks and ensuring a reliable application exit.
