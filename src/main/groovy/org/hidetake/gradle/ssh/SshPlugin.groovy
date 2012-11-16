package org.hidetake.gradle.ssh

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle SSH plugin.
 * 
 * @author hidetake.org
 *
 */
class SshPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.extensions.create('ssh', SshPluginExtension)
		project.extensions.remotes = project.container(Remote) { String name ->
			new Remote(name: name)
		}
	}
}
