package dev.rushii.rgp.config

import dev.rushii.rgp.RustPlugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import javax.inject.Inject

/**
 * Root project extension for configuring a [RustPlugin] applied to a project.
 */
public abstract class RustExtension @Inject constructor(project: Project) {
	internal val cargoProjects = project.container(CargoProjectDeclaration::class.java)

	/**
	 * Declare or configure all Cargo projects.
	 */
	public fun projects(block: NamedDomainObjectContainer<CargoProjectDeclaration>.() -> Unit) {
		block(cargoProjects)
	}
}
