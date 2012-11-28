package org.hidetake.gradle.ssh.internal

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
		assert sshSpec.dryRun != null, 'default of dryRun should be set by convention'
		assert sshSpec.logger != null, 'default of logger should be set by convention'
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
				if (spec.remote.password) {
					session.password = spec.remote.password
				}
				if (spec.remote.identity) {
					session.identityRepository.add(spec.remote.identity.bytes)
				}
				session.connect()
				sessions.put(spec, session)
			}

			def channelsLifecycleManager = new ChannelsLifecycleManager()
			try {
				def operationEventLogger = new OperationEventLogger(sshSpec.logger, LogLevel.INFO)
				def exitStatusValidator = new ExitStatusValidator()
				sessions.each { spec, session ->
					def handler = new DefaultOperationHandler(spec, session)
					handler.listeners.add(channelsLifecycleManager)
					handler.listeners.add(operationEventLogger)
					handler.listeners.add(exitStatusValidator)
					handler.with(spec.operationClosure)
				}
				channelsLifecycleManager.waitForPending(exitStatusValidator)
			} finally {
				channelsLifecycleManager.disconnect()
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
