package org.hidetake.gradle.ssh

/**
 * Specification of SSH operations.
 * 
 * @author hidetake.org
 *
 */
interface SshSpec {
	/**
	 * Dry-run flag.
	 * If <code>true</code>, establishes connection but performs no action.
	 */
	boolean isDryRun()

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
