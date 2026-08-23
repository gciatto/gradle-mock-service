# Gradle Mock Service Plugin

A Gradle plugin for starting an embedded HTTP mock service as part of your Gradle build.  
Use it to spin up a lightweight HTTP server before integration or functional tests, define custom routes (with [Javalin](https://javalin.io/)), and tear the server down automatically once the tests finish.

## Features

- Starts/stops an embedded Javalin HTTP server as Gradle tasks
- Configurable port (default: `8080`)
- DSL for declaring HTTP routes directly in `build.gradle.kts`
- Automatically wraps existing Gradle tasks (e.g. `test`) so the server starts before them and stops after
- Requests are logged via the Gradle lifecycle logger

## Requirements

- JVM 11+
- Gradle 7+

## Apply the plugin

### Using the Gradle Plugin Portal

In your project's `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.gciatto.gradle-mock-service") version "<version>"
}
```

> Replace `<version>` with the latest release available on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.gciatto.gradle-mock-service).

## Configuration

The plugin adds a `mockService` extension to your project.  
Configure it in your `build.gradle.kts`:

```kotlin
mockService {
    // Optional: set the port (default is 8080)
    port = 8080

    // Wrap one or more tasks so the mock server is started before them
    // and stopped after them automatically
    wrapTasks("test")

    // Declare HTTP routes using the Javalin DSL
    routes {
        get("/hello") { ctx -> ctx.result("hello") }
        post("/data") { ctx ->
            val body = ctx.body()
            ctx.result("Received: $body")
        }
    }
}
```

### Available DSL properties and methods

| Member | Type | Default | Description |
|---|---|---|---|
| `port` | `Int` | `8080` | The port the HTTP server listens on. Must be set before the server starts. |
| `wrapTasks(name, ...)` | function | — | Makes the named tasks depend on `startMock` and be finalized by `stopMock`. |
| `routes { ... }` | function | — | Configures Javalin routes through the `JavalinDefaultRoutingApi`. |

## Available tasks

After applying the plugin the following tasks are available under the **Mocking** group:

| Task | Description |
|---|---|
| `startMock` | Starts the embedded HTTP server. |
| `stopMock` | Stops the embedded HTTP server (automatically depends on `startMock`). |

You can invoke them manually:

```bash
./gradlew startMock        # start the server
./gradlew stopMock         # start (if not running) then stop the server
```

When you use `wrapTasks("test")`, running `./gradlew test` will automatically start the server before tests and stop it afterwards.

## Complete example

`build.gradle.kts`:

```kotlin
plugins {
    id("io.github.gciatto.gradle-mock-service") version "<version>"
    java
}

mockService {
    port = 8080
    wrapTasks("test")
    routes {
        get("/hello") { ctx -> ctx.result("hello") }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

repositories {
    mavenCentral()
}
```

`src/test/java/example/TestService.java`:

```java
package example;

import org.junit.Test;
import java.io.*;
import java.net.*;
import static org.junit.Assert.assertEquals;

public class TestService {
    @Test
    public void test() throws IOException {
        var url = new URL("http://localhost:8080/hello");
        try (var reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            assertEquals("hello", reader.readLine());
        }
    }
}
```

Running `./gradlew test` will:

1. Start the mock server on port `8080`
2. Execute the JUnit tests (which can make real HTTP requests to `localhost:8080`)
3. Stop the mock server

## License

This project is distributed under the Apache 2.0 License. See [LICENSE](LICENSE) for details.
