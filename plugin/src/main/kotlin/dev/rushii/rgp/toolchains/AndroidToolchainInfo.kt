package dev.rushii.rgp.toolchains

import dev.rushii.rgp.AndroidDeclaration
import org.apache.tools.ant.taskdefs.condition.Os
import java.io.File

/**
 * This toolchain info brings the assumption that building will succeed,
 * as it is native to the current platform.
 *
 * This is also the default for any unknown targets, and as such
 * no workarounds will be applied to make building work.
 *
 * This class is used at runtime when configuring Cargo for execution.
 */
internal class AndroidToolchainInfo(
	override val cargoTarget: String,

	/**
	 * Whether this toolchain is a prebuilt variant that shipped with the NDK.
	 */
	val isPrebuilt: Boolean,

	/**
	 * The Android API level to compile for.
	 */
	val apiLevel: Int,
) : ToolchainInfo {
	internal constructor(cargoTarget: String, android: AndroidDeclaration) : this(
		cargoTarget = cargoTarget,
		isPrebuilt = android.usePrebuiltToolchain.get(),
		apiLevel = android.apiLevel.get(),
	)

	private val compilerTriple: String = when (cargoTarget) {
		// "Note: For 32-bit ARM, the compiler is prefixed with armv7a-linux-androideabi,
		// but the binutils tools are prefixed with arm-linux-androideabi.
		// For other architectures, the prefixes are the same for all tools."
		// (Ref: https://developer.android.com/ndk/guides/other_build_systems#overview)
		"armv7-linux-androideabi" -> if (isPrebuilt) "armv7a-linux-androideabi" else "arm-linux-androideabi"
		"aarch64-linux-android" -> "aarch64-linux-android"
		"i686-linux-android" -> "i686-linux-android"
		"x86_64-linux-android" -> "x86_64-linux-android"
		else -> throw IllegalArgumentException("Unknown Android toolchain $cargoTarget")
	}

	private val binutilsTriple: String = when (cargoTarget) {
		"armv7-linux-androideabi" -> "arm-linux-androideabi"
		"aarch64-linux-android" -> "aarch64-linux-android"
		"i686-linux-android" -> "i686-linux-android"
		"x86_64-linux-android" -> "x86_64-linux-android"
		else -> throw IllegalArgumentException("Unknown Android toolchain $cargoTarget")
	}

	// The non-prebuilt paths are based on the paths the used by the generateToolchains task

	/**
	 * Gets the path to the clang C compiler.
	 * @param toolchainPath The host-specific NDK toolchain directory. (ie, `/toolchains/llvm/windows-x86_64/`)
	 */
	internal fun cc(toolchainPath: File): File {
		val basePath = when (isPrebuilt) {
			true -> "/bin/$compilerTriple$apiLevel-clang"
			false -> "/$cargoTarget-$apiLevel/bin/$compilerTriple-clang"
		}

		return File(toolchainPath, if (Os.isFamily(Os.FAMILY_WINDOWS)) "$basePath.cmd" else basePath)
	}

	/**
	 * Gets the path to the clang C++ compiler.
	 * @param toolchainPath The host-specific NDK toolchain directory. (ie, `/toolchains/llvm/windows-x86_64/`)
	 */
	internal fun cxx(toolchainPath: File): File {
		val basePath = when (isPrebuilt) {
			true -> "/bin/$compilerTriple$apiLevel-clang++"
			false -> "/$cargoTarget-$apiLevel/bin/$compilerTriple-clang++"
		}

		return File(toolchainPath, if (Os.isFamily(Os.FAMILY_WINDOWS)) "$basePath.cmd" else basePath)
	}

	/**
	 * Gets the path to llvm-ar.
	 * @param toolchainPath The host-specific NDK toolchain directory. (ie, `/toolchains/llvm/windows-x86_64/`)
	 * @param ndk The Android NDK this toolchain info represents.
	 */
	internal fun ar(toolchainPath: File, ndk: AndroidNdkInfo): File {
		val basePath = if (ndk.versionMajor >= 23) {
			"/bin/llvm-ar"
		} else when (isPrebuilt) {
			true -> "/bin/$binutilsTriple-ar"
			false -> "/$cargoTarget-$apiLevel/bin/$binutilsTriple-ar"
		}

		return File(toolchainPath, if (Os.isFamily(Os.FAMILY_WINDOWS)) "$basePath.exe" else basePath)
	}

	/**
	 * Gets the path to a python executable.
	 * @param toolchainPath The host-specific NDK toolchain directory. (ie, `/toolchains/llvm/windows-x86_64/`)
	 */
	internal fun python(toolchainPath: File): File {
		val basePath = "/python3/python"

		return File(toolchainPath, if (Os.isFamily(Os.FAMILY_WINDOWS)) "$basePath.exe" else basePath)
	}
}
