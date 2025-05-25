package org.hidetake.groovy.ssh.test.server

import groovy.util.logging.Slf4j
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.command.CommandFactory
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.interaction.InteractionException
import org.hidetake.groovy.ssh.operation.Command
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.session.execution.SudoException
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.groovy.ssh.test.server.SudoHelper.sudoCommand

@Slf4j
@Timeout(10)
class SudoSpec extends Specification {

    private static final NL = Utilities.eol()

    @Shared
    SshServer server

    @Shared @ClassRule
    TemporaryFolder temporaryFolder

    Service ssh

    def setupSpec() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            (1.._) * authenticate('someuser', 'somepassword', _) >> true
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
            testServerWithSudoPassword {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
                sudoPassword = 'passwordForTestServerSudo'
            }
            testServerWithSudoPath {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
                sudoPath = '/usr/local/bin/sudo'
            }
        }
    }


    def "commands should be executed sequentially in ssh.run"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand1'
                executeSudo 'somecommand2'
                executeSudo 'somecommand3'
            }
        }

        then: 1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand1', 'somepassword')
        }
        then: 1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand2', 'somepassword')
        }
        then: 1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand3', 'somepassword')
        }
    }

    def "executeSudo should throw an exception if sudo error"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 1, 'sudo', 'somecommand', 'somepassword', null) {
                outputStream << 'Sorry, try again.' << '\n'
                outputStream.flush()
                log.debug("[sudo] Sending prompt again: $parsedCommandLine.prompt")
                outputStream << parsedCommandLine.prompt
                outputStream.flush()
            }
        }

        then:
        InteractionException e = thrown()
        e.exceptions.any { it instanceof SudoException }
    }

    def "executeSudo should throw an exception if the command exits with non zero status"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 1, 'sudo', 'somecommand', 'somepassword')
        }

        then:
        BadExitStatusException e = thrown()
        e.message.contains('exit status 1')
    }

    def "executeSudo should ignore the exit status if ignoreError is true"() {
        when:
        def resultActual = ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand', ignoreError: true
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 1, 'sudo', 'somecommand', 'somepassword', null) {
                outputStream << 'something output'
            }
        }

        then:
        resultActual == 'something output'
    }

    def "executeSudo should escape arguments if string list is given"() {
        when:
        def resultActual = ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo([/this 'should' be escaped/])
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', /'this '\''should'\'' be escaped'/, 'somepassword', null) {
                outputStream << 'something output'
            }
        }

        then:
        resultActual == 'something output'
    }

    def "executeSudo should accept sudo password by method settings"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand1', sudoPassword: 'anotherpassword'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand1', 'anotherpassword')
        }
    }

    def "executeSudo should accept sudo password by remote settings"() {
        when:
        ssh.run {
            session(ssh.remotes.testServerWithSudoPassword) {
                executeSudo 'somecommand1'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand1', 'passwordForTestServerSudo')
        }
    }

    def "executeSudo should accept sudo path by method settings"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand1', sudoPath: '/usr/local/bin/sudo'
            }
        }

        then: 1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, '/usr/local/bin/sudo', 'somecommand1', 'somepassword')
        }
    }

    def "executeSudo should accept sudo path by remote settings"() {
        when:
        ssh.run {
            session(ssh.remotes.testServerWithSudoPath) {
                executeSudo 'somecommand1'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, '/usr/local/bin/sudo', 'somecommand1', 'somepassword')
        }
    }

    @Unroll
    def "executeSudo should return output of the command: #description"() {
        when:
        def resultActual = ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand', 'somepassword', null) {
                outputStream << outputValue
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
    def "executeSudo should exclude lecture message from result: #lectureMessage"() {
        when:
        def resultActual = ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand', 'somepassword', lectureMessage) {
                outputStream << 'something\noutput'
            }
        }

        then:
        resultActual == "something${NL}output"

        where:
        lectureMessage << ['lecture message', 'lecture\nmessage', 'lecture\nmessage\n']
    }

    def "executeSudo can return value via callback closure"() {
        given:
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo('somecommand') { result ->
                    resultActual = result
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand', 'somepassword', null) {
                outputStream << 'something output'
            }
        }

        then:
        resultActual == 'something output'
    }

    def "executeSudo should escape arguments if string list is given and return value via callback closure"() {
        given:
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo(["echo", /this 'should' be escaped/]) { result ->
                    resultActual = result
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', /'echo' 'this '\''should'\'' be escaped'/, 'somepassword', null) {
                outputStream << 'something output'
            }
        }

        then:
        resultActual == 'something output'
    }

    def "executeSudo can return value via callback setting"() {
        given:
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo('somecommand', pty: true) { result ->
                    resultActual = result
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand', 'somepassword', null) {
                outputStream << 'something output'
            }
        }

        then:
        resultActual == 'something output'
    }

    def "executeSudo should escape arguments if string list is given and return value via callback closure with settings"() {
        given:
        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo(['echo', /this 'should' be escaped/], pty: true) { result ->
                    resultActual = result
                }
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', /'echo' 'this '\''should'\'' be escaped'/, 'somepassword', null) {
                outputStream << 'something output'
            }
        }

        then:
        resultActual == 'something output'
    }

    @Unroll
    @ConfineMetaClassChanges(Command)
    def "executeSudo should write output to logger: #description"() {
        given:
        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        Command.metaClass.static.getLog = { -> logger }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand', 'somepassword', null) {
                outputStream << outputValue
            }
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
    def "executeSudo should send to standard input if given #input"() {
        given:
        def actual = new ByteArrayOutputStream()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand', inputStream: input
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String commandLine ->
            sudoCommand(commandLine, 0, 'sudo', 'somecommand', 'somepassword', null) {
                log.debug("[sudo] Reading output of the command: $commandLine")
                actual << inputStream
            }
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
