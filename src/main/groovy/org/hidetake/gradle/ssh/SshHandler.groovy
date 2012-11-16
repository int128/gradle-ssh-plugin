package org.hidetake.gradle.ssh

/**
 * Handler for the SSH task closure.
 * 
 * @author hidetake.org
 *
 */
interface SshHandler {
	/**
	 * Dry-run flag.
	 * If <code>true</code>, establishes connection but performs no action.
	 */
	void setDryRun(boolean dryRun)

	/**
	 * Adds configuration. For example:
	 * <pre>
	 * config(StrictHostKeyChecking: 'no')
	 * </pre>
	 * 
	 * @param pairs key value pairs of configuration
	 */
	void config(Map pairs)

	/**
	 * Adds a session.
	 * 
	 * @param remote the remote
	 * @param operationClosure closure for {@link OperationSpec} (run in execution phase)
	 */
	void session(Remote remote, Closure operationClosure)
}
