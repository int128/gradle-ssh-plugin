package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.SshServer
import org.apache.sshd.common.Factory
import org.apache.sshd.common.ForwardingFilter
import org.apache.sshd.common.SshdSocketAddress
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static SshServerMock.commandWithExit
import static org.hidetake.groovy.ssh.test.server.HostKeyFixture.publicKeys

class GatewaySpec extends Specification {

    SshServer targetServer
    SshServer gateway1Server
    SshServer gateway2Server

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        targetServer = setupServer('ssh-dss')
        gateway1Server = setupServer('ssh-rsa')
        gateway2Server = setupServer('ecdsa-sha2-nistp256')
        ssh = Ssh.newService()
    }

    def cleanup() {
        targetServer.stop(true)
        gateway1Server.stop(true)
        gateway2Server.stop(true)
    }


    def "it should connect to target server via gateway server"() {
        given:
        gateway1Server.start()
        targetServer.start()

        def knownHostsFile = temporaryFolder.newFile()
        publicKeys(['ssh-dss']).each { publicKey -> knownHostsFile << "[$targetServer.host]:$targetServer.port $publicKey" }
        publicKeys(['ssh-rsa']).each { publicKey -> knownHostsFile << "[$gateway1Server.host]:$gateway1Server.port $publicKey" }

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
        then: 1 * gateway1Server.tcpipForwardingFilter.canConnect(addressOf(targetServer), _) >> true
        then: (1.._) * targetServer.passwordAuthenticator.authenticate("targetUser", "targetPassword", _) >> true

        then:
        1 * targetServer.shellFactory.create() >> commandWithExit(0)

        then: 'Make sure target and gateway session is closed, too'
        def conditions = new PollingConditions(timeout: 10, initialDelay: 0.1)
        conditions.eventually {
            assert gateway1Server.activeSessions.size() == 0
            assert targetServer.activeSessions.size() == 0
        }
    }

    def "it should connect to target server via 2 gateway servers"() {
        given:
        gateway1Server.start()
        gateway2Server.start()
        targetServer.start()

        def knownHostsFile = temporaryFolder.newFile()
        publicKeys(['ssh-dss']).each { publicKey -> knownHostsFile << "[$targetServer.host]:$targetServer.port $publicKey" }
        publicKeys(['ssh-rsa']).each { publicKey -> knownHostsFile << "[$gateway1Server.host]:$gateway1Server.port $publicKey" }
        publicKeys(['ecdsa-sha2-nistp256']).each { publicKey -> knownHostsFile << "[$gateway2Server.host]:$gateway2Server.port $publicKey" }

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
        then: 1 * gateway1Server.tcpipForwardingFilter.canConnect(addressOf(gateway2Server), _) >> true
        then: (1.._) * gateway2Server.passwordAuthenticator.authenticate("gateway2User", "gateway2Password", _) >> true
        then: 1 * gateway2Server.tcpipForwardingFilter.canConnect(addressOf(targetServer), _) >> true
        then: (1.._) * targetServer.passwordAuthenticator.authenticate("targetUser", "targetPassword", _) >> true

        then:
        1 * targetServer.shellFactory.create() >> commandWithExit(0)
    }


    private setupServer(String keyType) {
        def server = SshServerMock.setUpLocalhostServer(HostKeyFixture.keyPairProvider([keyType]))
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.shellFactory = Mock(Factory)
        server.tcpipForwardingFilter = Mock(ForwardingFilter)
        server
    }

    private static addressOf(SshServer server) {
        new SshdSocketAddress(server.host, server.port)
    }

}
