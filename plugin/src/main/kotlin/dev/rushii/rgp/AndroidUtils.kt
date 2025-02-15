package dev.rushii.rgp

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import dev.rushii.rgp.toolchains.AndroidNdkInfo
import org.gradle.api.GradleException
import org.gradle.api.plugins.ExtensionContainer
import java.io.File
import java.util.Properties

/**
 * Obtains the configured Android Gradle plugin extension (either the [AppExtension] or [LibraryExtension]) for this project.
 * @throws IllegalStateException If AGP is not included in this project or the extension was not found.
 */
internal fun ExtensionContainer.getAndroid(): BaseExtension {
	val extension = try {
		findByType(AppExtension::class.java) ?: findByType(LibraryExtension::class.java)
	} catch (_: ClassNotFoundException) {
		throw GradleException("Cannot use a AndroidGenerateToolchainsTask in a project without the Android Gradle plugin applied")
	}

	return extension ?: throw GradleException("Failed to find a Android Gradle plugin extension in this project")
}

/**
 * Obtains info about the Android NDK from an AGP extension.
 */
internal fun BaseExtension.getNdkInfo(): AndroidNdkInfo {
	val ndkPropsFile = File(ndkDirectory, "source.properties")
	if (!ndkPropsFile.exists())
		throw GradleException("Specified ndkDirectory is not a valid NDK!")

	val ndkProps = Properties().apply { load(ndkPropsFile.inputStream()) }
	val ndkVersion = ndkProps.getProperty("Pkg.Revision", "0.0")
	val ndkVersionMajor = ndkVersion.split(".").first().toInt()

	return AndroidNdkInfo(path = ndkDirectory, versionMajor = ndkVersionMajor)
}
