package dev.rushii.rust_gradle_plugin

import dev.rushii.rust_gradle_plugin.models.ToolchainTargets
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException

/**
 * Task that builds a Cargo project for a single target, with the config
 * specified by a [CargoProjectDeclaration], and a target.
 */
// TODO: @CacheableTask
public abstract class CargoBuildTask : DefaultTask() {
	init {
		group = RustPlugin.TASK_GROUP
		description = "Builds a specific target for a Cargo project"
	}

	/**
	 * This specifies the Cargo project that will be built.
	 */
	@get:Input
	public abstract val cargoProject: Property<CargoProjectDeclaration>

	/**
	 * This specifies what target triple to build for via Cargo.
	 */
	@get:Input
	public abstract val target: Property<String>

	@TaskAction
	private fun run() {
		// ------------ Property retrieval ------------ //

		val cargoProject = cargoProject.get()
		val toolchainTargets = ToolchainTargets.getForTarget(target.get())
		logger.lifecycle("Building cargo project ${cargoProject.name.get()} for target ${toolchainTargets.cargoTarget}")

		val projectPathRaw = cargoProject.projectPath.get()
		val cargoExe = cargoProject.cargoExecutable.get()
		val extraArguments = cargoProject.cargoArguments.get()
		val profile = cargoProject.profile.get()
		val target = target.get()
		val libName = cargoProject.libName.orNull
		val extraIncludes = cargoProject.extraIncludes.get()

		// ------------ Setup ------------ //

		// Obtain resolved cargo project path
		val projectPath = when (File(projectPathRaw).isAbsolute) {
			true -> File(projectPathRaw)
			else -> File(project.projectDir, projectPathRaw)
		}

		// Make a list of all files to copy from the Cargo target build dir
		val buildIncludes = when {
			libName == null && extraIncludes.isEmpty() ->
				throw IllegalArgumentException("No cargo output files included! libName and extraIncludes are both missing!")

			libName != null -> listOf(
				"lib${libName}.so",
				"lib${libName}.dylib",
				"${libName}.dll",
			) + extraIncludes

			else -> extraIncludes
		}

		val cargoCommandLine = mutableListOf(
			cargoExe,
			"build",
			"--target=$target",
			"--profile=$profile",
			*extraArguments.toTypedArray(),
		)

		// ------------ Validation ------------ //

		if (!File(projectPath, "Cargo.toml").exists())
			throw FileNotFoundException("Supplied projectPath does not contain a Cargo.toml")

		// ------------ Cargo Execution ------------ //

		project.exec { spec ->
			spec.commandLine = cargoCommandLine
			spec.workingDir = projectPath
			spec.standardOutput = System.out
			spec.errorOutput = System.err
		}.assertNormalExitValue()

		// ------------ Artifact copying ------------ //

		project.copy { spec ->
			spec.from(projectPath.resolve("target/$target/$profile"))
			spec.into(project.layout.buildDirectory.get().dir("rustLibs").dir(target))
			spec.include(buildIncludes)
		}
	}
}
