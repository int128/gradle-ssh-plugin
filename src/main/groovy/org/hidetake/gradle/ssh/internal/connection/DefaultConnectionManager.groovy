package org.hidetake.gradle.ssh.internal.connection

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.agentproxy.ConnectorFactory
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.plugin.ConnectionSettings
import org.hidetake.gradle.ssh.plugin.Remote
import org.hidetake.gradle.ssh.plugin.session.BackgroundCommandException

import static Retry.retry

/**
 * A default implementation of {@link ConnectionManager}.
 *
 * @author hidetake.org
 */
@Slf4j
class DefaultConnectionManager implements ConnectionManager {
    protected static final LOCALHOST = '127.0.0.1'

    private final ConnectionSettings connectionSettings
    private final JSch jsch = new JSch()
    private final List<Connection> connections = []

    @Lazy
    protected remoteIdentityRepository = {
        def connectorFactory = ConnectorFactory.getDefault()
        def connector = connectorFactory.createConnector()
        new RemoteIdentityRepository(connector)
    }()

    def DefaultConnectionManager(ConnectionSettings connectionSettings1) {
        connectionSettings = connectionSettings1
        assert connectionSettings
    }

    @Override
    Connection connect(Remote remote) {
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
        def settings = connectionSettings + remote.connectionSettings

        assert settings.user, "user must be given (remote ${remote.name})"
        assert settings.knownHosts   != null, 'knownHosts must not be null'
        assert settings.retryCount   != null, 'retryCount must not be null'
        assert settings.retryWaitSec != null, 'retryWaitSec must not be null'
        assert settings.retryCount   >= 0, "retryCount must be zero or positive (remote ${remote.name})"
        assert settings.retryWaitSec >= 0, "retryWaitSec must be zero or positive (remote ${remote.name})"

        if (settings.knownHosts == ConnectionSettings.DEFAULT.allowAnyHosts) {
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
            session
        }
    }

    @Override
    void waitAndClose() {
        try {
            waitForPending()
        } finally {
            connections*.close()
            connections.clear()
        }
    }

    private void waitForPending() {
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
}
