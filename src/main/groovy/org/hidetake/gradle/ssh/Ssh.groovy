package org.hidetake.gradle.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hidetake.gradle.ssh.internal.Executor

/**
 * Task to perform SSH operations.
 * 
 * @author hidetake.org
 *
 */
class Ssh extends DefaultTask implements SshSpec {
	List<SessionSpec> sessionSpecs = []

	/**
	 * JSch configuration.
	 */
	Map config = [:]

	/**
	 * Dry-run flag.
	 * If <code>true</code>, establishes connection but performs no command or transfer.
	 */
	boolean dryRun

	/**
	 * Initializes properties by global settings.
	 */
	{
		dryRun = project.extensions.getByType(SshPluginExtension).dryRun
		config.putAll(project.extensions.getByType(SshPluginExtension).config)
	}

	@Override
	void config(Map pairs) {
		config.putAll(pairs)
	}

	@Override
	void session(Remote aRemote, Closure aOperationClosure) {
		sessionSpecs.add(new SessionSpec() {
			final Remote remote = aRemote
			final Closure operationClosure = aOperationClosure
		})
	}

	@TaskAction
	void perform() {
		if (dryRun) {
			dryRun()
		} else {
			Executor.instance.execute(this)
		}
	}

	protected void dryRun() {
		def executor = new OperationHandler() {
			@Override
			void execute(String command) {
				// TODO: logger.warn()
			}
			@Override
			void executeBackground(String command) {
				// TODO: logger.warn()
			}
			@Override
			void get(String remote, String local) {
				// TODO: logger.warn()
			}
			@Override
			void put(String local, String remote) {
				// TODO: logger.warn()
			}
		}
		sessionSpecs.each {
			executor.with(it.operationClosure)
		}
	}
}
