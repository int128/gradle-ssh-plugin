package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.common.Factory
import org.apache.sshd.common.util.net.SshdSocketAddress
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.forward.ForwardingFilter
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static org.apache.sshd.common.keyprovider.KeyPairProvider.*
import static org.hidetake.groovy.ssh.test.server.CommandHelper.command
import static org.hidetake.groovy.ssh.test.server.HostKeyFixture.keyPairProvider
import static org.hidetake.groovy.ssh.test.server.HostKeyFixture.publicKey
import static org.hidetake.groovy.ssh.test.server.SshServerMock.setUpLocalhostServer

class GatewaySpec extends Specification {

    @Shared SshServer targetServer
    @Shared SshServer gateway1Server
    @Shared SshServer gateway2Server

    @Shared @ClassRule
    TemporaryFolder temporaryFolder

    Service ssh

    def setupSpec() {
        targetServer = setUpLocalhostServer(keyPairProvider(ECDSA_SHA2_NISTP256))
        gateway1Server = setUpLocalhostServer(keyPairProvider(ECDSA_SHA2_NISTP256))
        gateway2Server = setUpLocalhostServer(keyPairProvider(ECDSA_SHA2_NISTP256))
        [targetServer, gateway1Server, gateway2Server].each { server ->
            server.passwordAuthenticator = Mock(PasswordAuthenticator)
            server.start()
        }
    }

    def cleanupSpec() {
        new PollingConditions().eventually {
            assert targetServer.activeSessions.empty
            assert gateway1Server.activeSessions.empty
            assert gateway2Server.activeSessions.empty
        }
        [targetServer, gateway1Server, gateway2Server]*.stop(true)
    }

    def setup() {
        ssh = Ssh.newService()
        [targetServer, gateway1Server, gateway2Server].each { server ->
            server.passwordAuthenticator = Mock(PasswordAuthenticator)
            server.shellFactory = Mock(Factory)
            server.forwardingFilter = Mock(ForwardingFilter)
        }
    }


    def "it should connect to target server via gateway server"() {
        given:
        def knownHostsContent = [
            "[$gateway1Server.host]:$gateway1Server.port ${publicKey(ECDSA_SHA2_NISTP256)}",
            "[$targetServer.host]:$targetServer.port ${publicKey(ECDSA_SHA2_NISTP256)}",
        ].join('')
        def knownHostsFile = temporaryFolder.newFile() << knownHostsContent

        ssh.remotes {
            gw {
                host = gateway1Server.host
                port = gateway1Server.port
                user = 'gateway1User'
                password = 'gateway1Password'
            }
            target {
                host = targetServer.host
                port = targetServer.port
                user = 'targetUser'
                password = 'targetPassword'
            }
        }

        when:
        ssh.run {
            settings {
                gateway = ssh.remotes.gw
                knownHosts = knownHostsFile
            }
            session(ssh.remotes.target) {
                shell(interaction: {})
            }
        }

        then: (1.._) * gateway1Server.passwordAuthenticator.authenticate("gateway1User", "gateway1Password", _) >> true
        then: 1 * gateway1Server.forwardingFilter.canConnect(_, addressOf(targetServer), _) >> true
        then: (1.._) * targetServer.passwordAuthenticator.authenticate("targetUser", "targetPassword", _) >> true
        then: 1 * targetServer.shellFactory.create() >> command(0)

        then:
        knownHostsFile.text == knownHostsContent
    }

    def "it should connect to target server via gateway server and add host keys"() {
        given:
        def knownHostsFile = temporaryFolder.newFile()
        def expectedKnownHosts = [
            "[$gateway1Server.host]:$gateway1Server.port ${publicKey(ECDSA_SHA2_NISTP256)}",
            "[$targetServer.host]:$targetServer.port ${publicKey(ECDSA_SHA2_NISTP256)}",
        ].join('')

        ssh.remotes {
            gw {
                host = gateway1Server.host
                port = gateway1Server.port
                user = 'gateway1User'
                password = 'gateway1Password'
            }
            target {
                host = targetServer.host
                port = targetServer.port
                user = 'targetUser'
                password = 'targetPassword'
                gateway = ssh.remotes.gw
            }
        }

        when:
        ssh.run {
            settings {
                knownHosts = addHostKey(knownHostsFile)
            }
            session(ssh.remotes.target) {
                shell(interaction: {})
            }
        }

        then: (1.._) * gateway1Server.passwordAuthenticator.authenticate("gateway1User", "gateway1Password", _) >> true
        then: (1.._) * gateway1Server.forwardingFilter.canConnect(_, addressOf(targetServer), _) >> true
        then: (1.._) * targetServer.passwordAuthenticator.authenticate("targetUser", "targetPassword", _) >> true
        then: 1 * targetServer.shellFactory.create() >> command(0)

        then:
        knownHostsFile.text == expectedKnownHosts
    }

