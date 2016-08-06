package org.hidetake.groovy.ssh.test.server

import com.jcraft.jsch.JSchException
import groovy.util.logging.Slf4j
import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import static org.apache.sshd.common.KeyPairProvider.*
import static org.hidetake.groovy.ssh.test.server.CommandHelper.command
import static org.hidetake.groovy.ssh.test.server.HostKeyFixture.keyPairProvider
import static org.hidetake.groovy.ssh.test.server.HostKeyFixture.publicKeys

@Slf4j
class HostAuthenticationSpec extends Specification {

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


    def "strict host key checking should be turned off by global settings"() {
        given:
        ssh.settings {
            knownHosts = allowAnyHosts
        }

        when:
        executeCommand()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> command(0)
    }

    def "strict host key checking should be turned off by per-remote settings"() {
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
        1 * server.commandFactory.createCommand('somecommand') >> command(0)
    }

    @Unroll
    def "strict host key checking should pass with known-hosts #knownHostsType and server-key #serverKeyType"() {
        given:
        server.keyPairProvider = keyPairProvider(serverKeyType)

        def knownHostsFile = temporaryFolder.newFile()
        publicKeys(knownHostsType).each { publicKey ->
            knownHostsFile << "[$server.host]:$server.port ${publicKey}"
        }
        ssh.settings {
            knownHosts = knownHostsFile
        }

        when:
        executeCommand()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> command(0)

        where:
        serverKeyType                  | knownHostsType
        [SSH_DSS]                      | [SSH_DSS]
        [SSH_RSA]                      | [SSH_RSA]
        [ECDSA_SHA2_NISTP256]          | [ECDSA_SHA2_NISTP256]
        [SSH_RSA]                      | [SSH_RSA, ECDSA_SHA2_NISTP256]
        [SSH_RSA]                      | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [ECDSA_SHA2_NISTP256]          | [SSH_RSA, ECDSA_SHA2_NISTP256]
        [ECDSA_SHA2_NISTP256]          | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [SSH_RSA]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [ECDSA_SHA2_NISTP256]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [SSH_RSA, ECDSA_SHA2_NISTP256]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [SSH_RSA]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [ECDSA_SHA2_NISTP256]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [SSH_RSA, ECDSA_SHA2_NISTP256]
    }

    @Unroll
    def "strict host key checking should pass with hashed known-hosts #knownHostsType and server-key #serverKeyType"() {
        given:
        server.keyPairProvider = keyPairProvider(serverKeyType)

        def knownHostsFile = temporaryFolder.newFile()
        publicKeys(knownHostsType).each { publicKey ->
            knownHostsFile << "${hashHost(server.host, server.port)} ${publicKey}"
        }
        ssh.settings {
            knownHosts = knownHostsFile
        }

        when:
        executeCommand()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> command(0)

        where:
        serverKeyType                  | knownHostsType
        [SSH_DSS]                      | [SSH_DSS]
        [SSH_RSA]                      | [SSH_RSA]
        [ECDSA_SHA2_NISTP256]          | [ECDSA_SHA2_NISTP256]
        [SSH_RSA]                      | [SSH_RSA, ECDSA_SHA2_NISTP256]
        [SSH_RSA]                      | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [ECDSA_SHA2_NISTP256]          | [SSH_RSA, ECDSA_SHA2_NISTP256]
        [ECDSA_SHA2_NISTP256]          | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [SSH_RSA]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [ECDSA_SHA2_NISTP256]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [SSH_RSA, ECDSA_SHA2_NISTP256]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [SSH_RSA]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [ECDSA_SHA2_NISTP256]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [SSH_RSA, ECDSA_SHA2_NISTP256]
    }

    @Unroll
    def "knownHosts can be a list of files where known-hosts #knownHostsType and server-key #serverKeyType"() {
        given:
        server.keyPairProvider = keyPairProvider(serverKeyType)

        def knownHostsFiles = publicKeys(knownHostsType).collect { publicKey ->
            temporaryFolder.newFile() << "[$server.host]:$server.port ${publicKey}"
        }
        ssh.settings {
            knownHosts = knownHostsFiles
        }

        when:
        executeCommand()

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
        1 * server.commandFactory.createCommand('somecommand') >> command(0)

        where:
        serverKeyType                  | knownHostsType
        [SSH_DSS]                      | [SSH_DSS]
        [SSH_RSA]                      | [SSH_RSA]
        [ECDSA_SHA2_NISTP256]          | [ECDSA_SHA2_NISTP256]
        [SSH_RSA]                      | [SSH_RSA, ECDSA_SHA2_NISTP256]
        [SSH_RSA]                      | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [ECDSA_SHA2_NISTP256]          | [SSH_RSA, ECDSA_SHA2_NISTP256]
        [ECDSA_SHA2_NISTP256]          | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [SSH_RSA]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [ECDSA_SHA2_NISTP256]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [SSH_RSA, ECDSA_SHA2_NISTP256]
        [SSH_RSA, ECDSA_SHA2_NISTP256] | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [SSH_RSA]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [ECDSA_SHA2_NISTP256]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [ECDSA_SHA2_NISTP256, SSH_RSA]
        [ECDSA_SHA2_NISTP256, SSH_RSA] | [SSH_RSA, ECDSA_SHA2_NISTP256]
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

    private static hashHost(String host, int port) {
        def hostname = "[$host]:$port"
        def salt = randomBytes(20)
        def hash = hmacSha1(salt, hostname.getBytes())
        "|1|${salt.encodeBase64()}|${hash.encodeBase64()}"
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
