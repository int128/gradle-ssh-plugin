package org.hidetake.gradle.ssh.server

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.security.PublicKey

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A helper class for server-based integration test.
 *
 * @author hidetake.org
 *
 */
class IntegrationTestHelper {
    final Logger logger = LoggerFactory.getLogger(IntegrationTestHelper)
    final SshServer server = SshServer.setUpDefaultServer()

    static final String DEFAULT_HOSTKEY = 'build/integration-test.host.key'

    /**
     * Host key file of the SSH server.
     * Default is {@value IntegrationTestHelper#DEFAULT_HOSTKEY}.
     */
    String hostkey = DEFAULT_HOSTKEY

    boolean authenticatedByPassword = false
    boolean authenticatedByPublickey = false
    List<String> requestedCommands = []

    IntegrationTestHelper() {
        server.host = 'localhost'
        server.port = pickUpFreePort()
        server.keyPairProvider = new SimpleGeneratorHostKeyProvider(hostkey)
    }

    /**
     * Enables password authentication.
     * If credential did not match, it will cause an assertion failure.
     *
     * @param credential
     */
    void enablePasswordAuthentication(Map credential) {
        server.passwordAuthenticator = [authenticate: { String username, String password, ServerSession s ->
            assertThat(username, is(credential.username))
            assertThat(password, is(credential.password))
            authenticatedByPassword = true
            true
        }] as PasswordAuthenticator
    }

    /**
     * Enables public key authentication.
     * If credential did not match, it will cause an assertion failure.
     *
     * @param assertion closure(String username, PublicKey key, ServerSession s)
     */
    void enablePublickeyAuthentication(Closure assertion) {
        server.publickeyAuthenticator = [authenticate: { String username, PublicKey key, ServerSession s ->
            assertion(username, key, s)
            authenticatedByPublickey = true
            true
        }] as PublickeyAuthenticator
    }

    /**
     * Enables command execution.
     */
    void enableCommand() {
        server.commandFactory = [createCommand: { String command ->
            requestedCommands.add(command)
            new NullCommand(0)
        }] as CommandFactory
    }

    /**
     * Execute the closure with the SSH server.
     *
     * @param closure
     */
    void execute(Closure closure) {
        server.start()
        logger.info("SSH server has been started at {}:{}", server.host, server.port)
        try {
            closure()
        } finally {
            server.stop()
            logger.info("SSH server has been terminated")
        }
    }

    protected int pickUpFreePort() {
        def socket = new ServerSocket(0)
        def port = socket.localPort
        socket.close()
        port
    }
}
