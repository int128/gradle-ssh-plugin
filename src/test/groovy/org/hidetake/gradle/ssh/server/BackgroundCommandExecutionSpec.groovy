package org.hidetake.gradle.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.api.session.BackgroundCommandException
import org.hidetake.groovy.ssh.api.session.BadExitStatusException
import org.hidetake.groovy.ssh.internal.operation.DefaultOperations
import org.hidetake.groovy.ssh.server.ServerIntegrationTest
import org.hidetake.groovy.ssh.server.SshServerMock
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.groovy.ssh.Ssh.ssh

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class BackgroundCommandExecutionSpec extends Specification {

    private static final NL = Utilities.eol()

    SshServer server

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'somepassword', _) >> true
        }

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
        ssh.remotes.clear()
        ssh.proxies.clear()
        ssh.settings.reset()
        server.stop(true)
    }


    def "execute commands sequentially"() {
        given:
        def recorder = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                SshServerMock.command { SshServerMock.CommandContext c ->
                    recorder(commandline)
                    c.exitCallback.onExit(0)
                }
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground 'somecommand1'
                executeBackground 'somecommand2'
                executeBackground 'somecommand3'
            }
        }

        then: 1 * recorder.call('somecommand1')
        then: 1 * recorder.call('somecommand2')
        then: 1 * recorder.call('somecommand3')
    }

    def "handling command failure"() {
        given:
        server.commandFactory = Mock(CommandFactory)
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(1)

        then:
        BackgroundCommandException e = thrown()
        e.exceptionsOfBackgroundExecution.size() == 1

        and:
        def e0 = e.exceptionsOfBackgroundExecution[0] as BadExitStatusException
        e0.exitStatus == 1
    }

    @Unroll
    def "all commands should be executed even if error, A=#exitA B=#exitB C=#exitC"() {
        given:
        server.commandFactory = Mock(CommandFactory)
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground 'commandA'
                executeBackground 'commandB'
                executeBackground 'commandC'
            }
        }

        then: 1 * server.commandFactory.createCommand('commandA') >> commandWithExit(exitA)
        then: 1 * server.commandFactory.createCommand('commandB') >> commandWithExit(exitB)
        then: 1 * server.commandFactory.createCommand('commandC') >> commandWithExit(exitC)

        then:
        BackgroundCommandException e = thrown()
        e.exceptionsOfBackgroundExecution.collect { exceptionOfBackgroundExecution ->
            (exceptionOfBackgroundExecution as BadExitStatusException).exitStatus
        } == exitStatusList

        where:
        exitA | exitB | exitC || exitStatusList
        1     | 0     | 0     || [1]
        0     | 2     | 0     || [2]
        0     | 0     | 3     || [3]
        4     | 5     | 0     || [4, 5]
        0     | 5     | 6     || [5, 6]
        4     | 0     | 6     || [4, 6]
        4     | 5     | 6     || [4, 5, 6]
    }

    def "all commands should be executed even if callback occurs error"() {
        given:
        server.commandFactory = Mock(CommandFactory)
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground('commandA')
                executeBackground('commandB') { result -> throw new RuntimeException('hoge') }
                executeBackground('commandC')
            }
        }

        then: 1 * server.commandFactory.createCommand('commandA') >> commandWithExit(0)
        then: 1 * server.commandFactory.createCommand('commandB') >> commandWithExit(0)
        then: 1 * server.commandFactory.createCommand('commandC') >> commandWithExit(0)

        then:
        BackgroundCommandException e = thrown()
        def e0 = e.exceptionsOfBackgroundExecution[0] as RuntimeException
        e0.localizedMessage == 'hoge'
    }

    @Unroll
    def "obtain a command result, #description"() {
        given:
        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << outputValue }
                c.exitCallback.onExit(0)
            }
        }
        server.start()
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground('somecommand') { result ->
                    resultActual = result
                }
            }
        }

        then:
        resultActual == resultExpected

        where:
        description            | outputValue                  | resultExpected
        'empty'                | ''                           | ''
        'a line'               | 'some result'                | 'some result'
        'a line with line sep' | 'some result\n'              | 'some result'
        'lines'                | 'some result\nsecond line'   | "some result${NL}second line"
        'lines with line sep'  | 'some result\nsecond line\n' | "some result${NL}second line"
    }

    @Unroll
    @ConfineMetaClassChanges(DefaultOperations)
    def "logging, #description"() {
        given:
        def logger = Mock(Logger) {
            isInfoEnabled() >> true
        }
        DefaultOperations.metaClass.static.getLog = { -> logger }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << outputValue }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground 'somecommand'
            }
        }

        then:
        logMessages.each {
            1 * logger.info(it)
        }

        where:
        description            | outputValue                  | logMessages
        'a line'               | 'some result'                | ['some result']
        'a line with line sep' | 'some result\n'              | ['some result']
        'lines'                | 'some result\nsecond line'   | ['some result', 'second line']
        'lines with line sep'  | 'some result\nsecond line\n' | ['some result', 'second line']
    }

    @Unroll
    @ConfineMetaClassChanges(DefaultOperations)
    def "toggle logging = #logging"() {
        given:
        def logger = Mock(Logger) {
            isInfoEnabled() >> true
        }
        DefaultOperations.metaClass.static.getLog = { -> logger }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'some message' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground('somecommand', logging: logging)
            }
        }

        then:
        (logging ? 1 : 0) * logger.info('some message')

        where:
        logging << [true, false]
    }

    @ConfineMetaClassChanges(DefaultOperations)
    def "toggle logging and obtain a command result"() {
        given:
        def logger = Mock(Logger) {
            isInfoEnabled() >> true
        }
        DefaultOperations.metaClass.static.getLog = { -> logger }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'some message' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground('somecommand', logging: true) { result ->
                    resultActual = result
                }
            }
        }

        then:
        1 * logger.info('some message')

        then:
        resultActual == 'some message'
    }

    private static commandWithExit(int status) {
        SshServerMock.command { SshServerMock.CommandContext c ->
            c.exitCallback.onExit(status)
        }
    }

}
