plugins {
    id("io.github.gciatto.gradle-mock-service")
    java
}

mockService {
    port = 8083
    wrapTasks("test")
    routes {
        get("/status") { it.result("ok") }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

repositories {
    mavenCentral()
}
