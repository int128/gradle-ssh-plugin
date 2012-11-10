package org.hidetake.gradle.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Task to perform SSH operations.
 * 
 * @author hidetake.org
 *
 */
class Ssh extends DefaultTask implements SshSpec {
	List<SshSpec.SessionSpec> sessionSpecs = []

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
		sessionSpecs.add(new SshSpec.SessionSpec() {
			final Remote remote = aRemote
			final Closure operationClosure = aOperationClosure
		})
	}

	@TaskAction
	void perform() {
		if (dryRun) {
			dryRun()
		} else {
			run()
		}
	}

	protected void dryRun() {
		def executor = new OperationSpec() {
			@Override
			void execute(String command) {
				logger.warn ""
			}
			@Override
			void get(String remote, String local) {
			}
			@Override
			void put(String local, String remote) {
			}
		}
		sessionSpecs.each {
			executor.with(it.operationClosure)
		}
	}

	protected void run() {
		def executor = new Executor()
		executor.execute(this)
		executor.errorChannels.each {
			logger.error "Remote host returned status ${it.exitStatus} on channel #${it.id}"
		}.each {
			throw new GradleException('Remote host returned error status')
		}
	}
}
