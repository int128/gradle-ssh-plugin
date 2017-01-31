package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.operation.Command
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.groovy.ssh.test.server.CommandHelper.command

class CommandSpec extends Specification {

    private static final NL = Utilities.eol()

    @Shared
    SshServer server

    @Shared @ClassRule
    TemporaryFolder temporaryFolder

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


    def "execute should execute commands sequentially"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand1'
                execute 'somecommand2'
                execute 'somecommand3'
            }
        }

        then: 1 * server.commandFactory.createCommand('somecommand1') >> command(0)
        then: 1 * server.commandFactory.createCommand('somecommand2') >> command(0)
        then: 1 * server.commandFactory.createCommand('somecommand3') >> command(0)
    }

    def "execute should throw an exception if the command exits with non zero status"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(1)

        then:
        BadExitStatusException e = thrown()
        e.exitStatus == 1
    }

    def "execute should ignore the exit status if ignoreError is true"() {
        when:
        def resultActual = ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand', ignoreError: true
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(1) {
            outputStream << 'something output'
        }

        then:
        resultActual == 'something output'
    }

    def "execute should escape arguments if string list is given"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute([/this 'should' be escaped/])
            }
        }

        then: 1 * server.commandFactory.createCommand(/'this '\''should'\'' be escaped'/) >> command(0)
    }

    @Unroll
    def "execute should return output of the command: #description"() {
        when:
        def resultActual = ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0) {
            outputStream << outputValue
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

    def "execute should return value via callback closure"() {
        given:
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute('somecommand') { result ->
                    resultActual = result
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0) {
            outputStream << 'something output'
        }

        then:
        resultActual == 'something output'
    }

    def "execute should escape arguments if string list is given and return value via callback closure"() {
        given:
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute(["echo", /this 'should' be escaped/]) { result ->
                    resultActual = result
                }
            }
        }

        then: 1 * server.commandFactory.createCommand(/'echo' 'this '\''should'\'' be escaped'/) >> command(0) {
            outputStream << 'something output'
        }

        then:
        resultActual == 'something output'
    }

    def "execute should return value via callback with setting"() {
        given:
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute('somecommand', pty: true) { result ->
                    resultActual = result
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0) {
            outputStream << 'something output'
        }

        then:
        resultActual == 'something output'
    }

    def "execute should escape arguments if string list is given return value via callback with setting"() {
        given:
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute(['echo', /this 'should' be escaped/], pty: true) { result ->
                    resultActual = result
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand(/'echo' 'this '\''should'\'' be escaped'/) >> command(0) {
            outputStream << 'something output'
        }

        then:
        resultActual == 'something output'
    }

    @Unroll
    @ConfineMetaClassChanges(Command)
    def "execute should write output to logger: #description"() {
        given:
        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        Command.metaClass.static.getLog = { -> logger }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0) {
            outputStream << outputValue
        }

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
    def "execute should write stdout/stderr to #logging"() {
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
                execute 'somecommand', logging: logging
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0) {
            outputStream << 'some message'
            errorStream << 'error'
        }

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
    def "execute should write to file if given: stdout=#stdout, stderr=#stderr"() {
        given:
        def logFile = temporaryFolder.newFile()

        when:
        logFile.withOutputStream { stream ->
            ssh.run {
                session(ssh.remotes.testServer) {
                    def map = [:]
                    if (stdout) { map.outputStream = stream }
                    if (stderr) { map.errorStream = stream }
                    execute(map, 'somecommand')
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0) {
            outputStream << 'some message'
            errorStream << 'error'
        }

        then:
        logFile.text in expectedLog

        where:
        stdout | stderr | expectedLog
        false  | false  | ['']
        true   | false  | ['some message']
        false  | true   | ['error']
        true   | true   | ['some messageerror', 'errorsome message']
    }

    def "execute can write stdout/stderr to system.out"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand', outputStream: System.out, errorStream: System.err
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0) {
            outputStream << 'some message'
            errorStream << 'error'
        }

        then:
        noExceptionThrown()
    }

    @Unroll
    def "execute should send to standard input if given #input"() {
        given:
        def actual = new ByteArrayOutputStream()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand', inputStream: input
            }
        }

        then:
        1 * server.commandFactory.createCommand('somecommand') >> command(0) {
            actual << inputStream
        }

        then:
        actual.toString() == 'some\nmessage'

        where:
        input << [
            new ByteArrayInputStream('some\nmessage'.bytes),
            'some\nmessage',
            'some\nmessage'.bytes,
            temporaryFolder.newFile() << 'some\nmessage',
        ]
    }

}
