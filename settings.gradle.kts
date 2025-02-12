pluginManagement {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
}

rootProject.name = "rust-gradle-plugin"
include(":examples:desktop-app")
include(":examples:android-app")
include(":examples:android-library")
includeBuild("./plugin")
