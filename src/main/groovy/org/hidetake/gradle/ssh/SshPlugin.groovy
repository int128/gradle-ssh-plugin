package org.hidetake.gradle.ssh

import org.gradle.api.NamedDomainObjectCollection
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
		project.extensions.remotes = createRemotes(project)
		project.convention.plugins.put('ssh', new SshPluginConvention(project))
	}

	protected NamedDomainObjectCollection<Remote> createRemotes(Project project) {
		def remotes = project.container(Remote)
		remotes.extensions.role = (Closure<Collection<Remote>>) { String role ->
			remotes.findAll { Remote remote -> remote.roles.contains(role) }
		}
		remotes
	}
}
