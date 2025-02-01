plugins {
	alias(libs.plugins.kotlin)
	id("dev.rushii.rust-gradle-plugin")
}

repositories {
	mavenCentral()
}

rust {
	projects {
		create("libhello") {
			projectPath.set("./src/main/rust")
			profile.set("release")
			targets.addAll("x86_64-pc-windows-msvc", "x86_64-unknown-linux-gnu")
		}
	}
}

kotlin {
	explicitApi()
}
