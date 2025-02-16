package dev.rushii.rgp

import dev.rushii.rgp.config.RustExtension
import org.gradle.api.DefaultTask
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import java.io.File

class RustPluginTest {
	@TempDir
	lateinit var projectDir: File

	@Test
	fun `plugin is applied correctly to the project`() {
		newProject().afterEvaluate { project ->
			assertTrue(project.plugins.hasPlugin(RustPlugin::class.java))
			assertInstanceOf<RustExtension>(project.extensions.findByName(RustPlugin.EXTENSION_NAME))
			assertInstanceOf<DefaultTask>(project.tasks.findByName("cargoBuildAll"))
		}
	}

	@Test
	fun `project builds with no rust config`() {
		assertDoesNotThrow { executeGradleRun(projectDir, "", "tasks") }
	}
}
