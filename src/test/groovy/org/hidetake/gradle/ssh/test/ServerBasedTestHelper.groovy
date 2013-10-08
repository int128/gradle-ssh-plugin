package org.hidetake.gradle.ssh.test

import groovy.util.logging.Slf4j
import org.apache.sshd.SshServer
import org.apache.sshd.common.keyprovider.FileKeyPairProvider

/**
 * A helper class for server-based integration tests.
 *
 * @author hidetake.org
 *
 */
@Slf4j
class ServerBasedTestHelper {
    static SshServer setUpLocalhostServer() {
        SshServer.setUpDefaultServer().with {
            host = 'localhost'
            port = pickUpFreePort()
            keyPairProvider = new FileKeyPairProvider(ServerBasedTestHelper.getResource('/hostkey').file)
            it
        }
    }

    static int pickUpFreePort() {
        def socket = new ServerSocket(0)
        def port = socket.localPort
        socket.close()
        port
    }
}
