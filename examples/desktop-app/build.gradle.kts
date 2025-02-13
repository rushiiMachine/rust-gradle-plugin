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
			targets.addAll("x86_64-pc-windows-msvc", "i686-pc-windows-msvc")
		}
	}
}

kotlin {
	explicitApi()
}

application {
	mainClass.set("dev.rushii.libhello.Main")
	// TODO: compile for default target & allow acquiring it?
	applicationDefaultJvmArgs = listOf("-Djava.library.path=./build/rustLibs/x86_64-pc-windows-msvc")
}

// Make sure we run Cargo when compiling
tasks.getByName("processResources").dependsOn("cargoBuildAll")

// Delete Cargo build dir when running clean from Gradle
tasks.maybeCreate("clean", Delete::class.java).apply {
	delete("./src/main/rust/target")
}
