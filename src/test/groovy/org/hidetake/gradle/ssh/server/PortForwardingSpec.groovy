package org.hidetake.gradle.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.common.Factory
import org.apache.sshd.common.ForwardingFilter
import org.apache.sshd.common.SshdSocketAddress
import org.apache.sshd.server.PasswordAuthenticator
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.hidetake.gradle.ssh.test.SshServerMock
import org.hidetake.gradle.ssh.test.SshServerMock.CommandContext
import spock.lang.Specification

class PortForwardingSpec extends Specification {

    SshServer targetServer
    SshdSocketAddress targetAddress
    SshServer gatewayServer
    Project project

    def setup() {
        targetServer = SshServerMock.setUpLocalhostServer()
        targetServer.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('targetUser', 'targetPassword', _) >> true
        }
        targetServer.shellFactory = Mock(Factory)

        targetAddress = new SshdSocketAddress(targetServer.host, targetServer.port)

        gatewayServer = SshServerMock.setUpLocalhostServer()
        gatewayServer.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('gatewayUser', 'gatewayPassword', _) >> true
        }
        gatewayServer.tcpipForwardingFilter = Mock(ForwardingFilter)

        project = ProjectBuilder.builder().build()
        project.with {
            apply plugin: 'ssh'
            ssh {
                knownHosts = allowAnyHosts
            }
            remotes {
                target {
                    host = targetServer.host
                    port = targetServer.port
                    user = 'targetUser'
                    password = 'targetPassword'
                }
                gateway {
                    host = gatewayServer.host
                    port = gatewayServer.port
                    user = 'gatewayUser'
                    password = 'gatewayPassword'
                }
            }
            remotes.target.gateway = remotes.gateway
        }
    }

    def teardown() {
        targetServer.stop(true)
        gatewayServer.stop(true)
    }


    def "local port forwarding"() {
        given:
        gatewayServer.start()
        targetServer.start()

        project.with {
            task(type: SshTask, 'localPortForwardingTask') {
                session(remotes.gateway) {
                    int localPort = forwardLocalPortTo(remotes.target.host, remotes.target.port)

                    remotes.create('targetViaGateway') {
                        host = 'localhost'
                        port = localPort
                        user = 'targetUser'
                        password = 'targetPassword'
                    }

                    sshexec {
                        session(remotes.targetViaGateway) {
                            shell {}
                        }
                    }
                }
            }
        }

        when:
        project.tasks.localPortForwardingTask.execute()

        then:
        1 * gatewayServer.tcpipForwardingFilter.canConnect(targetAddress, _) >> true

        then:
        1 * targetServer.shellFactory.create() >> SshServerMock.command { CommandContext c ->
            c.exitCallback.onExit(0)
        }
    }


    def "connect via the gateway server"() {
        given:
        gatewayServer.start()
        targetServer.start()

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.target) {
                    shell {}
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * gatewayServer.tcpipForwardingFilter.canConnect(targetAddress, _) >> true

        then:
        1 * targetServer.shellFactory.create() >> SshServerMock.command { CommandContext c ->
            c.exitCallback.onExit(0)
        }
    }

}
