package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Proxy as JschProxy
import com.jcraft.jsch.ProxyHTTP
import com.jcraft.jsch.ProxySOCKS4
import com.jcraft.jsch.ProxySOCKS5
import com.jcraft.jsch.Session
import com.jcraft.jsch.agentproxy.ConnectorFactory
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.settings.ConnectionSettings
import org.hidetake.groovy.ssh.core.Proxy
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.BackgroundCommandException

import static org.hidetake.groovy.ssh.util.Utility.retry
import static org.hidetake.groovy.ssh.core.ProxyType.SOCKS

/**
 * A manager of {@link Connection}s.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class ConnectionManager {
    protected static final LOCALHOST = '127.0.0.1'

    private final ConnectionSettings connectionSettings
    private final List<Connection> connections = []

    @Lazy
    protected remoteIdentityRepository = {
        def connectorFactory = ConnectorFactory.getDefault()
        def connector = connectorFactory.createConnector()
        new RemoteIdentityRepository(connector)
    }()

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
        def connection = new Connection(remote, establishViaGateway(remote))
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
            log.debug("Establishing a connection to $remote via $remote.gateway")
            def session = establishViaGateway(remote.gateway)
            def localPort = session.setPortForwardingL(0, remote.host, remote.port)
            log.info("Enabled local port forwarding from $localPort to ${remote.host}:${remote.port}")
            establishSession(remote, LOCALHOST, localPort)
        } else {
            log.debug("Establishing a connection to $remote")
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
        assert settings.keepAliveSec != null, 'keepAliveMillis must not be null'
        assert settings.retryCount   >= 0, "retryCount must be zero or positive (remote ${remote.name})"
        assert settings.retryWaitSec >= 0, "retryWaitSec must be zero or positive (remote ${remote.name})"
        assert settings.keepAliveSec >= 0, "keepAliveMillis must be zero or positive (remote ${remote.name})"
        assert settings.identity instanceof File || settings.identity instanceof String || settings.identity == null,
                'identity must be a File, String or null'

        JSch.logger = JSchLogger.instance

        retry(settings.retryCount, settings.retryWaitSec) {
            def jsch = new JSch()
            def session = jsch.getSession(settings.user, host, port)
            session.setConfig('PreferredAuthentications', 'publickey,keyboard-interactive,password')
            session.setServerAliveInterval(settings.keepAliveSec * 1000)

            if (settings.knownHosts == ConnectionSettings.Constants.allowAnyHosts) {
                session.setConfig('StrictHostKeyChecking', 'no')
                log.warn('Strict host key checking is off. It may be vulnerable to man-in-the-middle attacks.')
            } else {
                jsch.setKnownHosts(settings.knownHosts.path)
                session.setConfig('StrictHostKeyChecking', 'yes')
                log.debug("Using known-hosts file: ${settings.knownHosts.path}")
            }

            if (settings.password) {
                session.password = settings.password
            }
            if (settings.agent) {
                jsch.identityRepository = remoteIdentityRepository
            } else {
                jsch.identityRepository = null    /* null means the default repository */
                jsch.removeAllIdentity()
                if (settings.identity) {
                    final identity = settings.identity
                    if (identity instanceof File) {
                        jsch.addIdentity(identity.path, settings.passphrase as String)
                    } else if (identity instanceof String) {
                        jsch.addIdentity("identity-${identity.hashCode()}", identity.bytes, null, settings.passphrase?.bytes)
                    }
                }
            }
            if (settings.proxy) {
                validate(settings.proxy)
                session.setProxy(asJschProxy(settings.proxy))
            }

            log.debug("Establishing a connection to $remote")
            session.connect()
            log.info("Established the connection to $remote")
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
        log.debug("Waiting for pending operations")

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

        log.debug('Finished all operations including background commands')

        if (!exceptions.empty) {
            throw new BackgroundCommandException(exceptions)
        }
    }

    private static JschProxy asJschProxy(Proxy proxy) {
        def jschProxy = {
            if (proxy.type == SOCKS) {
                if (proxy.socksVersion == 5) {
                    new ProxySOCKS5(proxy.host, proxy.port)
                } else {
                    new ProxySOCKS4(proxy.host, proxy.port)
                }
            } else {
                new ProxyHTTP(proxy.host, proxy.port)
            }
        }()
        jschProxy.setUserPasswd(proxy.user, proxy.password)
        jschProxy
    }

	private static void validate(Proxy proxy) {
		def validator = new ProxyValidator(proxy)
		if (validator.error()) {
			throw new IllegalArgumentException(validator.error())
		}
		if (validator.warnings()) {
			validator.warnings().each { warning -> log.info(warning) }
		}
	}	
}
