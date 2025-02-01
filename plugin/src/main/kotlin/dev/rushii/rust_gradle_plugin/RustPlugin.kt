package dev.rushii.rust_gradle_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnnecessaryAbstractClass")
internal abstract class RustPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val extension = project.extensions.create("rust", RustConfigExtension::class.java, project)

//		project.tasks.register(TASK_NAME, CargoBuildTask::class.java) {
//			it.tag.set(extension.tag)
//			it.message.set(extension.message)
//			it.outputFile.set(extension.outputFile)
//		}
	}

	internal companion object {
		const val TASK_GROUP = "rust"
	}
}
