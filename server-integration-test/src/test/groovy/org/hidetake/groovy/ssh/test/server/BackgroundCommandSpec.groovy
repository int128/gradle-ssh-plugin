package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.operation.Command
import org.hidetake.groovy.ssh.session.BackgroundCommandException
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static SshServerMock.commandWithExit

class BackgroundCommandSpec extends Specification {

    private static final NL = Utilities.eol()

    SshServer server

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

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


    def "commands should be executed sequentially in ssh.run"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground 'somecommand1'
                executeBackground 'somecommand2'
                executeBackground 'somecommand3'
            }
        }

        then: 1 * server.commandFactory.createCommand('somecommand1') >> commandWithExit(0)
        then: 1 * server.commandFactory.createCommand('somecommand2') >> commandWithExit(0)
        then: 1 * server.commandFactory.createCommand('somecommand3') >> commandWithExit(0)
    }

    def "it should throw an exception if the command exits with non zero status"() {
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

    def "it should ignore the exit status if ignoreError is given"() {
        given:
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground('somecommand', ignoreError: true) { result -> resultActual = result }
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(1, 'something output')

        then:
        resultActual == 'something output'
    }

    @Unroll
    def "all commands should be executed even if error, A=#exitA B=#exitB C=#exitC"() {
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

    def "all commands should be executed even if callback occurs error"() {
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
    def "executeBackground should return output of the command: #description"() {
        given:
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
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0, outputValue)

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
    @ConfineMetaClassChanges(Command)
    def "executeBackground should write output to logger: #description"() {
        given:
        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        Command.metaClass.static.getLog = { -> logger }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0, outputValue)

        then:
        logMessages.each { logMessage ->
            1 * logger.info({ it =~ /testServer#\d+?\|$logMessage/ })
        }

        where:
        description            | outputValue                  | logMessages
        'a line'               | 'some result'                | ['some result']
        'a line with line sep' | 'some result\n'              | ['some result']
        'lines'                | 'some result\nsecond line'   | ['some result', 'second line']
        'lines with line sep'  | 'some result\nsecond line\n' | ['some result', 'second line']
    }

    @Unroll
    @ConfineMetaClassChanges(Command)
    def "executeBackground should write stdout/stderr to #logging"() {
        given:
        def out = System.out
        def err = System.err
        System.out = Mock(PrintStream)
        System.err = Mock(PrintStream)

        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        Command.metaClass.static.getLog = { -> logger }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground 'somecommand', logging: logging
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0, 'some message', 'error')

        then:
        stdout * System.out.println({ it =~ /testServer#\d+?\|some message/ })
        stdout * System.err.println({ it =~ /testServer#\d+?\|error/ })

        slf4j * logger.info ({ it =~ /testServer#\d+?\|some message/ })
        slf4j * logger.error({ it =~ /testServer#\d+?\|error/ })

        cleanup:
        System.out = out
        System.err = err

        where:
        logging        | stdout | slf4j
        LoggingMethod.stdout | 1      | 0
        LoggingMethod.slf4j  | 0      | 1
        LoggingMethod.none   | 0      | 0
    }

    @Unroll
    def "executeBackground should write to file if given: stdout=#stdout, stderr=#stderr"() {
        def logFile = temporaryFolder.newFile()

        when:
        logFile.withOutputStream { stream ->
            ssh.run {
                session(ssh.remotes.testServer) {
                    def map = [:]
                    if (stdout) { map.outputStream = stream }
                    if (stderr) { map.errorStream = stream }
                    executeBackground(map, 'somecommand')
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0, 'some message', 'error')

        then:
        logFile.text == expectedLog

        where:
        stdout | stderr | expectedLog
        false  | false  | ''
        true   | false  | 'some message'
        false  | true   | 'error'
        true   | true   | 'some messageerror'
    }

    def "executeBackground can write stdout/stderr to system.out"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground 'somecommand', outputStream: System.out, errorStream: System.err
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> commandWithExit(0, 'some message', 'error')

        then:
        noExceptionThrown()
    }

    private static commandWithExit(int status) {
        SshServerMock.command { SshServerMock.CommandContext c ->
            c.exitCallback.onExit(status)
        }
    }

}
