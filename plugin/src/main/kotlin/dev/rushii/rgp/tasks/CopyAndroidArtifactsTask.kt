package dev.rushii.rgp.tasks

import dev.rushii.rgp.RustPlugin
import dev.rushii.rgp.config.RustExtension
import org.gradle.api.tasks.Sync

/**
 * Task to copy the output binaries from registered [CargoBuildTask] that target Android into a
 * directory structure that's compatible with the Android Gradle Plugin. This is to allow
 * bundling these libraries as JNI libs into AARs or APKs.
 */
public abstract class CopyAndroidArtifactsTask : Sync() {
	init {
		group = RustPlugin.TASK_GROUP
		description = "Copies the output binaries from builds targeting Android into " +
			"a directory structure compatible with the Android Gradle Plugin."

		val rustExtension = project.extensions.getByType(RustExtension::class.java)
		val rustDir = project.layout.buildDirectory.dir("rustLibs")
		val jniDir = project.layout.buildDirectory.dir("rustJniLibs")

		for (cargoProject in rustExtension.cargoProjects) {
			for (target in cargoProject.targets.get()) {
				if (!target.contains("android"))
					continue

				this.dependsOn("cargoBuild-${cargoProject.name.get()}-$target")
				this.from(rustDir.get().dir(target))
			}
		}

		this.into(jniDir)
		this.eachFile {
			it.path = jniLibsName(it.file.parentFile.name) + "/" + it.path
		}
	}

	/**
	 * This is the directory name where the native libraries are kept for each architecture
	 * so it will be properly bundled into an APK by AGP.
	 */
	private fun jniLibsName(cargoTarget: String): String = when (cargoTarget) {
		"arm-linux-androideabi", "armv7-linux-androideabi", "thumbv7neon-linux-androideabi" -> "armeabi-v7a"
		"aarch64-linux-android" -> "arm64-v8a"
		"i686-linux-android" -> "x86"
		"x86_64-linux-android" -> "x86_64"
		"riscv64-linux-android" -> "riscv64"
		else -> throw IllegalArgumentException("Unknown Android target $cargoTarget")
	}
}
