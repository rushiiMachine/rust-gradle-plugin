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
	namespace = "dev.rushii.rgp.example"
	compileSdk = 35

	defaultConfig {
		minSdk = 21
		targetSdk = 35
		versionCode = 1
		versionName = "0.0.1"

		proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
			targets.addAll("armv7-linux-androideabi", "aarch64-linux-android", "i686-linux-android", "x86_64-linux-android")

			// Set Cargo profile to release if any Gradle release task in going to be run
			gradle.taskGraph.whenReady {
				if (allTasks.any { it.name.contains("release") })
					this@create.profile.set("release")
			}
		}
	}
}
