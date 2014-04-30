package org.hidetake.gradle.ssh.internal.ssh

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.agentproxy.ConnectorFactory
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.ssh.BackgroundCommandException
import org.hidetake.gradle.ssh.api.ssh.Connection
import org.hidetake.gradle.ssh.api.ssh.ConnectionManager
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings

import static Retry.retry

/**
 * A default implementation of {@link ConnectionManager}.
 *
 * @author hidetake.org
 */
@Slf4j
class DefaultConnectionManager implements ConnectionManager {
    protected static final LOCALHOST = '127.0.0.1'

    private final JSch jsch = new JSch()
    private final List<Session> sessions = []
    private final List<Connection> connections = []
    private final ConnectionSettings globalSettings

    def DefaultConnectionManager(ConnectionSettings globalSettings1) {
        globalSettings = globalSettings1
        assert globalSettings
    }

    @Override
    Connection establish(Remote remote) {
        def connection = new DefaultConnection(remote, establishViaGateway(remote))
        connections.add(connection)
        connection
    }

    /**
     * Establish a JSch session.
     *
     * @param remote target remote host
     * @return a JSch session
     */
    private Session establishViaGateway(Remote remote) {
        if (remote.gateway) {
            def session = establishViaGateway(remote.gateway)
            def localPort = session.setPortForwardingL(0, remote.host, remote.port)
            log.info("Enabled local port forwarding from $localPort to ${remote.host}:${remote.port}")
            establishSession(remote, LOCALHOST, localPort)
        } else {
            establishSession(remote, remote.host, remote.port)
        }
    }

    /**
     * Establish a JSch session via given host and port.
     *
     * @param remote target remote host
     * @param host endpoint host (usually <code>remote.host</code>)
     * @param port endpoint port (usually <code>remote.port</code>)
     * @return a JSch session
     */
    private Session establishSession(Remote remote, String host, int port) {
        def settings = globalSettings + remote.connectionSettings

        assert settings.user, "user must be given (remote ${remote.name})"
        assert settings.knownHosts   != null, 'knownHosts must not be null'
        assert settings.retryCount   != null, 'retryCount must not be null'
        assert settings.retryWaitSec != null, 'retryWaitSec must not be null'
        assert settings.retryCount   >= 0, "retryCount must be zero or positive (remote ${remote.name})"
        assert settings.retryWaitSec >= 0, "retryWaitSec must be zero or positive (remote ${remote.name})"

        if (settings.knownHosts == ConnectionSettings.allowAnyHosts) {
            jsch.setConfig('StrictHostKeyChecking', 'no')
            log.info('Strict host key checking is turned off. Use only for testing purpose.')
        } else {
            jsch.setKnownHosts(settings.knownHosts.path)
            jsch.setConfig('StrictHostKeyChecking', 'yes')
            log.debug("Using known-hosts file: ${settings.knownHosts.path}")
        }

        retry(settings.retryCount, settings.retryWaitSec) {
            def session = jsch.getSession(settings.user, host, port)
            if (settings.password) {
                session.password = settings.password
            }

            if (settings.agent) {
                jsch.identityRepository = remoteIdentityRepository
            } else {
                jsch.identityRepository = null    /* null means the default repository */
                jsch.removeAllIdentity()
                if (settings.identity) {
                    jsch.addIdentity(settings.identity.path, settings.passphrase as String)
                }
            }

            session.setConfig('PreferredAuthentications', 'publickey,keyboard-interactive,password')

            session.connect()
            log.info("Established a session to $remote via $host:$port")
            sessions.add(session)
            session
        }
    }

    @Override
    void waitForPending() {
        List<Exception> exceptions = []
        while (connections*.anyPending.any()) {
            connections.each { connection ->
                try {
                    connection.executeCallbackForClosedChannels()
                } catch (BackgroundCommandException e) {
                    exceptions.addAll(e.exceptionsOfBackgroundExecution)
                }
            }
            sleep(100)
        }
        connections.each { connection ->
            try {
                connection.executeCallbackForClosedChannels()
            } catch (BackgroundCommandException e) {
                exceptions.addAll(e.exceptionsOfBackgroundExecution)
            }
        }

        if (!exceptions.empty) {
            throw new BackgroundCommandException(exceptions)
        }
    }

    @Override
    void cleanup() {
        connections*.cleanup()
        connections.clear()

        sessions*.disconnect()
        sessions.clear()
    }

    @Lazy
    protected remoteIdentityRepository = {
        def connectorFactory = ConnectorFactory.getDefault()
        def connector = connectorFactory.createConnector()
        new RemoteIdentityRepository(connector)
    }()
}
