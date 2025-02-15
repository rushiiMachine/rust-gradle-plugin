package dev.rushii.rgp.tasks

import dev.rushii.rgp.RustPlugin
import dev.rushii.rgp.config.CargoProjectDeclaration
import dev.rushii.rgp.toolchains.*
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecOperations
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import javax.inject.Inject

/**
 * Task that builds a Cargo project for a single target, with the config
 * specified by a [CargoProjectDeclaration], and a target.
 */
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

	@Suppress("LeakingThis")
	private val outputDir = this.target.map { this.project.layout.buildDirectory.get().dir("rustLibs").dir(it) }
	private val customToolchainsDir = this.project.layout.buildDirectory.dir("generatedToolchains")
	private val gradleProjectBuildDir = this.project.layout.buildDirectory
	private val gradleProjectNamePath = this.project.path

	@TaskAction
	internal fun run() {
		// ------------ Property retrieval ------------ //

		val cargoProject = cargoProject.get()
		val toolchainInfo = ToolchainInfo.getForCargoTarget(
			targetName = target.get(),
			android = cargoProject.android,
			androidNdkInfo = androidNdk.orNull,
			androidCustomToolchainsDir = customToolchainsDir.get().asFile,
		)

		logger.lifecycle(
			"Building Cargo project ${cargoProject.name.get()}" +
					" for target ${toolchainInfo.cargoTarget}" +
					" for project $gradleProjectNamePath",
		)

		val cargoExe = cargoProject.cargoExecutable.get()
		val extraArguments = cargoProject.cargoArguments.get()
		val extraEnvVars = cargoProject.cargoEnvironmentVariables.get()
		val profile = cargoProject.profile.get()
		val target = target.get()
		val libName = cargoProject.libName.orNull
		val extraIncludes = cargoProject.extraIncludes.get()

		// ------------ Setup ------------ //

		// Obtain resolved cargo project path
		val projectPath = cargoProject.absoluteProjectPath.get()

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
			// For build.rs in `cc` consumers: like "CC_i686-linux-android".  See
			// https://github.com/alexcrichton/cc-rs#external-configuration-via-environment-variables.
			cargoEnvVars["CC_${target}"] = toolchainInfo.cc().absolutePath
			cargoEnvVars["CXX_${target}"] = toolchainInfo.cxx().absolutePath
			cargoEnvVars["AR_${target}"] = toolchainInfo.ar().absolutePath

			// Set CLANG_PATH in the environment, so that bindgen (or anything
			// else using clang-sys in a build.rs) works properly, and doesn't
			// use host headers and such.
			cargoEnvVars["CLANG_PATH"] = toolchainInfo.cc().absolutePath

			// Configure the linker wrapper to pass to Cargo later
			// This wraps linker calls by Cargo and fixes issues with Cargo workspaces and older NDK versions
			val linkerWrapperFileExtension = if (Os.isFamily(Os.FAMILY_WINDOWS)) "bat" else "sh"
			val linkerWrapperPlatformFile = gradleProjectBuildDir.get().asFile
				.resolve("android-linker-wrapper/android-linker-wrapper.$linkerWrapperFileExtension")

			cargoEnvVars["RGP_PYTHON_CMD"] = toolchainInfo.python().absolutePath
			cargoEnvVars["RGP_LINKER_WRAPPER"] = linkerWrapperPlatformFile.resolveSibling("android-linker-wrapper.py").absolutePath
			cargoEnvVars["RGP_CC"] = toolchainInfo.cc().absolutePath
			cargoEnvVars["RGP_CC_LINK_ARGS"] = "-Wl,--build-id,-soname,lib${libName!!}.so" // FIXME non-null assertion
			cargoEnvVars["RGP_NDK_MAJOR_VERSION"] = toolchainInfo.ndk.versionMajor

			// Make Cargo invoke our platform-specific linker wrapper script
			val cargoEnvTargetName = toolchainInfo.cargoTarget.uppercase(Locale.ROOT).replace('-', '_')
			cargoEnvVars["CARGO_TARGET_${cargoEnvTargetName}_LINKER"] = linkerWrapperPlatformFile
		}

		cargoEnvVars.putAll(extraEnvVars)

		// ------------ Validation ------------ //

		if (!File(projectPath, "Cargo.toml").exists())
			throw FileNotFoundException("Supplied projectPath does not contain a Cargo.toml")

		if (toolchainInfo is AndroidToolchainInfo && !toolchainInfo.getLLVMToolchainPath().exists())
			throw FileNotFoundException("Failed to locate a prebuilt LLVM toolchain in the Android NDK")

		// ------------ Cargo Execution ------------ //

		execOperations.exec { spec ->
			spec.commandLine = cargoCommandLine
			spec.environment = cargoEnvVars
			spec.workingDir = projectPath
			spec.standardOutput = System.out
			spec.errorOutput = System.err
		}.assertNormalExitValue()

		// ------------ Artifact copying ------------ //

		fsOperations.sync { spec ->
			spec.from(projectPath.resolve("target/$target/$profile"))
			spec.into(outputDir)
			spec.include(buildIncludes)
		}
	}
}
