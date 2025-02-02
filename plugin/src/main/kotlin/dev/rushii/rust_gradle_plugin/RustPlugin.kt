package dev.rushii.rust_gradle_plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnnecessaryAbstractClass")
internal abstract class RustPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val extension = project.extensions.create("rust", RustConfigExtension::class.java, project)

		project.afterEvaluate { registerBuildTasks(project, extension) }
	}

	private fun registerBuildTasks(project: Project, extension: RustConfigExtension) {
		val buildAllTask = project.tasks.maybeCreate("cargoBuildAll", DefaultTask::class.java).apply {
			group = TASK_GROUP
			description = "Build all registered targets"
		}

		for (cargoProject in extension.cargoProjects) {
			for (target in cargoProject.targets.get()) {
				val taskName = "cargoBuild-${cargoProject.name.get()}-${target}"
				val buildTask = project.tasks.maybeCreate(taskName, CargoBuildTask::class.java).apply {
					this.cargoProject.set(cargoProject)
					this.target.set(target)
				}

				buildAllTask.dependsOn(buildTask)
			}
		}
	}

	internal companion object {
		const val TASK_GROUP = "rust"
	}
}
