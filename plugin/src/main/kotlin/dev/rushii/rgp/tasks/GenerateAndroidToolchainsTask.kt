package dev.rushii.rgp.tasks

import dev.rushii.rgp.RustPlugin
import dev.rushii.rgp.config.CargoProjectDeclaration
import dev.rushii.rgp.config.RustExtension
import dev.rushii.rgp.toolchains.AndroidNdkInfo
import dev.rushii.rgp.toolchains.AndroidToolchainInfo
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

/**
 * For NDK versions below r19, there are no prebuilt toolchains that contain the necessary LLVM setup
 * in order to compile. Therefore, we can run the included `make_standalone_toolchain.py` from the NDK
 * in order to generate an API level + arch specific toolchain that will be used for compilation.
 */
public abstract class GenerateAndroidToolchainsTask : DefaultTask() {
	init {
		group = RustPlugin.TASK_GROUP
		description = "Make standalone Android NDK toolchains for each project that does not use a prebuilt toolchain"
	}

	@get:Input
	public abstract val androidNdk: Property<AndroidNdkInfo>

	@get:Inject
	internal abstract val execOperations: ExecOperations

	private val rustExtension = this.project.extensions.getByType(RustExtension::class.java)
	private val customToolchainsDir = this.project.layout.buildDirectory.dir("generatedToolchains")

	@TaskAction
	internal fun run() {
		val ndk = androidNdk.get()

		val generateToolchains = rustExtension.cargoProjects
			.filter { it.hasAndroidTargets() }
			.filterNot { it.android.usePrebuiltToolchain.get() }
			.flatMap { mapCargoProjectToolchains(it, ndk) }
			.distinctBy { it.targetArch to it.apiLevel }

		for (toolchainInfo in generateToolchains) {
			val outDir = customToolchainsDir.get()
				.dir("${toolchainInfo.targetArch}-${toolchainInfo.apiLevel}").asFile

			if (outDir.exists()) {
				logger.info("NDK toolchain ${outDir.name} already exists, skipping.")
				continue
			} else {
				logger.lifecycle("Generating NDK toolchain ${outDir.name}")
			}

			// Always regenerate the toolchain, even if it exists already. It is fast to do so
			// and fixes any issues with partially reclaimed temporary files.
			execOperations.exec { spec ->
				spec.standardOutput = System.out
				spec.errorOutput = System.err
				spec.executable = toolchainInfo.python().absolutePath
				spec.args = listOf(
					File(ndk.path, "/build/tools/make_standalone_toolchain.py").absolutePath,
					"--arch=${toolchainInfo.targetArch}",
					"--api=${toolchainInfo.apiLevel}",
					"--install-dir=${outDir.absolutePath}",
					"--force",
				)
			}
		}
	}

	private fun mapCargoProjectToolchains(
		cargoProject: CargoProjectDeclaration,
		ndk: AndroidNdkInfo,
	): Iterable<AndroidToolchainInfo> {
		return cargoProject.targets.get().map { target ->
			AndroidToolchainInfo(
				cargoTarget = target,
				// This is correct because new NDKs contain python in a
				// prebuilt toolchain directory (they already have prebuilt toolchains)
				isPrebuilt = true,
				customToolchainsDir = null, // Unused in this use-case
				configuredApiLevel = cargoProject.android.apiLevel.get(),
				ndk = ndk,
			)
		}
	}
}
