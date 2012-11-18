package org.hidetake.gradle.ssh

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec
import org.hidetake.gradle.ssh.internal.DefaultSshService

/**
 * Convention properties and methods.
 * 
 * @author hidetake.org
 *
 */
class SshPluginConvention {
	protected final SshSpec sshSpec = new SshSpec()
	protected SshService service = DefaultSshService.instance

	SshPluginConvention(Project project) {
		sshSpec.logger = project.logger
	}

	/**
	 * Configures global settings.
	 * 
	 * @param configure closure for {@link SshSpec}
	 */
	void ssh(Closure configure) {
		ConfigureUtil.configure(configure, sshSpec)
		if (sshSpec.sessionSpecs.size() > 0) {
			throw new IllegalStateException('Do not declare any session in convention')
		}
	}

	/**
	 * Executes SSH operations instead of a project task.
	 * 
	 * @param configure configuration closure for {@link SshSpec}
	 */
	void sshexec(Closure configure) {
		SshSpec localSpec = new SshSpec()
		ConfigureUtil.configure(configure, localSpec)
		service.execute(SshSpec.computeMerged(localSpec, sshSpec))
	}
}
