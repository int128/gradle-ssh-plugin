package org.hidetake.groovy.ssh.test.server

import com.jcraft.jsch.JSchException
import groovy.util.logging.Slf4j
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import static org.apache.sshd.common.keyprovider.KeyPairProvider.*
import static org.hidetake.groovy.ssh.test.server.CommandHelper.command
import static org.hidetake.groovy.ssh.test.server.HostKeyFixture.*

@Slf4j
class HostAuthenticationSpec extends Specification {

    @Shared
    SshServer server

    @Shared @ClassRule
    TemporaryFolder temporaryFolder

    Service ssh

    def setupSpec() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            authenticate('someuser', 'somepassword', _) >> true
        }
        server.start()
    }

    def cleanupSpec() {
        new PollingConditions().eventually {
            assert server.activeSessions.empty
        }
        server.stop()
    }

    def setup() {
        server.commandFactory = Mock(CommandFactory)

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


    def "[allowAnyHosts] host key checking should be turned off by global settings"() {
        given:
        ssh.settings {
            knownHosts = allowAnyHosts
        }

        when:
        executeCommand()

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0)
    }

    def "[allowAnyHosts] host key checking should be turned off by per-remote settings"() {
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
        1 * server.commandFactory.createCommand('somecommand') >> command(0)
    }

    @Unroll
    def "[file] host key checking should pass with known-hosts #knownHostsType and server-key #serverKeyType"() {
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
    def "[file] host key checking should pass with hashed known-hosts #knownHostsType and server-key #serverKeyType"() {
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
    def "[files] host key checking should pass with known-hosts #knownHostsType and server-key #serverKeyType"() {
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

    def "[file] host key checking should fail if an empty known-hosts is given"() {
        given:
        def knownHostsFile = temporaryFolder.newFile()

        ssh.settings {
            knownHosts = knownHostsFile
        }

        when:
        executeCommand()

        then:
        0 * server.commandFactory.createCommand('somecommand')

        then:
        JSchException e = thrown()
        e.message.contains 'reject HostKey'
    }

    def "[file] host key checking should fail if a wrong host key is given"() {
        given:
        server.keyPairProvider = keyPairProvider(ECDSA_SHA2_NISTP256)

        def knownHostsFile = temporaryFolder.newFile()
        knownHostsFile << "[$server.host]:$server.port ${publicKey("${ECDSA_SHA2_NISTP256}_another")}"

        ssh.settings {
            knownHosts = knownHostsFile
        }

        when:
        executeCommand()

        then:
        0 * server.commandFactory.createCommand('somecommand')

        then:
        JSchException e = thrown()
        e.message.contains 'HostKey has been changed'
    }

    def "[addHostKey] knownHosts file should be created if it does not exist"() {
        given:
        server.keyPairProvider = keyPairProvider(SSH_DSS)

        def knownHostsFile = temporaryFolder.newFile()
        knownHostsFile.delete()

        ssh.settings {
            knownHosts = addHostKey(knownHostsFile)
        }

        when:
        executeCommand()

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0)

        and: 'knownHosts should be created'
        knownHostsFile.text == "[$server.host]:$server.port ${publicKey(SSH_DSS)}" as String
    }

    def "[addHostKey] knownHosts file should be appended if it exists"() {
        given:
        server.keyPairProvider = keyPairProvider(ECDSA_SHA2_NISTP256)

        def initialContent = "example.com ${publicKey("${ECDSA_SHA2_NISTP256}_another")}"
        def knownHostsFile = temporaryFolder.newFile() << initialContent

        ssh.settings {
            knownHosts = addHostKey(knownHostsFile)
        }

        when:
        executeCommand()

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0)

        and: 'knownHosts should be appended'
        knownHostsFile.text ==
            (initialContent + "[$server.host]:$server.port ${publicKey(ECDSA_SHA2_NISTP256)}") as String
    }

    def "[addHostKey] knownHosts file should not be modified if host key already exists"() {
        given:
        server.keyPairProvider = keyPairProvider(SSH_DSS)

        def knownHostsFile = temporaryFolder.newFile()
        knownHostsFile << "[$server.host]:$server.port ${publicKey(SSH_DSS)}"

        ssh.settings {
            knownHosts = addHostKey(knownHostsFile)
        }

        when:
        executeCommand()

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0)
        knownHostsFile.text == "[$server.host]:$server.port ${publicKey(SSH_DSS)}" as String
    }

    def "[addHostKey] strict host key checking should fail if a wrong host key is given"() {
        given:
        server.keyPairProvider = keyPairProvider(ECDSA_SHA2_NISTP256)

        def knownHostsFile = temporaryFolder.newFile()
        knownHostsFile << "[$server.host]:$server.port ${publicKey("${ECDSA_SHA2_NISTP256}_another")}"

        ssh.settings {
            knownHosts = addHostKey(knownHostsFile)
        }

        when:
        executeCommand()

        then:
        0 * server.commandFactory.createCommand('somecommand')

        then:
        JSchException e = thrown()
        e.message.contains 'HostKey has been changed'
    }


    private executeCommand() {
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand'
            }
        }
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
