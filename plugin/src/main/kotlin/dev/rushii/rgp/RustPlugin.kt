package dev.rushii.rgp

import dev.rushii.rgp.toolchains.AndroidNdkInfo
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnnecessaryAbstractClass")
internal abstract class RustPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val extension = project.extensions.create("rust", RustConfigExtension::class.java, project)

		// Wait until extension has been configured
		project.afterEvaluate {
			// Obtain the Android NDK if any project has an Android target
			val androidNdk = when (extension.cargoProjects.any { it.hasAndroidTargets() }) {
				false -> null
				true -> project.extensions.getAndroid().getNdkInfo()
			}

			// Wait until all `afterEvaluate` have completed to wait for any `afterEvaluate` user config changes
			project.afterEvaluate {
				configurePlugin(project, extension, androidNdk)
				registerTasks(project, extension, androidNdk)
			}
		}
	}

	private fun configurePlugin(
		project: Project,
		extension: RustConfigExtension,
		androidNdk: AndroidNdkInfo?,
	) {
		for (cargoProject in extension.cargoProjects) {
			// Configure for Android if it builds for an Android target
			if (cargoProject.hasAndroidTargets()) {
				configurePluginAndroid(project, cargoProject, androidNdk!!)
			}

			// Lock these properties since their values are used at configuration time
			cargoProject.targets.disallowChanges()
			cargoProject.android.apiLevel.disallowChanges()
			cargoProject.android.usePrebuiltToolchain.disallowChanges()
		}
	}

	/**
	 * Verifies the user-set properties in [AndroidDeclaration]
	 * and populates any unset properties with default values obtained at runtime.
	 */
	private fun configurePluginAndroid(
		project: Project,
		cargoProject: CargoProjectDeclaration,
		ndk: AndroidNdkInfo,
	) {
		val androidExt = project.extensions.getAndroid()
		val androidConfig = cargoProject.android

		// NDK versions below 19 do not have prebuilt toolchains
		if (androidConfig.usePrebuiltToolchain.getOrElse(false) && ndk.versionMajor < 19) {
			throw GradleException("usePrebuiltToolchain=true requires NDK version 19+")
		}

		// Configure whether the project should use prebuilt toolchains if it has not already done so
		if (!androidConfig.usePrebuiltToolchain.isPresent)
			androidConfig.usePrebuiltToolchain.set(ndk.versionMajor >= 19)

		// Fill in AGP minSdkVersion as apiLevel for project
		if (!androidConfig.apiLevel.isPresent) {
			val minSdkVersion = androidExt.defaultConfig.minSdkVersion?.apiLevel
				?: throw GradleException("Neither apiLevel or minSdkVersion is configured for the cargo project ${cargoProject.name}")

			androidConfig.apiLevel.set(minSdkVersion)
		}
	}

	private fun registerTasks(
		project: Project,
		extension: RustConfigExtension,
		androidNdk: AndroidNdkInfo?,
	) {
		// Task to build all targets of all projects
		val buildAllTask = project.tasks.maybeCreate("cargoBuildAll").apply {
			group = TASK_GROUP
			description = "Build all targets for all Cargo projects"
		}

		for (cargoProject in extension.cargoProjects) {
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
					this.androidNdk.set(androidNdk)
					this.target.set(target)
				}

				// Link this individual build task to the combined build tasks
				buildAllTask.dependsOn(buildTask)
				buildAllProjectTask.dependsOn(buildTask)
			}
		}
	}

	internal companion object {
		const val TASK_GROUP = "rust"
	}
}
