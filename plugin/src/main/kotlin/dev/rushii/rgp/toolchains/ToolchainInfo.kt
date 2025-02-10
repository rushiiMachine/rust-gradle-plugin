package dev.rushii.rgp.toolchains

internal interface ToolchainInfo {
	/**
	 * A cargo triple as defined [here](https://doc.rust-lang.org/nightly/rustc/platform-support.html).
	 */
	val cargoTarget: String

	companion object {
		/**
		 * Obtain platform specific toolchain info for a specific Cargo target.
		 * @param targetName A cargo triple as defined [here](https://doc.rust-lang.org/nightly/rustc/platform-support.html).
		 */
		fun getForCargoTarget(targetName: String, androidPrebuiltToolchain: Boolean = false): ToolchainInfo {
			// Likely requires the Android NDK for compilation.
			if (targetName.contains("android"))
				return AndroidToolchainInfo(targetName, isPrebuilt = androidPrebuiltToolchain)

			// Unknown, assumed will build correctly
			return NativeToolchainInfo(cargoTarget = targetName)
		}
	}
}
