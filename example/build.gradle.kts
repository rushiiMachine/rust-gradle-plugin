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
			libName.set("hello")
			profile.set("release")
			targets.addAll("x86_64-pc-windows-msvc", "i686-pc-windows-msvc")
		}
	}
}

kotlin {
	explicitApi()
}
