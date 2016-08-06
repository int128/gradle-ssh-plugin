package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.server.CommandHelper.command

class ExtensionSpec extends Specification {

    SshServer server

    Service ssh

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.commandFactory = Mock(CommandFactory)
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            (1.._) * authenticate('someuser', 'somepassword', _) >> true
        }
        server.start()

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

    def cleanup() {
        server.stop(true)
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
