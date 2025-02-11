package dev.rushii.rgp

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import dev.rushii.rgp.toolchains.AndroidNdkInfo
import org.gradle.api.plugins.ExtensionContainer
import java.io.File
import java.util.*

/**
 * Obtains the configured Android Gradle plugin extension (either the [AppExtension] or [LibraryExtension]) for this project.
 * @throws IllegalStateException If AGP is not included in this project or the extension was not found.
 */
internal fun ExtensionContainer.getAndroid(): BaseExtension {
	val extension = try {
		findByType(AppExtension::class.java) ?: findByType(LibraryExtension::class.java)
	} catch (_: ClassNotFoundException) {
		throw IllegalStateException("Cannot use a AndroidGenerateToolchainsTask in a project without the Android Gradle plugin applied")
	}

	return extension ?: throw IllegalStateException("Failed to find a Android Gradle plugin extension in this project")
}

/**
 * Obtains info about the Android NDK from a AGP extension.
 */
internal fun BaseExtension.getNdkInfo(): AndroidNdkInfo {
	val ndkProps = Properties()
	val ndkSourcePropertiesFile = File(ndkDirectory, "source.properties")
	if (ndkSourcePropertiesFile.exists()) {
		ndkProps.load(ndkSourcePropertiesFile.inputStream())
	}

	val ndkVersion = ndkProps.getProperty("Pkg.Revision", "0.0")
	val ndkVersionMajor = ndkVersion.split(".").first().toInt()

	return AndroidNdkInfo(path = ndkDirectory, versionMajor = ndkVersionMajor)
}
