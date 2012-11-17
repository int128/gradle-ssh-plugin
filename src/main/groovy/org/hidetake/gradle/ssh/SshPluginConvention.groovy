package org.hidetake.gradle.ssh

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

/**
 * Convention of this plugin.
 * 
 * @author hidetake.org
 *
 */
class SshPluginConvention {
	protected final Project project
	protected final SshSpec sshSpec = new SshSpec()

	SshPluginConvention(Project project) {
		this.project = project
	}

	/**
	 * Configure global settings.
	 * 
	 * @param configure closure for {@link SshSpec}
	 */
	void ssh(Closure configure) {
		ConfigureUtil.configure(configure, sshSpec)
	}

	void sshexec(Closure configure) {
		// TODO
	}
}
