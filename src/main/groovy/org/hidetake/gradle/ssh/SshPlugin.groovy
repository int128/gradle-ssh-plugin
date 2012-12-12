package org.hidetake.gradle.ssh

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.hidetake.gradle.ssh.api.Remote

/**
 * Gradle SSH plugin.
 * This class adds project extension and convention properties.
 * 
 * @see SshPluginConvention
 * @author hidetake.org
 *
 */
class SshPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		attachRemoteContainer(project)
		project.convention.plugins.put('ssh', new SshPluginConvention(project))
	}

	protected NamedDomainObjectContainer<Remote> attachRemoteContainer(Project project) {
		NamedDomainObjectContainer<Remote> remotes = project.container(Remote)
		project.extensions.add('remotes', remotes)
		if (project.parent) {
			def parentRemotes = project.parent.extensions.findByName('remotes')
			if (parentRemotes) {
				remotes.addAll(parentRemotes)
			}
		}
		(remotes as ExtensionAware).extensions.add('role', filterByRole.curry(remotes))
	}

	protected final Closure<Collection> filterByRole = { Collection remotes, String... roles ->
		roles.collect { String role ->
			remotes.findAll { Remote remote -> remote.roles.contains(role) }
		}.flatten().toSet()
	}
}
