package org.hidetake.gradle.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec
import org.hidetake.gradle.ssh.internal.DefaultSshService

/**
 * SSH task.
 * 
 * @see SshService
 * @author hidetake.org
 *
 */
class SshTask extends DefaultTask {
	protected SshService service = DefaultSshService.instance

	/**
	 * Delegate of task specific settings.
	 * This overrides global settings.
	 */
	@Delegate
	final SshSpec sshSpec = new SshSpec()

	@TaskAction
	void perform() {
		SshPluginConvention convention = project.convention.getPlugin(SshPluginConvention)
		service.execute(SshSpec.computeMerged(sshSpec, convention.sshSpec))
	}
}
