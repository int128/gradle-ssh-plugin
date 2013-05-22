package org.hidetake.gradle.ssh.internal

import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec
import org.slf4j.Logger

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
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
				retry(sshSpec.retryCount, sshSpec.retryWaitSec, sshSpec.logger) {
					def session = jsch.getSession(spec.remote.user, spec.remote.host, spec.remote.port)
					if (spec.remote.password) {
						session.password = spec.remote.password
					}
					if (spec.remote.identity) {
                        // TODO: below impacts on global, but should be session-specific

                        if(spec.remote.passphrase) {
                            jsch.addIdentity(spec.remote.identity.path, spec.remote.passphrase)
                        } else {
                            jsch.addIdentity(spec.remote.identity.path)
                        }
					}
					session.connect()
					sessions.put(spec, session)
				}
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
			def handler = new DryRunOperationHandler(spec, [operationEventLogger])
			println handler
            handler.with(spec.operationClosure)
		}
	}

	/**
	 * Execute the closure with retrying.
	 * This method catches only {@link JSchException}s.
	 * 
	 * @param retryCount
	 * @param retryWaitSec
	 * @param logger logger (this is SLF4J logger, not Gradle logger)
	 * @param closure
	 */
	protected void retry(int retryCount, int retryWaitSec, Logger logger, Closure closure) {
		assert closure != null, 'closure should not be null'
		if (retryCount > 0) {
			try {
				closure()
			} catch(JSchException e) {
				logger.warn "Retrying connection: ${e.getClass().name}: ${e.localizedMessage}"
				Thread.sleep(retryWaitSec * 1000L)
				retry(retryCount - 1, retryWaitSec, logger, closure)
			}
		} else {
			closure()
		}
	}
}
