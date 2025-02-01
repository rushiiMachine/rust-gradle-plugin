package dev.rushii.rust_gradle_plugin

import dev.rushii.rust_gradle_plugin.models.CargoToolchain
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

public abstract class CargoBuildTask : DefaultTask() {
	init {
		group = RustPlugin.TASK_GROUP
		description = "Builds a specific target for a Cargo project"
	}

	/**
	 * This specifies the Cargo project that will be built.
	 */
	@get:Input
	public abstract val cargoProject: Property<CargoProjectDeclaration>

	/**
	 * This specifies what target triple to build for via Cargo.
	 */
	@get:Input
	public abstract val target: Property<String>

	@TaskAction
	private fun run() {
		val cargoProject = cargoProject.get()
		val cargoToolchain = CargoToolchain.getForTarget(target.get())
		logger.lifecycle("Building cargo project ${cargoProject.name} for target ${cargoToolchain.cargoTarget}")

		TODO()
	}
}
