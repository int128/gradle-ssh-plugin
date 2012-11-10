package org.hidetake.gradle.ssh

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle SSH plugin.
 * 
 * @author hidetake.org
 *
 */
class SshPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.extensions.create('ssh', SshPluginExtension)
		project.extensions.remotes = project.container(Remote) { String name ->
			new Remote(name: name)
		}
	}
}

/**
 * Global settings.
 * 
 * @author hidetake.org
 *
 */
class SshPluginExtension {
	/**
	 * JSch configuration.
	 */
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
