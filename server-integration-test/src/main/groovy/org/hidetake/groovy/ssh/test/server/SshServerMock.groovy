package org.hidetake.groovy.ssh.test.server

import groovy.util.logging.Slf4j
import org.apache.sshd.SshServer

/**
 * A helper class for server-based integration tests.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class SshServerMock {

    static SshServer setUpLocalhostServer(provider = HostKeyFixture.keyPairProvider()) {
        SshServer.setUpDefaultServer().with {
            host = 'localhost'
            port = pickUpFreePort()
            keyPairProvider = provider
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
