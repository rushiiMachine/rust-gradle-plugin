package dev.rushii.rgp.toolchains

import dev.rushii.rgp.AndroidDeclaration

internal interface ToolchainInfo {
	/**
	 * A cargo triple as defined [here](https://doc.rust-lang.org/nightly/rustc/platform-support.html).
	 */
	val cargoTarget: String

	companion object {
		/**
		 * Obtain platform specific toolchain info for a specific Cargo target.
		 * @param targetName A cargo triple as defined [here](https://doc.rust-lang.org/nightly/rustc/platform-support.html).
		 * @param android The Android config to be used for a specific project (required only for Android targets).
		 * 				  This config should be fully configured, meaning initializing [ToolchainInfo] should not be done at configuration time.
		 */
		fun getForCargoTarget(targetName: String, android: AndroidDeclaration? = null): ToolchainInfo {
			// Likely requires the Android NDK for compilation.
			if (targetName.contains("android")) {
				val androidConfig = android ?: throw IllegalArgumentException("Android config null for an Android target")
				return AndroidToolchainInfo(targetName, androidConfig)
			}

			// Unknown, assumed will build correctly
			return NativeToolchainInfo(cargoTarget = targetName)
		}
	}
}
