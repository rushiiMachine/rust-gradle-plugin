package dev.rushii.rgp.toolchains

import dev.rushii.rgp.AndroidDeclaration
import dev.rushii.rgp.GenerateAndroidToolchainsTask
import org.apache.tools.ant.taskdefs.condition.Os
import java.io.File
import kotlin.math.min

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
	 * See [AndroidDeclaration.usePrebuiltToolchain] for more info.
	 */
	val isPrebuilt: Boolean,

	/**
	 * If this is not a prebuilt toolchain shipped with the NDK, then this should point to the toolchains dir
	 * generated by [GenerateAndroidToolchainsTask] using the [ndk].
	 *
	 * This is only required when [isPrebuilt]=`false`.
	 */
	val customToolchainsDir: File?,

	/**
	 * The Android API level this toolchain was initially configured to compile for.
	 * This does not have the fixes applied by the getter [apiLevel].
	 */
	val configuredApiLevel: Int,

	/**
	 * The NDK this toolchain is based on.
	 */
	val ndk: AndroidNdkInfo,
) : ToolchainInfo {
	internal constructor(
		cargoTarget: String,
		android: AndroidDeclaration,
		ndk: AndroidNdkInfo,
		customToolchainsDir: File?,
	) : this(
		cargoTarget = cargoTarget,
		isPrebuilt = android.usePrebuiltToolchain.get(),
		customToolchainsDir = customToolchainsDir,
		configuredApiLevel = android.apiLevel.get(),
		ndk = ndk,
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
		"riscv64-linux-android" -> "riscv64-linux-android"
		else -> throw IllegalArgumentException("Unknown Android toolchain $cargoTarget")
	}

	private val binutilsTriple: String = when (cargoTarget) {
		"armv7-linux-androideabi" -> "arm-linux-androideabi"
		"aarch64-linux-android" -> "aarch64-linux-android"
		"i686-linux-android" -> "i686-linux-android"
		"x86_64-linux-android" -> "x86_64-linux-android"
		"riscv64-linux-android" -> "UNUSED"
		else -> throw IllegalArgumentException("Unknown Android toolchain $cargoTarget")
	}

	val targetArch: String = when (cargoTarget) {
		"arm-linux-androideabi", "armv7-linux-androideabi", "thumbv7neon-linux-androideabi" -> "arm"
		"aarch64-linux-android" -> "arm64"
		"i686-linux-android" -> "x86"
		"x86_64-linux-android" -> "x86_64"
		"riscv64-linux-android" -> "riscv64"
		else -> throw IllegalArgumentException("Unknown Android toolchain $cargoTarget")
	}

	val apiLevel: Int
		get() = when {
			// riscv64 is only available starting with API 35
			targetArch == "riscv64" -> min(35, configuredApiLevel)

			// Cannot build for 65-bit targeting below API 21
			targetArch.contains("64") -> min(21, configuredApiLevel)

			else -> configuredApiLevel
		}

	/**
	 * Gets the path to the clang C compiler.
	 */
	fun cc(): File {
		val basePath = when (isPrebuilt) {
			true -> "/bin/$compilerTriple$apiLevel-clang"
			false -> "/bin/$compilerTriple-clang"
		}

		return File(getLLVMToolchainPath(), withWindowsExtension(basePath, "cmd"))
	}

	/**
	 * Gets the path to the clang C++ compiler.
	 */
	fun cxx(): File {
		val basePath = when (isPrebuilt) {
			true -> "/bin/$compilerTriple$apiLevel-clang++"
			false -> "/bin/$compilerTriple-clang++"
		}

		return File(getLLVMToolchainPath(), withWindowsExtension(basePath, "cmd"))
	}

	/**
	 * Gets the path to llvm-ar.
	 */
	fun ar(): File {
		val basePath = if (ndk.versionMajor >= 23) {
			"/bin/llvm-ar"
		} else when (isPrebuilt) {
			true -> "/bin/$binutilsTriple-ar"
			false -> "/bin/$binutilsTriple-ar"
		}

		return File(getLLVMToolchainPath(), withWindowsExtension(basePath, "exe"))
	}

	/**
	 * Gets the path to a python executable shipped with the NDK.
	 * This can be either Python 2 or Python 3 depending on the NDK version.
	 */
	fun python(): File {
		val pythonExe = withWindowsExtension("python", "exe")

		return if (ndk.versionMajor >= 25) {
			File(getLLVMToolchainPath(), "/python3/$pythonExe")
		} else {
			File(ndk.path, "/prebuilt/${getHostTag()}/bin/$pythonExe")
		}
	}

	/**
	 * Returns the host platform-specific NDK LLVM toolchain directory. (ie, `/toolchains/llvm/windows-x86_64/`)
	 */
	fun getLLVMToolchainPath(): File {
		return when (isPrebuilt) {
			true -> File(ndk.path, "/toolchains/llvm/prebuilt/${getHostTag()}")
			false -> customToolchainsDir?.let { File(it, "$targetArch-$apiLevel") }
				?: throw IllegalStateException("Generated toolchain without any custom toolchain dir!")
		}
	}

	/**
	 * Returns the platform name of the prebuilt toolchain based on the current host platform.
	 */
	private fun getHostTag(): String {
		// Based on:
		// https://cs.android.com/android/kernel/superproject/+/common-android-mainline:prebuilts/ndk-r26/build/tools/ndk_bin_common.sh
		// https://stackoverflow.com/q/10846105/13964629

		val arch = when (val rawArch = System.getProperty("os.arch")) {
			"arm64" -> "arm64"
			"x86_64", "amd64" -> "x86_64"
			"x86", "i386", "i486", "i586", "i686" -> "x86"
			else -> throw IllegalStateException("Unsupported host architecture $rawArch")
		}

		return when {
			Os.isFamily(Os.FAMILY_WINDOWS) -> "windows-$arch"
			Os.isFamily(Os.FAMILY_MAC) -> "darwin-x86_64"
			else -> "linux-$arch"
		}
	}

	/**
	 * Appends a file extension to a string if the host platform is Windows.
	 */
	private fun withWindowsExtension(original: String, windowsExt: String): String {
		return if (Os.isFamily(Os.FAMILY_WINDOWS)) "$original.$windowsExt" else original
	}
}
