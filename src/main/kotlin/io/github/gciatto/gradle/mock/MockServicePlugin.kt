package io.github.gciatto.gradle.mock

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.create

/**
 * Mocking service plugin.
 */
class MockServicePlugin : Plugin<Project> {
    @Suppress("ObjectLiteralToLambda")
    override fun apply(project: Project) {
        val extension = project.extensions.create<MockServiceExtension>("mockService", project)
        project.tasks.register(MockServiceExtension.TASK_NAME_START_MOCK) {
            it.doFirst {
                extension.start()
                project.logger.lifecycle("Mock service listening on port ${extension.port}")
            }
            it.configureMockServiceTask()
        }
        project.tasks.register(MockServiceExtension.TASK_NAME_STOP_MOCK) {
            it.dependsOn(MockServiceExtension.TASK_NAME_START_MOCK)
            it.doLast {
                extension.stop()
                project.logger.lifecycle("Mock service stopped")
            }
            it.configureMockServiceTask()
        }
    }

    private fun Task.configureMockServiceTask() {
        outputs.upToDateWhen { false }
        group = "Mocking"
    }
}
