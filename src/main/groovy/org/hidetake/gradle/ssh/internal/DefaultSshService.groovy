package org.hidetake.gradle.ssh.internal

import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 * Default implementation of {@link SshService}.
 * 
 * @author hidetake.org
 *
 */
@Singleton
class DefaultSshService implements SshService {
	protected Closure<JSch> jschFactory = { new JSch() }

	@Override
	void execute(SshSpec sshSpec) {
		if (sshSpec.dryRun) {
			dryRun(sshSpec)
		} else {
			wetRun(sshSpec)
		}
	}

	/**
	 * Opens sessions and performs each operations.
	 *
	 * @param sshSpec
	 */
	void wetRun(SshSpec sshSpec) {
		JSch jsch = jschFactory()
		jsch.config.putAll(sshSpec.config)

		Map<SessionSpec, Session> sessions = [:]
		try {
			sshSpec.sessionSpecs.each { spec ->
				def session = jsch.getSession(spec.remote.user, spec.remote.host, spec.remote.port)
				session.password = spec.remote.password
				session.identityRepository.add(spec.remote.identity.bytes)
				session.connect()
				sessions.put(spec, session)
			}

			def unmanagedChannelsManager = new UnmanagedChannelsManager()
			try {
				def operationEventLogger = new OperationEventLogger(sshSpec.logger, LogLevel.INFO)
				sessions.each { spec, session ->
					def handler = new DefaultOperationHandler(spec, session)
					handler.listeners.add(unmanagedChannelsManager)
					handler.listeners.add(operationEventLogger)
					handler.with(spec.operationClosure)
				}
				while (unmanagedChannelsManager.pending) {
					Thread.sleep(500L)
				}
				def errorChannels = unmanagedChannelsManager.errorChannels
				if (errorChannels.size() > 0) {
					throw new GradleException(errorChannels.collect {
						"Channel #${it.id} returned status ${it.exitStatus}"}.join('\n'))
				}
			} finally {
				unmanagedChannelsManager.disconnect()
			}
		} finally {
			sessions.each { spec, session -> session.disconnect() }
		}
	}

	/**
	 * Performs no action.
	 * 
	 * @param sshSpec
	 */
	void dryRun(SshSpec sshSpec) {
		def operationEventLogger = new OperationEventLogger(sshSpec.logger, LogLevel.WARN)
		sshSpec.sessionSpecs.each { spec ->
			def handler = new DryRunOperationHandler()
			handler.listeners.add(operationEventLogger)
			handler.with(spec.operationClosure)
		}
	}
}
