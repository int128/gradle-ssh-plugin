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
	 * @param conventionSpec global settings
	 * @param taskSpecificSpec task specific settings
	 * @return merged one
	 */
	static SshSpec computeMerged(SshSpec conventionSpec, SshSpec taskSpecificSpec) {
		def merged = new SshSpec()
		merged.config.putAll(conventionSpec.config)
		merged.config.putAll(taskSpecificSpec.config)
		if (conventionSpec.sessionSpecs.size() > 0) {
			throw new IllegalArgumentException('Do not declare any session in the ssh convention.')
		}
		merged.sessionSpecs.addAll(taskSpecificSpec.sessionSpecs)
		merged.dryRun = {
			if (taskSpecificSpec.dryRun != null) {
				taskSpecificSpec.dryRun
			} else if (conventionSpec.dryRun != null) {
				conventionSpec.dryRun
			} else {
				false
			}
		}()
		merged.logger = {
			if (taskSpecificSpec.logger != null) {
				taskSpecificSpec.logger
			} else if (conventionSpec.logger != null) {
				conventionSpec.logger
			} else {
				throw new IllegalStateException('property logger in convention is null')
			}
		}()
		merged
	}
}
