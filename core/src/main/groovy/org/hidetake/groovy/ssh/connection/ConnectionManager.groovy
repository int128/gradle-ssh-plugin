package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.*
import com.jcraft.jsch.Proxy as JschProxy
import com.jcraft.jsch.agentproxy.ConnectorFactory
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Proxy
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.session.BackgroundCommandException

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import static org.hidetake.groovy.ssh.core.ProxyType.SOCKS
import static org.hidetake.groovy.ssh.util.Utility.retry

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
        def settings = connectionSettings + remote.connectionSettings
        if (settings.gateway && settings.gateway != remote) {
            log.debug("Connecting to $remote via $settings.gateway")
            def session = establishViaGateway(settings.gateway)

            log.debug("Requesting port forwarding " +
                      "to $remote.name [$remote.host:$remote.port]")
            def localPort = session.setPortForwardingL(0, remote.host, remote.port)
            log.info("Enabled local port forwarding" +
                     "from $LOCALHOST:$localPort " +
                     "to $remote.name [$remote.host:$remote.port]")
            establishSession(remote, LOCALHOST, localPort)
        } else {
            log.debug("Connecting to $remote")
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
        log.debug("Connecting to $remote with $settings")

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
                log.warn("Strict host key checking is off. It may be vulnerable to man-in-the-middle attacks.")
            } else {
                jsch.setKnownHosts(settings.knownHosts.path)

                def keyTypes = findKeyTypes(session.hostKeyRepository, host, port).join(',')
                if (keyTypes) {
                    session.setConfig('server_host_key', keyTypes)
                    log.debug("Using key exhange algorithm for $remote.name: $keyTypes")
                }

                session.setConfig('StrictHostKeyChecking', 'yes')
                log.debug("Using known-hosts file for $remote.name: $settings.knownHosts.path")
            }

            if (settings.password) {
                session.password = settings.password
                log.debug("Using password authentication for $remote.name")
            }
            if (settings.agent) {
                jsch.identityRepository = remoteIdentityRepository
                log.debug("Using SSH agent authentication for $remote.name")
            } else {
                jsch.identityRepository = null    /* null means the default repository */
                jsch.removeAllIdentity()
                if (settings.identity) {
                    final identity = settings.identity
                    if (identity instanceof File) {
                        jsch.addIdentity(identity.path, settings.passphrase as String)
                        log.debug("Using public key authentication for $remote.name: $identity.path")
                    } else if (identity instanceof String) {
                        jsch.addIdentity("identity-${identity.hashCode()}", identity.bytes, null, settings.passphrase?.bytes)
                        log.debug("Using public key authentication for $remote.name")
                    }
                }
            }
            if (settings.proxy) {
                validate(settings.proxy)
                session.setProxy(asJschProxy(settings.proxy))
                log.debug("Using $settings.proxy.type proxy for $remote.name: $settings.proxy")
            }

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

    private static List<String> findKeyTypes(HostKeyRepository repository, String host, int port) {
        repository.hostKey.findAll { knownHostsItem ->
            knownHostsItem.host == host ||
            knownHostsItem.host == "[$host]:$port" as String ||
            compareHashedKnownHostsItem(knownHostsItem.host, host) ||
            compareHashedKnownHostsItem(knownHostsItem.host, "[$host]:$port")
        }.collect { knownHostsItem ->
            knownHostsItem.type
        }.unique()
    }

    private static boolean compareHashedKnownHostsItem(String knownHostsItem, String host) {
        def matched = false
        knownHostsItem.eachMatch(~/^\|1\|(.+?)\|(.+?)$/) { all, String salt, String hash ->
            matched = hmacSha1(salt.decodeBase64(), host.bytes) == hash.decodeBase64()
        }
        matched
    }

    private static hmacSha1(byte[] salt, byte[] data) {
        def key = new SecretKeySpec(salt, 'HmacSHA1')
        def mac = Mac.getInstance(key.algorithm)
        mac.init(key)
        mac.doFinal(data)
    }
}
