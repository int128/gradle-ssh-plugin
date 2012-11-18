package org.hidetake.gradle.ssh

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.hidetake.gradle.ssh.internal.DefaultExecutor

/**
 * Convention of this plugin.
 * 
 * @author hidetake.org
 *
 */
class SshPluginConvention {
	protected final SshSpec sshSpec = new SshSpec()
	protected Executor service = DefaultExecutor.instance

	/**
	 * Configures global settings.
	 * 
	 * @param configure closure for {@link SshSpec}
	 */
	void ssh(Closure configure) {
		ConfigureUtil.configure(configure, sshSpec)
	}

	/**
	 * Executes SSH.
	 * 
	 * @param configure configuration closure for {@link SshSpec}
	 */
	void sshexec(Closure configure) {
		SshSpec localSpec = new SshSpec()
		ConfigureUtil.configure(configure, localSpec)
		service.execute(SshSpec.computeMerged(sshSpec, localSpec))
	}
}
