plugins {
    id("io.github.gciatto.gradle-mock-service")
    java
}

mockService {
    port = 8082
    wrapTasks("test")
    routes {
        get("/ping") { it.result("pong") }
        post("/echo") { it.result(it.body()) }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

repositories {
    mavenCentral()
}
