package io.github.gciatto.gradle.mock

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.flow.FlowScope
import org.gradle.kotlin.dsl.create
import javax.inject.Inject

/**
 * Mocking service plugin.
 */
class MockServicePlugin
    @Inject
    constructor(
        private val flowScope: FlowScope,
    ) : Plugin<Project> {
        @Suppress("ObjectLiteralToLambda")
        override fun apply(project: Project) {
            val extension = project.extensions.create<MockServiceExtension>("mockService", project)
            if (project == project.rootProject) {
                StopMockBuildAction.extension = extension
            }
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
            flowScope.always(StopMockBuildAction::class.java) {}
        }

        private fun Task.configureMockServiceTask() {
            outputs.upToDateWhen { false }
            group = "Mocking"
        }
    }

class StopMockBuildAction : FlowAction<FlowParameters.None> {
    companion object {
        @Volatile
        var extension: MockServiceExtension? = null
    }

    override fun execute(parameters: FlowParameters.None) {
        extension?.stopIfStarted()
    }
}
