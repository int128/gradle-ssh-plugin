package org.hidetake.gradle.ssh

/**
 * Executor for {@link SshSpec}.
 * 
 * @author hidetake.org
 *
 */
interface Executor {
	/**
	 * Executes SSH.
	 *
	 * @param sshSpec
	 */
	void execute(SshSpec sshSpec)
}
