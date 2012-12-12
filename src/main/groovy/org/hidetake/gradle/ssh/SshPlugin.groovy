package org.hidetake.gradle.ssh

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
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
		NamedDomainObjectContainer<Remote> remotes = project.container(Remote)
		remotes.extensions.add('role', (Closure<Collection<Remote>>) { String... roles ->
			roles.collect { String role ->
				remotes.findAll { Remote remote -> remote.roles.contains(role) }
			}.flatten().toSet()
		})
		project.extensions.add('remotes', remotes)
		if (project.parent) {
			NamedDomainObjectContainer<Remote> parentRemotes = project.parent.extensions.findByName('remotes')
			if (parentRemotes) {
				remotes.addAll(parentRemotes)
			}
		}

		project.convention.plugins.put('ssh', new SshPluginConvention(project))
	}
}
