package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.JSch
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.BackgroundCommandException

import static org.hidetake.groovy.ssh.util.Utility.retry

/**
 * A manager of {@link Connection}s.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class ConnectionManager implements UserAuthentication, HostAuthentication, ProxyConnection {
    protected static final LOCALHOST = '127.0.0.1'

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
     * @param remote the remote host
     * @return a connection
     */
    Connection connect(Remote remote) {
        def connection = new Connection(remote, connectViaGateway(remote))
        connections.add(connection)
        connection
    }

    /**
     * Establish a JSch session.
     *
     * @param remote target remote host
     * @return a JSch session
     */
    private connectViaGateway(Remote remote) {
        def settings = new ConnectionSettings.With(connectionSettings, remote)
        if (settings.gateway && settings.gateway != remote) {
            log.debug("Connecting to $remote via $settings.gateway")
            def session = connectViaGateway(settings.gateway)

            log.debug("Requesting port forwarding to $remote")
            def localPort = session.setPortForwardingL(0, remote.host, remote.port)
            log.info("Enabled local port forwarding from $LOCALHOST:$localPort to $remote")

            connectInternal(remote, LOCALHOST, localPort)
        } else {
            connectInternal(remote)
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
    private connectInternal(Remote remote, String host = remote.host, int port = remote.port) {
        def settings = new ConnectionSettings.With(connectionSettings, remote)
        log.debug("Connecting to $remote with $settings")

        validateHostAuthentication(settings, remote)
        validateUserAuthentication(settings, remote)
        validateProxyConnection(settings, remote)

        assert settings.retryCount   >= 0, "retryCount must be zero or positive (remote ${remote.name})"
        assert settings.retryWaitSec >= 0, "retryWaitSec must be zero or positive (remote ${remote.name})"
        assert settings.keepAliveSec >= 0, "keepAliveMillis must be zero or positive (remote ${remote.name})"

        retry(settings.retryCount, settings.retryWaitSec) {
            def jsch = new JSch()
            def session = jsch.getSession(settings.user, host, port)
            session.setServerAliveInterval(settings.keepAliveSec * 1000)
            session.timeout = settings.timeoutSec * 1000

            configureHostAuthentication(jsch, session, remote, settings)
            configureUserAuthentication(jsch, session, remote, settings)
            configureProxyConnection(jsch, session, remote, settings)

            session.connect()
            log.info("Connected to $remote")
            session
        }
    }

    /**
     * Wait for pending connections and close all.
     *
     * @throws BackgroundCommandException if any error occurs
     */
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
