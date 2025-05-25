package org.hidetake.groovy.ssh.test.server

import groovy.util.logging.Slf4j
import org.apache.sshd.common.keyprovider.KeyPairProvider
import org.apache.sshd.server.SshServer

import static org.apache.sshd.common.keyprovider.KeyPairProvider.ECDSA_SHA2_NISTP256
import static org.hidetake.groovy.ssh.test.server.HostKeyFixture.keyPairProvider

/**
 * A helper class for server-based integration tests.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class SshServerMock {

    static SshServer setUpLocalhostServer(KeyPairProvider provider = keyPairProvider(ECDSA_SHA2_NISTP256)) {
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
