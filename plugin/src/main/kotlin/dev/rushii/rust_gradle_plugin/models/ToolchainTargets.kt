package dev.rushii.rust_gradle_plugin.models

public class ToolchainTargets private constructor(
	internal val type: Type,
	internal val cargoTarget: String,
	internal val androidCompilerTriple: String? = null,
	internal val androidBinutilsTriple: String? = null,
) {
	internal enum class Type {
		// TODO: figure out what the difference between these two are
		AndroidPrebuilt,
		AndroidGenerated,

		/**
		 * This is the default if the target is unknown.
		 * This also means that the least amount of workarounds are used,
		 * assuming that the build will succeed.
		 */
		Desktop,
	}

	public companion object {
		/**
		 * Obtains a list of target triples for the specified cargo target triple.
		 * @param targetName A cargo triple as defined [here](https://doc.rust-lang.org/nightly/rustc/platform-support.html).
		 * @throws IllegalArgumentException
		 */
		public fun getForTarget(targetName: String): ToolchainTargets {
			if (targetName.count { it == '-' } < 2)
				throw IllegalArgumentException("Invalid target triple! ($targetName)")

			val toolchain = PREDEFINED_TOOLCHAINS.find { it.cargoTarget == targetName }
			if (toolchain != null) {
				return toolchain
			}

			return ToolchainTargets(
				type = Type.Desktop, // Unknown, assumed will build correctly
				cargoTarget = targetName,
			)
		}

		private val PREDEFINED_TOOLCHAINS = arrayOf(
			ToolchainTargets(
				type = Type.AndroidGenerated,
				cargoTarget = "armv7-linux-androideabi",
				androidCompilerTriple = "arm-linux-androideabi",
				androidBinutilsTriple = "arm-linux-androideabi",
			),
			ToolchainTargets(
				type = Type.AndroidGenerated,
				cargoTarget = "aarch64-linux-android",
				androidCompilerTriple = "aarch64-linux-android",
				androidBinutilsTriple = "aarch64-linux-android",
			),
			ToolchainTargets(
				type = Type.AndroidGenerated,
				cargoTarget = "i686-linux-android",
				androidCompilerTriple = "i686-linux-android",
				androidBinutilsTriple = "i686-linux-android",
			),
			ToolchainTargets(
				type = Type.AndroidGenerated,
				cargoTarget = "x86_64-linux-android",
				androidCompilerTriple = "x86_64-linux-android",
				androidBinutilsTriple = "x86_64-linux-android",
			),
			// This is correct.
			// "Note: For 32-bit ARM, the compiler is prefixed with armv7a-linux-androideabi,
			// but the binutils tools are prefixed with arm-linux-androideabi.
			// For other architectures, the prefixes are the same for all tools."
			// (Ref: https://developer.android.com/ndk/guides/other_build_systems#overview)
			ToolchainTargets(
				type = Type.AndroidPrebuilt,
				cargoTarget = "armv7-linux-androideabi",
				androidCompilerTriple = "armv7a-linux-androideabi",
				androidBinutilsTriple = "arm-linux-androideabi",
			),
			ToolchainTargets(
				type = Type.AndroidPrebuilt,
				cargoTarget = "aarch64-linux-android",
				androidCompilerTriple = "aarch64-linux-android",
				androidBinutilsTriple = "aarch64-linux-android",
			),
			ToolchainTargets(
				type = Type.AndroidPrebuilt,
				cargoTarget = "i686-linux-android",
				androidCompilerTriple = "i686-linux-android",
				androidBinutilsTriple = "i686-linux-android",
			),
			ToolchainTargets(
				type = Type.AndroidPrebuilt,
				cargoTarget = "x86_64-linux-android",
				androidCompilerTriple = "x86_64-linux-android",
				androidBinutilsTriple = "x86_64-linux-android",
			),
		)
	}
}
