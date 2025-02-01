package dev.rushii.rust_gradle_plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

fun newProject(): Project {
	return ProjectBuilder.builder().build()
		.also { it.pluginManager.apply("dev.rushii.rust-gradle-plugin") }
}

class RustPluginTest {
	@JvmField
	@Rule
	var testProjectDir: TemporaryFolder = TemporaryFolder()

	@Test
	fun `plugin is applied correctly to the project`() {
		assert(newProject().extensions.getByName("rust") is RustConfigExtension)
	}

	@Test
	fun `parameters are passed correctly from extension to task`() {
		val project = ProjectBuilder.builder().build()
		project.pluginManager.apply("dev.rushii.rust-gradle-plugin")
		val aFile = File(project.projectDir, ".tmp")
		(project.extensions.getByName("templateExampleConfig") as RustConfigExtension).apply {
			tag.set("a-sample-tag")
			message.set("just-a-message")
			outputFile.set(aFile)
		}

		val task = project.tasks.getByName("templateExample") as CargoBuildTask

		assertEquals("a-sample-tag", task.tag.get())
		assertEquals("just-a-message", task.message.get())
		assertEquals(aFile, task.outputFile.get().asFile)
	}

	@Test
	fun `task generates file with message`() {
		val message = "Just trying this gradle plugin..."
		testProjectDir.root.removeRecursively()
		File(testProjectDir.root, "build.gradle")
			.writeText(
				generateBuildFile("message.set(\"$message\")\ntag.set(\"tag\")"),
			)

		val gradleResult = executeGradleRun("templateExample")
		assert(gradleResult.output.contains("message is: $message"))

		val generatedFileText = (testProjectDir.root.resolve("build/template-example.txt")).readText()
		assert(generatedFileText == "[tag] $message")
	}

	private fun executeGradleRun(task: String): BuildResult =
		GradleRunner
			.create()
			.withProjectDir(testProjectDir.root)
			.withArguments(task)
			.withPluginClasspath()
			.build()

	private fun generateBuildFile(config: String) =
		"""
        plugins {
            id 'dev.rushii.rust-gradle-plugin'
        }
        templateExampleConfig {
            $config
        }
        """.trimIndent()
}

private fun File.removeRecursively() =
	this
		.walkBottomUp()
		.filter { it != this }
		.forEach { it.deleteRecursively() }
