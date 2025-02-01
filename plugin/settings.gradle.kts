@file:Suppress("UnstableApiUsage")

pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
	}
}

dependencyResolutionManagement {
	repositories {
		google()
		mavenCentral()
	}

	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}
