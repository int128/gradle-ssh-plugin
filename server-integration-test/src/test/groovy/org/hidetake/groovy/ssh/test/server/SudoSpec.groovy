package org.hidetake.groovy.ssh.test.server

import groovy.util.logging.Slf4j
import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.operation.DefaultOperations
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.test.server.SshServerMock.CommandContext
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@Slf4j
class SudoSpec extends Specification {

    private static final NL = Utilities.eol()

    SshServer server

    Service ssh

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            (1.._) * authenticate('someuser', 'somepassword', _) >> true
        }
        server.commandFactory = Mock(CommandFactory)
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


    static parseSudoCommand(String command) {
        def matcher = command =~ /^sudo -S -p '(.+?)' (.+)$/
        assert matcher.matches()
        def groups = matcher[0] as List
        [prompt: groups[1], command: groups[2]]
    }

    static commandWithSudoPrompt(String actualCommand,
                                 String expectedCommand,
                                 String expectedPassword,
                                 int status,
                                 String outputMessage = null,
                                 String errorMessage = null) {
        SshServerMock.command { CommandContext c ->
            def parsed = parseSudoCommand(actualCommand)
            assert parsed.command == expectedCommand

            log.debug("[sudo] Sending prompt: $parsed.prompt")
            c.outputStream << parsed.prompt
            c.outputStream.flush()

            log.debug("[sudo] Waiting for password: $parsed.prompt")
            def actualPassword = c.inputStream.withReader { it.readLine() }
            assert actualPassword == expectedPassword

            c.outputStream << '\n'
            if (outputMessage) {
                log.debug("[sudo] Sending to standard output: $outputMessage")
                c.outputStream << outputMessage
            }
            if (errorMessage) {
                log.debug("[sudo] Sending to standard error: $errorMessage")
                c.errorStream << errorMessage
            }
            c.exitCallback.onExit(status)
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

        then: 1 * server.commandFactory.createCommand(_) >> { String command -> commandWithSudoPrompt(command, 'somecommand1', 'somepassword', 0) }
        then: 1 * server.commandFactory.createCommand(_) >> { String command -> commandWithSudoPrompt(command, 'somecommand2', 'somepassword', 0) }
        then: 1 * server.commandFactory.createCommand(_) >> { String command -> commandWithSudoPrompt(command, 'somecommand3', 'somepassword', 0) }
    }

    def "it should throw an exception if sudo returns failure"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String command ->
            commandWithSudoPrompt(command, 'somecommand', 'somepassword', 0, 'Sorry, try again.\n')
        }

        then:
        BadExitStatusException e = thrown()
        e.message.contains('exit status -1')
    }

    def "it should throw an exception if the command exits with non zero status"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String command ->
            commandWithSudoPrompt(command, 'somecommand', 'somepassword', 1)
        }

        then:
        BadExitStatusException e = thrown()
        e.message.contains('exit status 1')
    }

    def "it should ignore the exit status if ignoreError is given"() {
        when:
        def resultActual = ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand', ignoreError: true
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String command ->
            commandWithSudoPrompt(command, 'somecommand', 'somepassword', 1, 'something output')
        }

        then:
        resultActual == 'something output'
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
        1 * server.commandFactory.createCommand(_) >> { String command ->
            commandWithSudoPrompt(command, 'somecommand', 'somepassword', 0, outputValue)
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
        1 * server.commandFactory.createCommand(_) >> { String command ->
            commandWithSudoPrompt(command, 'somecommand', 'somepassword', 0, 'something output')
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
        1 * server.commandFactory.createCommand(_) >> { String command ->
            commandWithSudoPrompt(command, 'somecommand', 'somepassword', 0, 'something output')
        }

        then:
        resultActual == 'something output'
    }

    @Unroll
    @ConfineMetaClassChanges(DefaultOperations)
    def "executeSudo should write output to logger: #description"() {
        given:
        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        DefaultOperations.metaClass.static.getLog = { -> logger }

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String command ->
            commandWithSudoPrompt(command, 'somecommand', 'somepassword', 0, outputValue)
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

}
