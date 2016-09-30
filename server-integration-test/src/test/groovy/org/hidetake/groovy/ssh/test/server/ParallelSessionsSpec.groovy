package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.ParallelSessionsException
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.BadExitStatusException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import static org.hidetake.groovy.ssh.test.server.CommandHelper.command

class ParallelSessionsSpec extends Specification {

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


    @Unroll
    def "ssh.run should execute all sessions and throw ParallelSessionsException if exit status is A=#exitA B=#exitB C=#exitC"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'commandA'
            }
            session(ssh.remotes.testServer) {
                execute 'commandB'
            }
            session(ssh.remotes.testServer) {
                execute 'commandC'
            }
        }

        then:
        1 * server.commandFactory.createCommand('commandA') >> command(exitA)
        1 * server.commandFactory.createCommand('commandB') >> command(exitB)
        1 * server.commandFactory.createCommand('commandC') >> command(exitC)

        then:
        ParallelSessionsException e = thrown()
        e.causes.size() > 0
        e.causes.collect { throwable ->
            (throwable as BadExitStatusException).exitStatus
        }.toSet() == exitStatuses.toSet()

        where:
        exitA | exitB | exitC || exitStatuses
        1     | 0     | 0     || [1]
        0     | 2     | 0     || [2]
        0     | 0     | 3     || [3]
        4     | 5     | 0     || [4, 5]
        0     | 5     | 6     || [5, 6]
        4     | 0     | 6     || [4, 6]
        4     | 5     | 6     || [4, 5, 6]
    }

    def "ssh.run should execute all sessions and throw ParallelSessionsException if one caused error"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'commandA'
            }
            session(ssh.remotes.testServer) {
                execute('commandB')
                throw new IllegalStateException('hoge')
            }
            session(ssh.remotes.testServer) {
                execute 'commandC'
            }
        }

        then:
        1 * server.commandFactory.createCommand('commandA') >> command(0)
        1 * server.commandFactory.createCommand('commandB') >> command(0)
        1 * server.commandFactory.createCommand('commandC') >> command(0)

        then:
        ParallelSessionsException e = thrown()
        e.cause instanceof IllegalStateException
        e.causes.size() == 1
        e.causes.head() instanceof IllegalStateException
    }

    @Unroll
    def "ssh.runInOrder should execute sessions and throw the exception if exit status is A=#exitA B=#exitB C=#exitC"() {
        when:
        ssh.runInOrder {
            session(ssh.remotes.testServer) {
                execute 'commandA'
            }
            session(ssh.remotes.testServer) {
                execute 'commandB'
            }
            session(ssh.remotes.testServer) {
                execute 'commandC'
            }
        }

        then: tA * server.commandFactory.createCommand('commandA') >> command(exitA)
        then: tB * server.commandFactory.createCommand('commandB') >> command(exitB)
        then: tC * server.commandFactory.createCommand('commandC') >> command(exitC)

        then:
        BadExitStatusException e = thrown()
        e.exitStatus == expectedExitStatus

        where:
        exitA | exitB | exitC || tA | tB | tC | expectedExitStatus
        1     | 0     | 0     || 1  | 0  | 0  | 1
        0     | 2     | 0     || 1  | 1  | 0  | 2
        0     | 0     | 3     || 1  | 1  | 1  | 3
        4     | 5     | 0     || 1  | 0  | 0  | 4
        0     | 5     | 6     || 1  | 1  | 0  | 5
        4     | 0     | 6     || 1  | 0  | 0  | 4
        4     | 5     | 6     || 1  | 0  | 0  | 4
    }

    def "ssh.runInOrder should execute sessions and throw the first exception"() {
        when:
        ssh.runInOrder {
            session(ssh.remotes.testServer) {
                execute 'commandA'
            }
            session(ssh.remotes.testServer) {
                execute('commandB')
                throw new IllegalStateException('hoge')
            }
            session(ssh.remotes.testServer) {
                execute 'commandC'
            }
        }

        then:
        1 * server.commandFactory.createCommand('commandA') >> command(0)
        1 * server.commandFactory.createCommand('commandB') >> command(0)
        0 * server.commandFactory.createCommand('commandC') >> command(0)

        then:
        IllegalStateException e = thrown()
        e.message == 'hoge'
    }

}
