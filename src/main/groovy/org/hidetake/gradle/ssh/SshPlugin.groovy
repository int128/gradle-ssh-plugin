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
		project.extensions.remotes = project.container(Remote)
		project.convention.plugins.put('ssh', new SshPluginConvention())
	}
}
