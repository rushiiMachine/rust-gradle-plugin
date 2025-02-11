package dev.rushii.rgp

import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * The configuration for the Android NDK to be used for a specific project.
 */
public class AndroidDeclaration(project: Project) {
	/**
	 * Whether to use only toolchains that have a prebuilt variant shipped with the NDK.
	 * If this is false, then `make_standalone_toolchain.py` is invoked to **quickly** generate
	 * a toolchain with all the specified [apiLevel] and targets of the project in [customToolchainDirectory].
	 *
	 * This can only be used if the specified NDK's version is at least v19.
	 *
	 * Defaults to whether the specified NDK's version it at least v19.
	 */
	public val usePrebuiltToolchain: Property<Boolean> = project.objects.property(Boolean::class.java)

	/**
	 * The Android API level to compile for.
	 *
	 * Note that for 64-bit targets, the apiLevel will be clamped to a minimum of API 21, since
	 * API levels prior to that do not support building for 64-bit.
	 *
	 * Defaults to the `minSdkVersion` property of the Android Gradle plugin.
	 */
	public val apiLevel: Property<Int> = project.objects.property(Int::class.java)
}
