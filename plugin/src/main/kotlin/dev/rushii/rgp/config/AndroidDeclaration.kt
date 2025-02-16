package dev.rushii.rgp.config

import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * The configuration for the Android NDK to be used for a specific project.
 */
public class AndroidDeclaration internal constructor(project: Project) {
	/**
	 * Whether to use only toolchains that have a prebuilt variant shipped with the NDK.
	 * If this is false, then `make_standalone_toolchain.py` is invoked to **quickly** generate
	 * a toolchain with the specified [apiLevel] and all targets of the project into a temporary build directory.
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

	/**
	 * Sets the necessary linker flags to enable 16KiB page alignment. See
	 * [developer.android.com/guide/practices/page-sizes](https://developer.android.com/guide/practices/page-sizes)
	 * for more information
	 *
	 * This only has an impact when the currently used NDK meets one of the following conditions:
	 * - Is NDK r27
	 * - Is a canary release of LTS NDK r21 with a revision number of at least `12105395`.
	 *   [CI page](https://ci.android.com/builds/branches/aosp-ndk-release-r21/grid?legacy=1)
	 * - Is a canary release of LTS NDK r23 with a revision number of at least `12099874`.
	 *   [CI page](https://ci.android.com/builds/branches/aosp-ndk-release-r23/grid?legacy=1)
	 * - Is a canary release of LTS NDK r25 with a revision number of at least `12093701`.
	 *   [CI page](https://ci.android.com/builds/branches/aosp-ndk-r25-release/grid?legacy=1)
	 *
	 * By default, this is `false`. However, if you are using NDK r28 or above, 16KiB page alignment is
	 * already enabled by default and no longer experimental.
	 */
	public val experimentalPageAlignment: Property<Boolean> = project.objects
		.property(Boolean::class.java)
		.convention(false)
}
