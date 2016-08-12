package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static org.hidetake.groovy.ssh.test.server.CommandHelper.command

class ExtensionSpec extends Specification {

    @Shared
    SshServer server

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
        ssh.settings {
            knownHosts = allowAnyHosts
        }
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
            }
        }
    }

    def "adding map to ssh.settings.extensions should extends DSL"() {
        given:
        ssh.settings {
            extensions.add restartAppServer: {
                execute 'sudo service tomcat restart'
            }
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                restartAppServer()
            }
        }

        then: 1 * server.commandFactory.createCommand('sudo service tomcat restart') >> command(0)
    }

    def "adding map to settings.extensions should extends DSL"() {
        when:
        ssh.run {
            settings {
                extensions.add restartAppServer: {
                    execute 'sudo service tomcat restart'
                }
            }
            session(ssh.remotes.testServer) {
                restartAppServer()
            }
        }

        then: 1 * server.commandFactory.createCommand('sudo service tomcat restart') >> command(0)
    }

}
