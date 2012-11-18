package org.hidetake.gradle.ssh.api

import org.gradle.api.logging.Logger

/**
 * Specification of a SSH task.
 * 
 * @author hidetake.org
 *
 */
class SshSpec {
	/**
	 * Dry-run flag.
	 * If <code>true</code>, performs no action.
	 */
	Boolean dryRun = null

	/**
	 * Logger.
	 */
	Logger logger = null

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

	/**
	 * Computes merged settings.
	 * 
	 * @param specs list of {@link SshSpec}s in priority order (first item is highest priority)
	 * @return merged one
	 */
	static SshSpec computeMerged(SshSpec... specs) {
		def merged = new SshSpec()
		specs.reverse().each { spec ->
			merged.config.putAll(spec.config)
			merged.sessionSpecs.addAll(spec.sessionSpecs)
		}
		merged.dryRun = specs.collect { it.dryRun }.findResult(false) { it }
		merged.logger = specs.collect { it.logger }.find()
		merged
	}
}
