package org.hidetake.groovy.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.operation.DefaultOperations
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class CommandExecutionSpec extends Specification {

    private static final NL = Utilities.eol()

    SshServer server

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
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


    def "commands should be executed sequentially in ssh.run"() {
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
                execute 'somecommand1'
                execute 'somecommand2'
                execute 'somecommand3'
            }
        }

        then: 1 * recorder.call('somecommand1')
        then: 1 * recorder.call('somecommand2')
        then: 1 * recorder.call('somecommand3')
    }

    def "it should throw an exception if the command exits with non zero status"() {
        given:
        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.exitCallback.onExit(1)
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand'
            }
        }

        then:
        BadExitStatusException e = thrown()
        e.exitStatus == 1
    }

    def "it should ignore the exit status if ignoreError is given"() {
        given:
        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'something output' }
                c.exitCallback.onExit(1)
            }
        }
        server.start()

        when:
        def resultActual = ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand', ignoreError: true
            }
        }

        then:
        resultActual == 'something output'
    }

    @Unroll
    def "execute should return output of the command: #description"() {
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
                resultActual = execute 'somecommand'
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

    def "execute can return value via callback closure"() {
        given:
        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'something output' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()
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
        resultActual == 'something output'
    }

    def "execute can return value via callback setting"() {
        given:
        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'something output' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()
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
        resultActual == 'something output'
    }

    @Unroll
    @ConfineMetaClassChanges(DefaultOperations)
    def "execute should write output to logger: #description"() {
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
                execute 'somecommand'
            }
        }

        then:
        logMessages.each {
            1 * logger.info("testServer|$it")
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
    def "execute should write stdout/stderr to #logging"() {
        given:
        def out = System.out
        def err = System.err
        System.out = Mock(PrintStream)
        System.err = Mock(PrintStream)

        def logger = Mock(Logger)
        logger.isInfoEnabled() >> true
        DefaultOperations.metaClass.static.getLog = { -> logger }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'some message' }
                c.errorStream.withWriter('UTF-8') { it << 'error' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand', logging: logging
            }
        }

        then:
        stdout * System.out.println('testServer|some message')
        stdout * System.err.println('testServer|error')

        slf4j * logger.info ('testServer|some message')
        slf4j * logger.error('testServer|error')

        cleanup:
        System.out = out
        System.err = err

        where:
        logging                          | stdout | slf4j
        OperationSettings.Logging.stdout | 1      | 0
        OperationSettings.Logging.slf4j  | 0      | 1
        OperationSettings.Logging.none   | 0      | 0
    }

    @Unroll
    def "execute should write to file if given: stdout=#stdout, stderr=#stderr"() {
        given:
        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'some message' }
                c.errorStream.withWriter('UTF-8') { it << 'error' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

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
        logFile.text == expectedLog

        where:
        stdout | stderr | expectedLog
        false  | false  | ''
        true   | false  | 'some message'
        false  | true   | 'error'
        true   | true   | 'some messageerror'
    }

    def "execute can write stdout/stderr to system.out"() {
        given:
        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { SshServerMock.CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'some message' }
                c.errorStream.withWriter('UTF-8') { it << 'error' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute 'somecommand', outputStream: System.out, errorStream: System.err
            }
        }

        then:
        noExceptionThrown()
    }

}
