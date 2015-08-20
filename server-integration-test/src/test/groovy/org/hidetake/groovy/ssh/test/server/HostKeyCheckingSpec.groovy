package org.hidetake.groovy.ssh.test.server

import com.jcraft.jsch.JSchException
import groovy.util.logging.Slf4j
import org.apache.sshd.SshServer
import org.apache.sshd.common.KeyPairProvider
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.test.HostKeyFixture
import org.hidetake.groovy.ssh.test.SshServerMock
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import static org.hidetake.groovy.ssh.test.HostKeyFixture.KeyType.*
import static org.hidetake.groovy.ssh.test.SshServerMock.commandWithExit

@org.junit.experimental.categories.Category(ServerIntegrationTest)
@Slf4j
class HostKeyCheckingSpec extends Specification {

    SshServer server

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.commandFactory = Mock(CommandFactory)
        server.start()

        ssh = Ssh.newService()
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
            }
        }
    }

    def cleanup() {
        server.stop(true)
    }

    def executeCommand() {
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand'
            }
        }
    }


    def "strict host key checking should be turned off if knownHosts is allowAnyHosts"() {
        given:
        ssh.settings {
            knownHosts = allowAnyHosts
        }

        when:
        executeCommand()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0)
    }

    def "strict host key checking should be turned off by remote specific settings"() {
        given:
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
                knownHosts = allowAnyHosts
            }
        }

        when:
        executeCommand()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0)
    }

    @Unroll
    def "strict host key checking should pass with a valid known-hosts of #keyType host key"() {
        given:
        server.keyPairProvider = HostKeyFixture.keyPairProvider(keyType)
        assert server.keyPairProvider.keyTypes == keyTypeSshd

        def hostKey = HostKeyFixture.publicKey(keyType)
        def knownHostsFile = temporaryFolder.newFile() << "[localhost]:${server.port} ${hostKey}"
        ssh.settings {
            knownHosts = knownHostsFile
        }

        when:
        executeCommand()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0)

        where:
        keyType | keyTypeSshd
        dsa     | KeyPairProvider.SSH_DSS
        rsa     | KeyPairProvider.SSH_RSA
        ecdsa   | KeyPairProvider.ECDSA_SHA2_NISTP256
    }

    @Unroll
    def "strict host key checking should accept a hashed known-hosts of #keyType host key"() {
        given:
        server.keyPairProvider = HostKeyFixture.keyPairProvider(keyType)
        assert server.keyPairProvider.keyTypes == keyTypeSshd

        def hostname = "[localhost]:${server.port}"
        def salt = randomBytes(20)
        def hash = hmacSha1(salt, hostname.getBytes())

        def hostKey = HostKeyFixture.publicKey(keyType)
        def knownHostsItem = "|1|${salt.encodeBase64()}|${hash.encodeBase64()} ${hostKey}"
        def knownHostsFile = temporaryFolder.newFile() << knownHostsItem
        log.debug(knownHostsItem)

        ssh.settings {
            knownHosts = knownHostsFile
        }

        when:
        executeCommand()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0)

        where:
        keyType | keyTypeSshd
        dsa     | KeyPairProvider.SSH_DSS
        rsa     | KeyPairProvider.SSH_RSA
        ecdsa   | KeyPairProvider.ECDSA_SHA2_NISTP256
    }

    def "strict host key checking should fail if an empty known-hosts is given"() {
        given:
        def knownHostsFile = temporaryFolder.newFile()

        ssh.settings {
            knownHosts = knownHostsFile
        }

        when:
        executeCommand()

        then:
        0 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _)
        0 * server.commandFactory.createCommand('somecommand')

        then:
        JSchException e = thrown()
        e.message.contains 'reject HostKey'
    }

    private static randomBytes(int size) {
        def bytes = new byte[size]
        new Random().nextBytes(bytes)
        bytes
    }

    private static hmacSha1(byte[] salt, byte[] data) {
        def key = new SecretKeySpec(salt, 'HmacSHA1')
        def mac = Mac.getInstance(key.algorithm)
        mac.init(key)
        mac.doFinal(data)
    }

}
