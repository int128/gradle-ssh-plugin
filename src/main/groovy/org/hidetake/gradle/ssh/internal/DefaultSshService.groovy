package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.agentproxy.ConnectorFactory
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec
import org.hidetake.gradle.ssh.internal.session.ChannelManager

/**
 * Default implementation of {@link SshService}.
 *
 * @author hidetake.org
 *
 */
@Singleton
@Slf4j
class DefaultSshService implements SshService {
    protected Closure<JSch> jschFactory = { new JSch() }

    @Lazy
    protected remoteIdentityRepository = {
        def connectorFactory = ConnectorFactory.getDefault()
        def connector = connectorFactory.createConnector()
        new RemoteIdentityRepository(connector)
    }()

    @Override
    void execute(SshSpec sshSpec) {
        assert sshSpec.dryRun == Boolean.FALSE, 'dryRun should be false'

        def jsch = jschFactory()

        // TODO: for backward compatibility, to be removed in v0.3.0
        sshSpec.config.each { k, v ->
            if ([k, v] == ['StrictHostKeyChecking', 'no']) {
                sshSpec.knownHosts = SshSpec.allowAnyHosts
                log.warn("Deprecated: Use `knownHosts = allowAnyHosts` instead of `config($k: '$v')`")
            } else {
                jsch.setConfig(k, v.toString())
                log.warn("Deprecated: JSch config `$k` will be no longer supported in v0.3.0")
            }
        }

        if (sshSpec.knownHosts == SshSpec.allowAnyHosts) {
            jsch.setConfig('StrictHostKeyChecking', 'no')
            log.info('Strict host key checking is turned off. Use only for testing purpose.')
        } else {
            jsch.setKnownHosts(sshSpec.knownHosts.path)
            jsch.setConfig('StrictHostKeyChecking', 'yes')
            log.debug("Using known-hosts file: ${sshSpec.knownHosts.path}")
        }

        def sessions = [:] as Map<SessionSpec, Session>
        try {
            sshSpec.sessionSpecs.each { spec ->
                retry(sshSpec.retryCount, sshSpec.retryWaitSec) {
                    def session = jsch.getSession(spec.remote.user, spec.remote.host, spec.remote.port)
                    if (spec.remote.password) {
                        session.password = spec.remote.password
                    }

                    if (spec.remote.agent) {
                        jsch.identityRepository = remoteIdentityRepository
                    } else {
                        jsch.identityRepository = null    /* null means the default repository */
                        jsch.removeAllIdentity()
                        if (spec.remote.identity) {
                            jsch.addIdentity(spec.remote.identity.path, spec.remote.passphrase as String)
                        } else if (sshSpec.identity) {
                            jsch.addIdentity(sshSpec.identity.path, sshSpec.passphrase as String)
                        }
                    }

                    session.connect()
                    sessions.put(spec, session)
                }
            }

            def channelManager = new ChannelManager()
            try {
                sessions.each { sessionSpec, session ->
                    def handler = new DefaultOperationHandler(sshSpec, sessionSpec, session, channelManager)
                    handler.with(sessionSpec.operationClosure)
                }

                channelManager.waitForPending { Channel channel ->
                    log.info("Channel #${channel.id} has been closed with exit status ${channel.exitStatus}")
                }
                channelManager.validateExitStatus()
            } finally {
                channelManager.disconnect()
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
                log.warn("Retrying connection: ${e.getClass().name}: ${e.localizedMessage}")
                sleep(retryWaitSec * 1000L)
                retry(retryCount - 1, retryWaitSec, closure)
            }
        } else {
            closure()
        }
    }
}
