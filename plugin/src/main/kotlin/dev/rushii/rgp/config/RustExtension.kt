package dev.rushii.rgp.config

import dev.rushii.rgp.RustPlugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Root project extension for configuring a [RustPlugin] applied to a project.
 */
public abstract class RustExtension @Inject constructor(project: Project) {
	internal val cargoProjects = project.container(CargoProjectDeclaration::class.java)

	/**
	 * This enables integrating Cargo build tasks with external plugins/tasks, such as
	 * - Deleting all [cargoProjects] build dirs when an existing `clean` task is run
	 * - Bundling all [cargoProjects] build artifacts that target Android, as JNI libraries
	 * 	 to be merged into the final AAR/APK by the Android Gradle Plugin (AGP). This also in turn
	 * 	 makes the AGP `merge*JniLibFolders` tasks depend on this plugin's `copyRustAndroidArtifacts` task.
	 *
	 * The code regarding integrating with external plugins can be seen in this function: [RustPlugin.registerTasks],
	 * or alternatively on the [GitHub repository](https://github.com/rushiiMachine/rust-gradle-plugin/blob/master/plugin/src/main/kotlin/dev/rushii/rgp/RustPlugin.kt).
	 *
	 * By default, this is `true`. If you experience issues with these changes or wish to
	 * modify the default behavior, turn this off and implement it yourself.
	 * Each `cargoBuild-*-*` tasks copies the output artifact files
	 * into the `<projectDir>/build/rustDir/<cargoTarget>` directory.
	 */
	public val integrateWithPlugins: Property<Boolean> = project.objects
		.property(Boolean::class.java)
		.convention(true)

	/**
	 * Declare or configure all Cargo projects.
	 */
	public fun projects(block: NamedDomainObjectContainer<CargoProjectDeclaration>.() -> Unit) {
		block(cargoProjects)
	}
}
