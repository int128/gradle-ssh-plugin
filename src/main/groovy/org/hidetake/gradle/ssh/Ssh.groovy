package org.hidetake.gradle.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.ssh.internal.DefaultExecutor

/**
 * Task to perform SSH operations.
 * 
 * Global settings can be override by this task.
 * 
 * @author hidetake.org
 *
 */
class Ssh extends DefaultTask {
	protected Executor service = DefaultExecutor.instance

	@Delegate
	final SshSpec sshSpec = new SshSpec()

	@TaskAction
	void perform() {
		SshPluginConvention convention = project.convention.getPlugin(SshPluginConvention)
		service.execute(SshSpec.computeMerged(convention.sshSpec, sshSpec))
	}
}
