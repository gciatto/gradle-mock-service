@file:Suppress("OPT_IN_USAGE")

import de.aaschmid.gradle.plugins.cpd.Cpd
import dev.detekt.gradle.Detekt
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlin.text.startsWith

val kotlinVersion =
    extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")
        .findVersion("kotlin")
        .get()
        .requiredVersion

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-gradle-plugin`
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.jacoco.testkit)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
}

/*
 * Project information
 */
group = "io.github.gciatto"
description = "A Gradle plugin for starting mock services as Gradle tasks"

class ProjectInfo {
    val longName = "Gradle Plugin for Mock Service"
    val website = "https://github.com/gciatto/$name"
    val vcsUrl = "$website.git"
    val scm = "scm:git:$website.git"
    val pluginImplementationClass = "$group.gradle.mock.MockServicePlugin" // io.github.gciatto.gradle.mock
    val tags = listOf("mock", "service", "web")
}
val info = ProjectInfo()

gitSemVer {
    buildMetadataSeparator.set("-")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

val jvmVersion =
    libs.versions.jvm
        .map { JavaVersion.toVersion(it) }
        .getOrElse(JavaVersion.VERSION_11)

java {
    targetCompatibility = jvmVersion
    sourceCompatibility = jvmVersion
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    api(kotlin("stdlib-jdk8"))
    implementation(libs.kotlin.bom)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.dokka)
    implementation(libs.ktlint)
    implementation(libs.detekt)
    implementation(libs.publishOnCentral)
    implementation(libs.shadowJar)
    implementation(libs.npmPublish)
    testImplementation(gradleTestKit())
    testImplementation(libs.konf.yaml)
    testImplementation(libs.classgraph)
    testImplementation(libs.bundles.kotlin.testing)
    testImplementation("org.junit.jupiter:junit-jupiter-migrationsupport")
    api(libs.javalin)
}

// Enforce Kotlin version coherence
configurations.matching { "detekt" !in it.name }.all {
    val configuration = this
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin")) {
            useVersion(kotlinVersion)
            val artifact = "${requested.group}:${requested.name}"
            because("Force version $version for $artifact in configuration ${configuration.name}")
        }
    }
}

kotlin {
    target {
        compilerOptions {
            allWarningsAsErrors = true
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

inline fun <reified T : Task> Project.disableTrackStateOnWindows() {
    tasks.withType<T>().configureEach {
        doNotTrackState("Windows is a mess and JaCoCo does not work correctly")
    }
}

if (Os.isFamily(Os.FAMILY_WINDOWS)) {
    disableTrackStateOnWindows<Test>()
    disableTrackStateOnWindows<JacocoReport>()
}

tasks.withType<Test> {
    useJUnitPlatform()
    dependsOn(tasks.generateJacocoTestKitProperties)
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(
            *TestLogEvent.entries.toTypedArray(),
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

detekt {
    config.from(".detekt-config.yml")
    buildUponDefaultConfig = true
}

/*
 * Publication on Maven Central and the Plugin portal
*/
publishOnCentral {
    projectLongName.set(info.longName)
    projectDescription.set(description ?: TODO("Missing description"))
    projectUrl.set(info.website)
    scmConnection.set(info.scm)
    repository("https://maven.pkg.github.com/gciatto/${rootProject.name}".lowercase(), name = "github") {
        user.set("gciatto")
        password.set(System.getenv("GITHUB_TOKEN"))
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Giovanni Ciatto")
                        email.set("giovanni.ciatto@gmail.com")
                        url.set("https://www.about.me/gciatto")
                    }
                }
            }
        }
    }
}

// java-gradle-plugin creates a 'pluginMaven' publication with the same coordinates as the 'OSSRH'
// publication created by publishOnCentral. Prevent pluginMaven from being published to the
// ProjectLocal repository (which is used to create the Maven Central zip) to avoid the
// "will overwrite each other" warning.
tasks.matching { it.name == "publishPluginMavenPublicationToProjectLocalRepository" }.configureEach {
    enabled = false
}

signing {
    if (System.getenv()["CI"].equals("true", ignoreCase = true)) {
        val signingKey: String? = findProperty("signingKey")?.toString()
        val signingPassword: String? = findProperty("signingPassword")?.toString()
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}

gradlePlugin {
    plugins {
        website.set(info.website)
        vcsUrl.set(info.vcsUrl)
        create("") {
            id = "$group.${project.name}"
            displayName = info.longName
            description = project.description
            implementationClass = info.pluginImplementationClass
            tags.set(info.tags)
        }
    }
}

tasks.named("check") {
    dependsOn(
        tasks.withType<Detekt>().matching { it.name.endsWith("Main") || it.name.endsWith("Test") },
    )
}
