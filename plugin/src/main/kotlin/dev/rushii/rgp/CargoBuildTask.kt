package dev.rushii.rgp

import dev.rushii.rgp.toolchains.AndroidNdkInfo
import dev.rushii.rgp.toolchains.AndroidToolchainInfo
import dev.rushii.rgp.toolchains.ToolchainInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

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
	 * The Android NDK to be used for this build, if this is an Android target.
	 */
	@get:Input
	@get:Optional
	public abstract val androidNdk: Property<AndroidNdkInfo>

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

	// All inputs must be cached outside the task action (using Task.project accessor is disallowed)
	@get:Inject
	internal abstract val execOperations: ExecOperations

	@get:Inject
	internal abstract val fsOperations: FileSystemOperations

	private val gradleProjectDir = project.projectDir
	private val outputDir = target.map { project.layout.buildDirectory.get().dir("rustLibs").dir(it) }

	@TaskAction
	internal fun run() {
		// ------------ Property retrieval ------------ //

		val cargoProject = cargoProject.get()
		val toolchainInfo = ToolchainInfo.getForCargoTarget(
			targetName = target.get(),
			android = cargoProject.android,
			ndkInfo = androidNdk.orNull,
		)

		logger.lifecycle("Building cargo project ${cargoProject.name.get()} for target ${toolchainInfo.cargoTarget}")

		val projectPathRaw = cargoProject.projectPath.get()
		val cargoExe = cargoProject.cargoExecutable.get()
		val extraArguments = cargoProject.cargoArguments.get()
		val extraEnvVars = cargoProject.cargoEnvironmentVariables.get()
		val profile = cargoProject.profile.get()
		val target = target.get()
		val libName = cargoProject.libName.orNull
		val extraIncludes = cargoProject.extraIncludes.get()

		// ------------ Setup ------------ //

		// Obtain resolved cargo project path
		val projectPath = when (File(projectPathRaw).isAbsolute) {
			true -> File(projectPathRaw)
			else -> File(gradleProjectDir, projectPathRaw)
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

		val cargoEnvVars = mutableMapOf<String, Any?>().apply { putAll(System.getenv()) }
		val cargoCommandLine = mutableListOf(
			cargoExe,
			"build",
			"--target=$target",
			"--profile=$profile",
			*extraArguments.toTypedArray(),
		)

		// Configure Android compilation
		if (toolchainInfo is AndroidToolchainInfo) {
			val ndk = androidNdk.get()

			// FIXME non-null assertion
			cargoEnvVars["RUSTFLAGS"] = "-C linker=${toolchainInfo.cc()} -C link-arg=-Wl,-soname,lib${libName!!}.so"

			if (toolchainInfo.isPrebuilt)
				cargoEnvVars["CARGO_NDK_MAJOR_VERSION"] = ndk.versionMajor

			// For build.rs in `cc` consumers: like "CC_i686-linux-android".  See
			// https://github.com/alexcrichton/cc-rs#external-configuration-via-environment-variables.
			cargoEnvVars["CC_${target}"] = toolchainInfo.cc()
			cargoEnvVars["CXX_${target}"] = toolchainInfo.cxx()
			cargoEnvVars["AR_${target}"] = toolchainInfo.ar()

			// Set CLANG_PATH in the environment, so that bindgen (or anything
			// else using clang-sys in a build.rs) works properly, and doesn't
			// use host headers and such.
			cargoEnvVars["CLANG_PATH"] = toolchainInfo.cc()
		}

		cargoEnvVars.putAll(extraEnvVars)

		// ------------ Validation ------------ //

		if (!File(projectPath, "Cargo.toml").exists())
			throw FileNotFoundException("Supplied projectPath does not contain a Cargo.toml")

		// ------------ Cargo Execution ------------ //

		execOperations.exec { spec ->
			spec.commandLine = cargoCommandLine
			spec.environment = cargoEnvVars
			spec.workingDir = projectPath
			spec.standardOutput = System.out
			spec.errorOutput = System.err
		}.assertNormalExitValue()

		// ------------ Artifact copying ------------ //

		fsOperations.copy { spec ->
			spec.from(projectPath.resolve("target/$target/$profile"))
			spec.into(outputDir)
			spec.include(buildIncludes)
		}
	}
}
