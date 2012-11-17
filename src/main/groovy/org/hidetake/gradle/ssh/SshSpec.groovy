package org.hidetake.gradle.ssh

/**
 * Specification of SSH operations.
 * 
 * @author hidetake.org
 *
 */
class SshSpec {
	/**
	 * Dry-run flag.
	 * If <code>true</code>, establishes connection but performs no action.
	 */
	boolean dryRun = false

	/**
	 * JSch configuration.
	 */
	final Map config = [:]

	/**
	 * Sessions.
	 */
	final List<SessionSpec> sessionSpecs = []

	/**
	 * Adds configuration. For example:
	 * <pre>
	 * config(StrictHostKeyChecking: 'no')
	 * </pre>
	 * 
	 * @param pairs key value pairs of configuration
	 */
	void config(Map pairs) {
		config.putAll(pairs)
	}

	/**
	 * Adds a session.
	 * 
	 * @param remote the remote
	 * @param operationClosure closure for {@link OperationSpec} (run in execution phase)
	 */
	void session(Remote remote, Closure operationClosure) {
		sessionSpecs.add(new SessionSpec(remote: remote, operationClosure: operationClosure))
	}
}
