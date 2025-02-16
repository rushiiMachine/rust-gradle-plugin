plugins {
	application
	alias(libs.plugins.kotlin.jvm)
	id("dev.rushii.rust-gradle-plugin")
}

repositories {
	mavenCentral()
}

rust {
	projects {
		create("libhello") {
			projectPath.set("./src/main/rust")
			libName.set("hello")
			profile.set("release")
			targets.addAll("default") // Target the host's default target
		}
	}
}

// Make sure we run Cargo when building
// `processResources` task is added by the `application` plugin
tasks.getByName("processResources").dependsOn("cargoBuildAll")

// The app is runnable through the `run` task
application {
	mainClass.set("dev.rushii.libhello.Main")

	applicationDefaultJvmArgs = listOf(
		// Optional debugging flag for development
		"-Xcheck:jni",

		// Uses the build for the default target
		"-Djava.library.path=./build/rustLibs/default",
	)
}
