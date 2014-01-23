package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.gradle.api.logging.Logging
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec

/**
 * Default implementation of {@link SshService}.
 *
 * @author hidetake.org
 *
 */
@Singleton
class DefaultSshService implements SshService {
    protected Closure<JSch> jschFactory = { new JSch() }

    static final logger = Logging.getLogger(DefaultSshService)

    @Override
    void execute(SshSpec sshSpec) {
        assert sshSpec.dryRun == Boolean.FALSE, 'dryRun should be false'

        def jsch = jschFactory()
        jsch.config.putAll(sshSpec.config)

        def sessions = [:] as Map<SessionSpec, Session>
        try {
            sshSpec.sessionSpecs.each { spec ->
                retry(sshSpec.retryCount, sshSpec.retryWaitSec) {
                    def session = jsch.getSession(spec.remote.user, spec.remote.host, spec.remote.port)
                    if (spec.remote.password) {
                        session.password = spec.remote.password
                    }

                    jsch.removeAllIdentity()
                    if (spec.remote.identity) {
                        jsch.addIdentity(spec.remote.identity.path, spec.remote.passphrase as String)
                    } else if (sshSpec.identity) {
                        jsch.addIdentity(sshSpec.identity.path, sshSpec.passphrase as String)
                    }

                    session.connect()
                    sessions.put(spec, session)
                }
            }

            def lifecycleManager = new SessionLifecycleManager()
            try {
                sessions.each { sessionSpec, session ->
                    def handler = new DefaultOperationHandler(sshSpec, sessionSpec, session, lifecycleManager)
                    handler.with(sessionSpec.operationClosure)
                }

                lifecycleManager.waitForPending { DefaultCommandContext context ->
                    logger.info("Channel #${context.channel.id} has been closed with exit status ${context.channel.exitStatus}")
                }
                lifecycleManager.validateExitStatus()
            } finally {
                lifecycleManager.disconnect()
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
     * @param closure
     */
    protected void retry(int retryCount, int retryWaitSec, Closure closure) {
        assert closure != null, 'closure should be set'
        if (retryCount > 0) {
            try {
                closure()
            } catch (JSchException e) {
                logger.warn "Retrying connection: ${e.getClass().name}: ${e.localizedMessage}"
                sleep(retryWaitSec * 1000L)
                retry(retryCount - 1, retryWaitSec, closure)
            }
        } else {
            closure()
        }
    }
}