    def "it should connect to target server via 2 gateway servers"() {
        given:
        def knownHostsContent = [
            "[$gateway1Server.host]:$gateway1Server.port ${publicKey(ECDSA_SHA2_NISTP256)}",
            "[$gateway2Server.host]:$gateway2Server.port ${publicKey(ECDSA_SHA2_NISTP256)}",
            "[$targetServer.host]:$targetServer.port ${publicKey(ECDSA_SHA2_NISTP256)}",
        ].join('')
        def knownHostsFile = temporaryFolder.newFile() << knownHostsContent

        ssh.remotes {
            gw01 {
                host = gateway1Server.host
                port = gateway1Server.port
                user = 'gateway1User'
                password = 'gateway1Password'
            }
            gw02 {
                host = gateway2Server.host
                port = gateway2Server.port
                user = 'gateway2User'
                password = 'gateway2Password'
                gateway = ssh.remotes.gw01
            }
            target {
                host = targetServer.host
                port = targetServer.port
                user = 'targetUser'
                password = 'targetPassword'
                gateway = ssh.remotes.gw02
            }
        }

        when:
        ssh.run {
            settings {
                knownHosts = knownHostsFile
            }
            session(ssh.remotes.target) {
                shell(interaction: {})
            }
        }

        then: (1.._) * gateway1Server.passwordAuthenticator.authenticate("gateway1User", "gateway1Password", _) >> true
        then: 1 * gateway1Server.forwardingFilter.canConnect(_, addressOf(gateway2Server), _) >> true
        then: (1.._) * gateway2Server.passwordAuthenticator.authenticate("gateway2User", "gateway2Password", _) >> true
        then: 1 * gateway2Server.forwardingFilter.canConnect(_, addressOf(targetServer), _) >> true
        then: (1.._) * targetServer.passwordAuthenticator.authenticate("targetUser", "targetPassword", _) >> true
        then: 1 * targetServer.shellFactory.create() >> command(0)

        then:
        knownHostsFile.text == knownHostsContent
    }

    def "it should connect to target server via 2 gateway servers and add host keys"() {
        given:
        def knownHostsFile = temporaryFolder.newFile()
        def expectedKnownHosts = [
            "[$gateway1Server.host]:$gateway1Server.port ${publicKey(ECDSA_SHA2_NISTP256)}",
            "[$gateway2Server.host]:$gateway2Server.port ${publicKey(ECDSA_SHA2_NISTP256)}",
            "[$targetServer.host]:$targetServer.port ${publicKey(ECDSA_SHA2_NISTP256)}",
        ].join('')

        ssh.remotes {
            gw01 {
                host = gateway1Server.host
                port = gateway1Server.port
                user = 'gateway1User'
                password = 'gateway1Password'
            }
            gw02 {
                host = gateway2Server.host
                port = gateway2Server.port
                user = 'gateway2User'
                password = 'gateway2Password'
                gateway = ssh.remotes.gw01
            }
            target {
                host = targetServer.host
                port = targetServer.port
                user = 'targetUser'
                password = 'targetPassword'
                gateway = ssh.remotes.gw02
            }
        }

        when:
        ssh.run {
            settings {
                knownHosts = addHostKey(knownHostsFile)
            }
            session(ssh.remotes.target) {
                shell(interaction: {})
            }
        }

        then: (1.._) * gateway1Server.passwordAuthenticator.authenticate("gateway1User", "gateway1Password", _) >> true
        then: (1.._) * gateway1Server.forwardingFilter.canConnect(_, addressOf(gateway2Server), _) >> true
        then: (1.._) * gateway2Server.passwordAuthenticator.authenticate("gateway2User", "gateway2Password", _) >> true
        then: (1.._) * gateway2Server.forwardingFilter.canConnect(_, addressOf(targetServer), _) >> true
        then: (1.._) * targetServer.passwordAuthenticator.authenticate("targetUser", "targetPassword", _) >> true
        then: 1 * targetServer.shellFactory.create() >> command(0)

        then:
        knownHostsFile.text == expectedKnownHosts
    }


    private static addressOf(SshServer server) {
        new SshdSocketAddress(server.host, server.port)
    }

}
