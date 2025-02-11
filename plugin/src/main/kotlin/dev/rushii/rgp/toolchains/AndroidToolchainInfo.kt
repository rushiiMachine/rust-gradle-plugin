package dev.rushii.rgp.toolchains

import dev.rushii.rgp.AndroidDeclaration

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
) : ToolchainInfo {
	internal constructor(cargoTarget: String, android: AndroidDeclaration) : this(
		cargoTarget = cargoTarget,
		isPrebuilt = android.usePrebuiltToolchains.get(),
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
}
