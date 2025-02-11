import org.gradle.kotlin.dsl.support.listFilesOrdered

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	id("dev.rushii.rust-gradle-plugin")
}

repositories {
	google()
	mavenCentral()
}

android {
	namespace = "com.aliucord.manager"
	compileSdk = 35

	defaultConfig {
		minSdk = 21
		targetSdk = 35
		versionCode = 1
		versionName = "0.0.1"
	}

	kotlinOptions {
		jvmTarget = "1.8"
	}

	ndkVersion = sdkDirectory.resolve("ndk").listFilesOrdered().last().name
}

rust {
	projects {
		create("libhello") {
			projectPath.set("./src/main/rust")
			libName.set("hello")
			profile.set("release")
			targets.addAll("armv7-linux-androideabi", "aarch64-linux-android", "i686-linux-android", "x86_64-linux-android")
		}
	}
}

// Delete Cargo build dir when running clean from Gradle
tasks.maybeCreate("clean", Delete::class.java).apply {
	delete("./src/main/rust/target")
}
