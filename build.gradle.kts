plugins {
	alias(libs.plugins.kotlin) apply false
	alias(libs.plugins.pluginPublish) apply false
	alias(libs.plugins.ktlint) apply false
	alias(libs.plugins.versionCheck) apply false
}

tasks.register("clean", Delete::class.java) {
	delete(rootProject.layout.buildDirectory)
}
