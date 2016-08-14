package org.hidetake.groovy.ssh.test.server

import com.jcraft.jsch.JSchException
import org.apache.sshd.SshServer
import org.apache.sshd.common.Factory
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.sftp.SftpSubsystem
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout
import spock.util.concurrent.PollingConditions

import static org.hidetake.groovy.ssh.test.server.CommandHelper.command

@Timeout(10)
class TimeoutSpec extends Specification {

    @Shared
    SshServer server

    Service ssh

    def setupSpec() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
        server.start()
    }

    def cleanupSpec() {
        new PollingConditions().eventually {
            assert server.activeSessions.empty
        }
        server.stop()
    }

    def setup() {
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            authenticate('someuser', 'somepassword', _) >> true
        }
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


    def 'should reach socket read timeout set by timeoutSec'() {
        given:
        server.passwordAuthenticator = Mock(PasswordAuthenticator)
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

        then:
        JSchException e = thrown()
        e.message.contains('Auth fail')
    }

    def 'should reach channel timeout set by timeoutSec on execute()'() {
        given:
        server.commandFactory = Mock(CommandFactory)
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
        1 * server.commandFactory.createCommand('somecommand1') >> {
            Thread.sleep(2000L)
            command(0)
        }

        then:
        JSchException e = thrown()
        e.message == 'channel request: timeout'
    }

    def 'should reach channel timeout set by timeoutSec on executeBackground()'() {
        given:
        server.commandFactory = Mock(CommandFactory)
        ssh.settings {
            timeoutSec = 1
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground 'somecommand1'
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand1') >> {
            Thread.sleep(2000L)
            command(0)
        }

        then:
        JSchException e = thrown()
        e.message == 'channel request: timeout'
    }

    def 'should reach channel timeout set by timeoutSec on shell()'() {
        given:
        server.shellFactory = Mock(Factory)
        ssh.settings {
            timeoutSec = 1
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell(interaction: {})
            }
        }

        then:
        1 * server.shellFactory.create() >> {
            Thread.sleep(2000L)
            command(0)
        }

        then:
        JSchException e = thrown()
        e.message == 'channel request: timeout'
    }

    def 'should reach channel timeout set by timeoutSec on sftp()'() {
        given:
        server.subsystemFactories = [Mock(SftpSubsystem.Factory)]
        ssh.settings {
            timeoutSec = 1
        }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                sftp {}
            }
        }

        then:
        server.subsystemFactories[0].getName() >> 'sftp'
        server.subsystemFactories[0].create() >> {
            Thread.sleep(2000L)
            new SftpSubsystem()
        }

        then:
        JSchException e = thrown()
        e.message == 'channel request: timeout'
    }

    def 'should not reach any timeout set by timeoutSec'() {
        given:
        server.commandFactory = Mock(CommandFactory)
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
        1 * server.commandFactory.createCommand('somecommand1') >> command(0)
    }

}
