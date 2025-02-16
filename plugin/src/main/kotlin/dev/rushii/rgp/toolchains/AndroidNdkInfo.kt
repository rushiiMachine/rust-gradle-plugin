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
	 * The full NDK version string (value of `Pkg.Revision` in `source.properties` of the NDK)
	 */
	public val version: String,

	/**
	 * The major version of the NDK specified.
	 */
	public val versionMajor: Int,

	/**
	 * The last part of the NDK version, which is also the CI build number on ci.android.com
	 */
	public val versionRevision: Int,
)
