package org.hidetake.gradle.ssh

/**
 * Global settings.
 * 
 * @author hidetake.org
 *
 */
class SshPluginExtension {
	final Map config = [:]

	/**
	 * Dry-run flag.
	 * If <code>true</code>, establishes connection but performs no command or transfer.
	 * Default is <code>false</code>.
	 */
	boolean dryRun = false

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
}
