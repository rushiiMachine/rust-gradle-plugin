package dev.rushii.rgp

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnnecessaryAbstractClass")
internal abstract class RustPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val extension = project.extensions.create("rust", RustConfigExtension::class.java, project)

		// Wait until extension has been configured
		project.afterEvaluate {
			// Wait until all `afterEvaluate` have run to wait for any config changes to perform
			project.afterEvaluate {
				registerBuildTasks(project, extension)
			}
		}
	}

	private fun registerBuildTasks(project: Project, extension: RustConfigExtension) {
		// Task to build all targets of all projects
		val buildAllTask = project.tasks.maybeCreate("cargoBuildAll").apply {
			group = TASK_GROUP
			description = "Build all targets for all Cargo projects"
		}

		for (cargoProject in extension.cargoProjects) {
			cargoProject.targets.disallowChanges()

			// Task to build all targets of this specific project
			val buildAllProjectTask = project.tasks.maybeCreate("cargoBuildAll-${cargoProject.name.get()}").apply {
				group = TASK_GROUP
				description = "Build all targets for the ${cargoProject.name.get()} Cargo project"
			}

			// Register build tasks for every target
			for (target in cargoProject.targets.get()) {
				// Register a build task for this specific project+target
				val buildTaskName = "cargoBuild-${cargoProject.name.get()}-${target}"
				val buildTask = project.tasks.maybeCreate(buildTaskName, CargoBuildTask::class.java).apply {
					this.cargoProject.set(cargoProject)
					this.target.set(target)
				}

				// Link this individual build task to the combined build tasks
				buildAllTask.dependsOn(buildTask)
				buildAllProjectTask.dependsOn(buildTask)
			}
		}
		}
	}

	internal companion object {
		const val TASK_GROUP = "rust"
	}
}
