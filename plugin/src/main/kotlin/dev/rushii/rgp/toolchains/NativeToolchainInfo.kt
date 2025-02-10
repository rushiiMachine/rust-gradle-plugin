package dev.rushii.rgp.toolchains

/**
 * This toolchain info brings the assumption that building will succeed,
 * as it is native to the current platform.
 *
 * This is also the default for any unknown targets, and as such
 * no workarounds will be applied to make building work.
 */
internal class NativeToolchainInfo(
	override val cargoTarget: String,
) : ToolchainInfo
