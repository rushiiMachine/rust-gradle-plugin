@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.loadProperties

val pluginProperties = loadProperties(file("plugin.properties").absolutePath)

plugins {
	`java-gradle-plugin`
	alias(libs.plugins.kotlin)
	alias(libs.plugins.ktlint)
	alias(libs.plugins.pluginPublish)
	alias(libs.plugins.versionCheck)
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation(gradleApi())

	testImplementation(libs.junit)
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_1_8)
	}
}

ktlint {
	debug.set(false)
	verbose.set(true)
	android.set(false)
	outputToConsole.set(true)
	ignoreFailures.set(false)
	enableExperimentalRules.set(true)
	filter {
		exclude("**/generated/**")
		include("**/kotlin/**")
	}
}

group = pluginProperties["GROUP"].toString()
version = pluginProperties.getProperty("VERSION")

gradlePlugin {
	website.set(pluginProperties.getProperty("WEBSITE"))
	vcsUrl.set(pluginProperties.getProperty("VCS_URL"))

	plugins {
		create(pluginProperties.getProperty("ID")) {
			id = pluginProperties.getProperty("ID")
			implementationClass = pluginProperties.getProperty("IMPLEMENTATION_CLASS")
			version = pluginProperties.getProperty("VERSION")
			description = pluginProperties.getProperty("DESCRIPTION")
			displayName = pluginProperties.getProperty("DISPLAY_NAME")
			tags.set(listOf("rust", "cargo", "android"))
		}
	}
}
