package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

import static SshServerMock.commandWithExit

class RetryConnectionSpec extends Specification {

    SshServer server

    Service ssh

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.commandFactory = Mock(CommandFactory)
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            (1.._) * authenticate('someuser', 'somepassword', _) >> true
        }

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


    def 'it should retry connection if retryCount > 1'() {
        given:
        ssh.settings {
            retryWaitSec = 2
            retryCount = 1
        }

        and: 'start SSH server after 1 second'
        Thread.start {
            Thread.sleep(1000L)
            server.start()
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand1'
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand1') >> commandWithExit(0)
    }

}
