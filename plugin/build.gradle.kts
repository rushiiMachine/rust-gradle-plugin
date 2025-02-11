@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
	`java-gradle-plugin`
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.pluginPublish)
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation(gradleApi())
	compileOnly(libs.androidGradle)

	testImplementation(libs.junit)
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_11)

		if (sourceSetName.get() == "main")
			explicitApiMode.set(ExplicitApiMode.Strict)
	}
}

val pluginProperties = loadProperties(file("plugin.properties").absolutePath)

group = pluginProperties["GROUP"].toString()
version = pluginProperties.getProperty("VERSION")

gradlePlugin {
	website.set(pluginProperties.getProperty("WEBSITE"))
	vcsUrl.set(pluginProperties.getProperty("VCS_URL"))

	plugins {
		create("rust-gradle-plugin") {
			id = pluginProperties.getProperty("PLUGIN_ID")
			implementationClass = pluginProperties.getProperty("IMPLEMENTATION_CLASS")
			version = pluginProperties.getProperty("VERSION")
			description = pluginProperties.getProperty("DESCRIPTION")
			displayName = pluginProperties.getProperty("DISPLAY_NAME")
			tags.set(listOf("rust", "cargo", "android"))
		}
	}
}
