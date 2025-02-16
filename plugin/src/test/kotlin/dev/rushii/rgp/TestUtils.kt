package dev.rushii.rgp

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

const val PLUGIN_ID = "dev.rushii.rust-gradle-plugin"

fun newProject(): Project = ProjectBuilder.builder()
	.build()
	.also { it.pluginManager.apply(PLUGIN_ID) }

fun executeGradleRun(dir: File, buildGradleKts: String, task: String): BuildResult {
	dir.resolve("build.gradle.kts").writeText(buildGradleKts)

	return GradleRunner.create()
		.withProjectDir(dir)
		.withArguments(task)
		.withPluginClasspath()
		.build()
}
