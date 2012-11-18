package org.hidetake.gradle.ssh.api

/**
 * Service to execute a SSH task specified by {@link SshSpec}.
 * 
 * @see SshSpec
 * @author hidetake.org
 *
 */
interface SshService {
	/**
	 * Executes a SSH task.
	 *
	 * @param sshSpec specification
	 */
	void execute(SshSpec sshSpec)
}
