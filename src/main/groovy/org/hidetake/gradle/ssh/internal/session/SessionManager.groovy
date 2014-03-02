package org.hidetake.gradle.ssh.internal.session

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.agentproxy.ConnectorFactory
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SshSpec

import static org.hidetake.gradle.ssh.internal.session.Retry.retry

/**
 * Factory and lifecycle manager class of JSch session.
 *
 * @author hidetake.org
 */
@Slf4j
class SessionManager {
    final SshSpec sshSpec
    final JSch jsch
    final List<Session> sessions = []

    /**
     * Constructor.
     *
     * @param sshSpec ssh spec
     * @return a SessionManager instance
     */
    def SessionManager(SshSpec sshSpec1) {
        sshSpec = sshSpec1
        jsch = new JSch()

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
    }

    /**
     * Establish a JSch session.
     *
     * @param spec session spec
     * @return a JSch session
     */
    Session create(Remote remote) {
        retry(sshSpec.retryCount, sshSpec.retryWaitSec) {
            def session = jsch.getSession(remote.user, remote.host, remote.port)
            if (remote.password) {
                session.password = remote.password
            }
            jsch.removeAllIdentity()
            if (remote.identity) {
                jsch.addIdentity(remote.identity.path, remote.passphrase as String)
            } else if (sshSpec.identity) {
                jsch.addIdentity(sshSpec.identity.path, sshSpec.passphrase as String)
            } else if (remote.agent) {
                def connectorFactory = ConnectorFactory.getDefault()
                def connector = connectorFactory.createConnector()
                def identityRepository = new RemoteIdentityRepository(connector)
                jsch.setIdentityRepository(identityRepository)
            }

            session.connect()
            sessions.add(session)
            session
        }
    }

    /**
     * Disconnect all sessions.
     */
    void disconnect() {
        sessions*.disconnect()
    }
}
