package dev.rushii.rgp.tasks

import dev.rushii.rgp.RustPlugin
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import java.io.File

/**
 * Task that extracts the bundled build scripts from the resources of this plugin.
 * These scripts are used to intercept Cargo linker calls in order to maintain
 * compatibility between mismatched Rust and NDK versions.
 *
 * More info:
 * - https://blog.rust-lang.org/2023/01/09/android-ndk-update-r25.html
 * - https://github.com/mozilla/rust-android-gradle/commit/eddfc9e42708d9605ccde7e9963c88bdd41228d1
 */
public abstract class GenerateAndroidLinkerWrapperTask : Sync() {
	init {
		group = RustPlugin.TASK_GROUP
		description = "Extract build scripts that wrap Cargo linker calls in order to keep compatibility between mismatched Rust and NDK versions"

		val currentJar = File(RustPlugin::class.java.protectionDomain.codeSource.location.toURI())

		this.from(project.rootProject.zipTree(currentJar.path))
		this.into(project.layout.buildDirectory.dir("android-linker-wrapper"))
		this.include("**/android-linker-wrapper*")
		this.eachFile {
			it.path = it.path.replaceFirst("dev/rushii/rgp", "")
		}
		this.filePermissions {
			// rwxr-xr-x (u+rwx go+rx)
			// 0755 in decimal; Kotlin doesn't have octal literals
			it.unix(493)
		}

		includeEmptyDirs = false
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	}
}
