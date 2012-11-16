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
class Ssh extends DefaultTask implements SshHandler {
	protected final List<SessionSpec> sessionSpecs = []
	protected final Map config = [:]

	Boolean dryRun = null

	@Override
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun
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
		Executor.instance.execute(computeSpec())
	}

	protected SshSpec computeSpec() {
		SshPluginExtension global = project.extensions.getByType(SshPluginExtension)
		Ssh local = this
		new SshSpec() {
			final boolean dryRun = (local.dryRun == null) ? global.dryRun : local.dryRun
			final Map config = merge(global.config, local.config)
			final List<SessionSpec> sessionSpecs = local.sessionSpecs
		}
	}

	protected static Map merge(Map... maps) {
		maps.inject([:]) { x, y ->
			x.putAll(y)
			x
		}
	}
}
