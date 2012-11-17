package org.hidetake.gradle.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.ssh.internal.Executor

/**
 * Task to perform SSH operations.
 * 
 * @author hidetake.org
 *
 */
class Ssh extends DefaultTask {
	protected Executor service = new Executor()

	@Delegate
	final SshSpec sshSpec = new SshSpec()

	@TaskAction
	void perform() {
		// TODO: merge global settings
		service.execute(sshSpec)
	}
}
