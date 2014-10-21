package org.hidetake.groovy.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.api.session.BadExitStatusException
import org.hidetake.groovy.ssh.internal.operation.DefaultOperations
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.groovy.ssh.Ssh.ssh

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class CommandExecutionSpec extends Specification {

    private static final NL = Utilities.eol()

    SshServer server

    @Rule
    TemporaryFolder temporaryFolder

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
                execute 'somecommand1'
                execute 'somecommand2'
                execute 'somecommand3'
            }
        }

        then: 1 * recorder.call('somecommand1')
        then: 1 * recorder.call('somecommand2')
        then: 1 * recorder.call('somecommand3')
    }

    def "handling command failure"() {
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

    def "obtain a command result via callback"() {
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

    def "obtain a command result via callback with settings"() {
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
                execute 'somecommand'
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
                execute('somecommand', logging: logging)
            }
        }

        then:
        (logging ? 1 : 0) * logger.info('some message')

        where:
        logging << [true, false]
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
