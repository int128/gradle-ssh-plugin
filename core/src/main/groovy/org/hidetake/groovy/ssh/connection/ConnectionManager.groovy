package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.BackgroundCommandException
import org.hidetake.groovy.ssh.session.forwarding.LocalPortForwardSettings

import static org.hidetake.groovy.ssh.util.Utility.retry

/**
 * A manager of {@link Connection}s.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class ConnectionManager implements UserAuthentication, HostAuthentication, ProxyConnection {

    /**
     * Settings with default, global and per-service.
     */
    private final ConnectionSettings connectionSettings

    private final List<Connection> connections = []

    def ConnectionManager(ConnectionSettings connectionSettings1) {
        connectionSettings = connectionSettings1
        assert connectionSettings
    }

    /**
     * Establish a connection.
     *
     * @param remote target remote host
     * @return a connection
     */
    private Connection connect(Remote remote) {
        def settings = new ConnectionSettings.With(connectionSettings, remote)
        if (settings.gateway && settings.gateway != remote) {
            log.debug("Connecting to $remote via $settings.gateway")
            def gatewayConnection = connect(settings.gateway)

            log.debug("Requesting port forwarding to $remote")
            def localPort = gatewayConnection.forwardLocalPort(new LocalPortForwardSettings.With(
                host: remote.host, hostPort: remote.port, bind: '127.0.0.1', port: 0,
            ))
            log.info("Enabled local port forwarding from localhost:$localPort to $remote")

            connectInternal(remote, '127.0.0.1', localPort)
        } else {
            connectInternal(remote)
        }
    }

    /**
     * Establish a connection via given host and port.
     *
     * @param remote target remote host
     * @param host endpoint host (usually <code>remote.host</code>)
     * @param port endpoint port (usually <code>remote.port</code>)
     * @return a connection
     */
    private Connection connectInternal(Remote remote, String host = remote.host, int port = remote.port) {
        def settings = new ConnectionSettings.With(connectionSettings, remote)
        log.debug("Connecting to $remote with $settings")

        validateHostAuthentication(settings, remote)
        validateUserAuthentication(settings, remote)
        validateProxyConnection(settings, remote)

        assert settings.retryCount   >= 0, "retryCount must be zero or positive (remote ${remote.name})"
        assert settings.retryWaitSec >= 0, "retryWaitSec must be zero or positive (remote ${remote.name})"
        assert settings.keepAliveSec >= 0, "keepAliveMillis must be zero or positive (remote ${remote.name})"

        retry(settings.retryCount, settings.retryWaitSec) {
            try {
                connectInternal(remote, host, port, settings)
            } catch (JSchException e) {
                if (settings.knownHosts instanceof AddHostKey && e.message.startsWith('UnknownHostKey')) {
                    log.info(e.message)

                    configureToAddNewHostKey(settings)
                    connectInternal(remote, host, port, settings)
                } else {
                    throw e
                }
            }
        }
    }

    private Connection connectInternal(Remote remote, String host, int port, ConnectionSettings settings) {
        def jsch = new JSch()
        def session = jsch.getSession(settings.user, host, port)
        session.setServerAliveInterval(settings.keepAliveSec * 1000)
        session.timeout = settings.timeoutSec * 1000

        configureHostAuthentication(jsch, session, remote, settings)
        configureUserAuthentication(jsch, session, remote, settings)
        configureProxyConnection(jsch, session, remote, settings)

        session.connect()

        log.info("Connected to $remote (${session.serverVersion})")
        def connection = new Connection(remote, session)
        connections.add(connection)
        connection
    }

    /**
     * Wait for pending connections and close all.
     *
     * @throws BackgroundCommandException if any error occurs
     */
    void waitAndClose() {
        try {
            log.debug("Waiting for connections: $connections")
            waitForPending()
        } finally {
            log.debug("Closing connections: $connections")
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
