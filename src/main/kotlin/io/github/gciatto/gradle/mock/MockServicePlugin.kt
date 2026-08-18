package io.github.gciatto.gradle.mock

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/**
 * Mocking service plugin.
 */
class MockServicePlugin : Plugin<Project> {
    @Suppress("ObjectLiteralToLambda")
    override fun apply(project: Project) {
        project.tasks.register("startMock") { start ->
            project.tasks.register("stopMock") { stop ->
                stop.dependsOn(start)
                listOf(start, stop).forEach {
                    it.outputs.upToDateWhen { false }
                    it.group = "Mocking"
                }
                val extension = project.extensions.create<MockServiceExtension>("mockService", project, start, stop)
                start.doFirst(
                    object : Action<Any> {
                        override fun execute(t: Any) {
                            extension.start()
                            project.logger.lifecycle("Mock service listening on port ${extension.port}")
                        }
                    },
                )
                stop.doLast(
                    object : Action<Any> {
                        override fun execute(t: Any) {
                            extension.stop()
                            project.logger.lifecycle("Mock service stopped")
                        }
                    },
                )
            }
        }
    }
}
