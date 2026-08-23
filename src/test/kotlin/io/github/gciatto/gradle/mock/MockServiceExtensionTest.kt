package io.github.gciatto.gradle.mock

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder

class MockServiceExtensionTest :
    StringSpec(
        {
            fun mockExtension(): MockServiceExtension {
                val project = ProjectBuilder.builder().build()
                return MockServiceExtension(project)
            }

            "default port should be ${MockServiceExtension.DEFAULT_PORT}" {
                val ext = mockExtension()
                ext.port shouldBe MockServiceExtension.DEFAULT_PORT
            }

            "port can be changed before start" {
                val ext = mockExtension()
                ext.port = 9090
                ext.port shouldBe 9090
            }

            "start and stop succeed normally" {
                val ext = mockExtension()
                ext.port = 0
                ext.routes { get("/ping") { it.result("pong") } }
                ext.start()
                ext.stop()
            }

            "stop throws if not started" {
                val ext = mockExtension()
                shouldThrow<IllegalArgumentException> {
                    ext.stop()
                }
            }

            "start throws if already started" {
                val ext = mockExtension()
                ext.port = 0
                ext.start()
                try {
                    shouldThrow<IllegalStateException> {
                        ext.start()
                    }
                } finally {
                    ext.stop()
                }
            }

            "setting port after start throws" {
                val ext = mockExtension()
                ext.port = 0
                ext.start()
                try {
                    shouldThrow<IllegalStateException> {
                        ext.port = 9090
                    }
                } finally {
                    ext.stop()
                }
            }

            "configuring routes after start throws" {
                val ext = mockExtension()
                ext.port = 0
                ext.start()
                try {
                    shouldThrow<IllegalStateException> {
                        ext.routes { get("/x") { it.result("x") } }
                    }
                } finally {
                    ext.stop()
                }
            }
        },
    )
