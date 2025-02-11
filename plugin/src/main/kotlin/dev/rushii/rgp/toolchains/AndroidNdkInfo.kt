package dev.rushii.rgp.toolchains

import java.io.File

/**
 * Information about the Android NDK used to build a specific project
 * obtained from the Android Gradle plugin.
 */
public class AndroidNdkInfo(
	/**
	 * Path to the Android NDK directory ([com.android.build.gradle.BaseExtension.ndkDirectory]).
	 */
	public val path: File,

	/**
	 * The major version of the NDK specified.
	 */
	public val versionMajor: Int,
)
