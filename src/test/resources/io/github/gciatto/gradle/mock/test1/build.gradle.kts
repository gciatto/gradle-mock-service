plugins {
    id("io.github.gciatto.gradle-mock-service")
    java
}

mockService {
    port = 9090
    wrapTasks("test")
    routes {
        get("/greet") { it.result("hi") }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

repositories {
    mavenCentral()
}
