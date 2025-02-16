package dev.rushii.rgp

import dev.rushii.rgp.config.*
import dev.rushii.rgp.tasks.*
import dev.rushii.rgp.toolchains.AndroidNdkInfo
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.*
import org.gradle.api.tasks.Delete

@Suppress("UnnecessaryAbstractClass")
internal abstract class RustPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val extension = project.extensions.create(EXTENSION_NAME, RustExtension::class.java, project)

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
		extension: RustExtension,
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
		extension: RustExtension,
		androidNdk: AndroidNdkInfo?,
	) {
		// Task to build all targets of all projects
		val buildAllTask = project.tasks.maybeCreate("cargoBuildAll").apply {
			group = TASK_GROUP
			description = "Build all targets for all Cargo projects"
		}

		// Register a task to generate toolchains if any project doesn't use prebuilt Android toolchains
		val shouldMakeGenTask = extension.cargoProjects
			.any { it.hasAndroidTargets() && !it.android.usePrebuiltToolchain.get() }
		val generateAndroidToolchainsTask = when (shouldMakeGenTask) {
			false -> null
			true -> project.tasks.maybeCreate("generateAndroidToolchains", GenerateAndroidToolchainsTask::class.java).apply {
				this.androidNdk.set(androidNdk)
			}
		}

		// Register a task that extracts the Android linker wrapper scripts to be passed to Cargo
		val generateAndroidLinkerWrapperTask = when (extension.cargoProjects.any { it.hasAndroidTargets() }) {
			false -> null
			true -> project.tasks.maybeCreate("generateAndroidLinkerWrapper", GenerateAndroidLinkerWrapperTask::class.java)
		}

		for (cargoProject in extension.cargoProjects) {
			if (extension.integrateWithPlugins.get()) {
				// Delete Cargo build dir when running project clean
				project.tasks.maybeCreate("clean", Delete::class.java).apply {
					group = group ?: TASK_GROUP
					delete(cargoProject.absoluteProjectPath.get().resolve("target"))
				}
			}

			// Task to build all targets of this specific project
			val buildAllProjectTask = project.tasks.register("cargoBuildAll-${cargoProject.name.get()}") { task ->
				task.group = TASK_GROUP
				task.description = "Build all targets for the ${cargoProject.name.get()} Cargo project"
			}

			// Register build tasks for every target
			for (target in cargoProject.targets.get()) {
				// Register a build task for this specific project+target
				val buildTaskName = "cargoBuild-${cargoProject.name.get()}-${target}"
				val buildTask = project.tasks.register(buildTaskName, CargoBuildTask::class.java) { task ->
					task.cargoProject.set(cargoProject)
					task.androidNdk.set(androidNdk)
					task.target.set(target)
				}

				// Link this individual build task to the combined build tasks
				buildAllTask.dependsOn(buildTask)
				buildAllProjectTask.get().dependsOn(buildTask)

				if (cargoProject.hasAndroidTargets()) {
					buildTask.get().dependsOn(generateAndroidLinkerWrapperTask)

					// Make sure toolchains are generated for all tasks of projects that don't use prebuilt Android toolchains
					if (!cargoProject.android.usePrebuiltToolchain.get())
						buildTask.get().dependsOn(generateAndroidToolchainsTask)
				}
			}
		}

		// If this is an Android project, make sure AGP bundles our libs as JNI libs
		if (extension.integrateWithPlugins.get() && androidNdk != null) {
			val copyArtifactsTask = project.tasks.maybeCreate("copyRustAndroidArtifacts", CopyAndroidArtifactsTask::class.java)

			project.extensions.getAndroid()
				.sourceSets.getByName("main")
				.jniLibs.srcDir(copyArtifactsTask.outputs.files.singleFile)

			// TODO: is there a more reliable way to obtain all of these?
			for (name in arrayOf("mergeDebugJniLibFolders", "mergeReleaseJniLibFolders"))
				project.tasks.getByName(name).dependsOn(copyArtifactsTask)
		}
	}

	internal companion object {
		const val EXTENSION_NAME = "rust"
		const val TASK_GROUP = "rust"

		val IS_WINDOWS = Os.isFamily(Os.FAMILY_WINDOWS)
	}
}
