package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec
import org.slf4j.Logger

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
        assert sshSpec.dryRun == Boolean.FALSE, 'dryRun should be false'
        assert sshSpec.logger != null, 'default logger should be set by convention'

        def jsch = jschFactory()
        jsch.config.putAll(sshSpec.config)

        def sessions = [:] as Map<SessionSpec, Session>
        try {
            sshSpec.sessionSpecs.each { spec ->
                retry(sshSpec.retryCount, sshSpec.retryWaitSec, sshSpec.logger) {
                    def session = jsch.getSession(spec.remote.user, spec.remote.host, spec.remote.port)
                    if (spec.remote.password) {
                        session.password = spec.remote.password
                    }
                    if (spec.remote.identity) {
                        // TODO: below impacts on global, but should be session-specific

                        if (spec.remote.passphrase) {
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
                sessions.each { sessionSpec, session ->
                    def handler = new DefaultOperationHandler(sshSpec, sessionSpec, session)
                    handler.listeners.add(channelsLifecycleManager)
                    handler.listeners.add(operationEventLogger)
                    handler.listeners.add(exitStatusValidator)
                    handler.with(sessionSpec.operationClosure)
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
     * Execute the closure with retrying.
     * This method catches only {@link JSchException}s.
     *
     * @param retryCount
     * @param retryWaitSec
     * @param logger logger (this is SLF4J logger, not Gradle logger)
     * @param closure
     */
    protected void retry(int retryCount, int retryWaitSec, Logger logger, Closure closure) {
        assert closure != null, 'closure should be set'
        if (retryCount > 0) {
            try {
                closure()
            } catch (JSchException e) {
                logger.warn "Retrying connection: ${e.getClass().name}: ${e.localizedMessage}"
                Thread.sleep(retryWaitSec * 1000L)
                retry(retryCount - 1, retryWaitSec, logger, closure)
            }
        } else {
            closure()
        }
    }
}
