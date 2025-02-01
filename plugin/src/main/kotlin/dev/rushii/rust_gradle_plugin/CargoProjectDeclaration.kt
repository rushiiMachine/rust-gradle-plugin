package dev.rushii.rust_gradle_plugin

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

/**
 * Configures a specific Cargo project to be built.
 */
public abstract class CargoProjectDeclaration @Inject internal constructor(name: String, project: Project) : Named {
	/**
	 * The name of this declaration.
	 *
	 * This is used when generating gradle tasks.
	 */
	public val name: Property<String> = project.objects
		.property(String::class.java)
		.value(name)
		.apply { finalizeValue() }

	/**
	 * The name or path of the Cargo executable to use when building.
	 *
	 * This can be changed for an absolute path to a specific version of `cargo`, or for another cargo-compatible tool
	 * like `cross`.
	 *
	 * By default, this is `cargo`, using the executable on the `PATH`.
	 */
	public val cargoExecutable: Property<String> = project.objects
		.property(String::class.java)
		.convention("cargo")

	/**
	 * The path to the directory where a Cargo project exists
	 */
	public val projectPath: Property<String> = project.objects.property(String::class.java)

	/**
	 * Name of the profile to pass to Cargo through `--profile=...`.
	 *
	 * By default, this is omitted, defaulting to debug.
	 */
	public val profile: Property<String> = project.objects.property(String::class.java)

	/**
	 * Optional extra arguments to pass to Cargo when running a build for a target.
	 *
	 * This does not replace any Cargo arguments added by this plugin.
	 */
	public val cargoArguments: ListProperty<String> = project.objects.listProperty(String::class.java)

	/**
	 * A list of targets that will each be built by Cargo.
	 *
	 * Each target must be a valid target triple as defined by Cargo [here](https://doc.rust-lang.org/nightly/rustc/platform-support.html).
	 */
	public val targets: SetProperty<String> = project.objects.setProperty(String::class.java)

	override fun getName(): String = name.get()
}
