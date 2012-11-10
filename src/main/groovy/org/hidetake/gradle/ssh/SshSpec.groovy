package org.hidetake.gradle.ssh

/**
 * Specification of a SSH task.
 * 
 * @author hidetake.org
 *
 */
interface SshSpec {
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

	/**
	 * Returns configuration.
	 * 
	 * @return key value pairs of configuration
	 */
	Map getConfig()

	/**
	 * Returns sessions.
	 * 
	 * @return
	 */
	List<SessionSpec> getSessionSpecs()
}
