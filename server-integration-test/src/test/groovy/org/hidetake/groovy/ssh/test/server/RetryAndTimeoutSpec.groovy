package org.hidetake.groovy.ssh.test.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.server.SshServerMock.command

class RetryAndTimeoutSpec extends Specification {

    SshServer server

    Service ssh

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.commandFactory = Mock(CommandFactory) {
            createCommand('somecommand1') >> command(0)
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


    def 'should fail if retryWaitSec is too short'() {
        given:
        ssh.settings {
            retryWaitSec = 1
            retryCount = 1
        }

        and: 'start SSH server after 2 second'
        Thread.start {
            Thread.sleep(2000L)
            server.start()
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand1'
            }
        }

        then:
        0 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _)

        then: 'should cause connection refused'
        JSchException e = thrown()
        e.cause instanceof ConnectException
    }

    def 'should retry and success'() {
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
        (1.._) * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> true
    }

    def 'should reach socket read timeout set by timeoutSec'() {
        given:
        server.start()
        ssh.settings {
            timeoutSec = 1
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand1'
            }
        }

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> {
            Thread.sleep(2000L)
            true
        }

        then: 'should cause auth fail'
        thrown(JSchException)
    }

    def 'should not reach socket read timeout set by timeoutSec'() {
        given:
        server.start()
        ssh.settings {
            timeoutSec = 2
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand1'
            }
        }

        then:
        1 * server.passwordAuthenticator.authenticate('someuser', 'somepassword', _) >> {
            Thread.sleep(1000L)
            true
        }
    }

}
