plugins {
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.android) apply false
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.pluginPublish) apply false
}

tasks.register("clean", Delete::class.java) {
	delete(rootProject.layout.buildDirectory)
}
