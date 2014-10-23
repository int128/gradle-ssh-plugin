package org.hidetake.groovy.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.common.Factory
import org.apache.sshd.common.ForwardingFilter
import org.apache.sshd.common.SshdSocketAddress
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.api.Service
import org.hidetake.groovy.ssh.server.SshServerMock.CommandContext
import spock.lang.Specification

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class PortForwardingSpec extends Specification {

    SshServer targetServer
    SshServer gateway1Server
    SshServer gateway2Server

    Service ssh

    def setup() {
        targetServer = setupServer('target')
        gateway1Server = setupServer('gateway1')
        gateway2Server = setupServer('gateway2')

        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
    }

    def setupServer(String name) {
        def server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate("${name}User", "${name}Password", _) >> true
        }
        server.shellFactory = Mock(Factory)
        server.tcpipForwardingFilter = Mock(ForwardingFilter)
        server
    }

    def cleanup() {
        targetServer.stop(true)
        gateway1Server.stop(true)
        gateway2Server.stop(true)
    }


    def "connect via the gateway server"() {
        given:
        gateway1Server.start()
        targetServer.start()

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
            session(ssh.remotes.target) {
                shell(interaction: {})
            }
        }

        then:
        1 * gateway1Server.tcpipForwardingFilter.canConnect(addressOf(targetServer), _) >> true

        then:
        1 * targetServer.shellFactory.create() >> SshServerMock.command { CommandContext c ->
            c.exitCallback.onExit(0)
        }
    }

    def "connect via 2 gateway servers"() {
        given:
        gateway1Server.start()
        gateway2Server.start()
        targetServer.start()

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
            session(ssh.remotes.target) {
                shell(interaction: {})
            }
        }

        then:
        1 * gateway1Server.tcpipForwardingFilter.canConnect(addressOf(gateway2Server), _) >> true

        then:
        1 * gateway2Server.tcpipForwardingFilter.canConnect(addressOf(targetServer), _) >> true

        then:
        1 * targetServer.shellFactory.create() >> SshServerMock.command { CommandContext c ->
            c.exitCallback.onExit(0)
        }
    }


    static addressOf(SshServer server) {
        new SshdSocketAddress(server.host, server.port)
    }

}
