package org.hidetake.groovy.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.operation.DefaultOperations
import org.hidetake.groovy.ssh.server.SshServerMock.CommandContext
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class SudoCommandExecutionSpec extends Specification {

    private static final NL = Utilities.eol()

    SshServer server

    Service ssh

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'somepassword', _) >> true
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

    def parseSudoCommandLine(String commandline) {
        def matcher = commandline =~ /^sudo -S -p '(.+?)' (.+)$/
        assert matcher.matches()
        def groups = matcher[0] as List
        [prompt: groups[1], commandline: groups[2]]
    }


    def "execute commands sequentially"() {
        given:
        def recorder = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                SshServerMock.command { CommandContext c ->
                    def sudo = parseSudoCommandLine(commandline)
                    recorder(sudo.commandline)
                    c.outputStream.withWriter('UTF-8') { it << sudo.prompt << '\n' }
                    c.exitCallback.onExit(0)
                }
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand1'
                executeSudo 'somecommand2'
                executeSudo 'somecommand3'
            }
        }

        then: 1 * recorder.call('somecommand1')
        then: 1 * recorder.call('somecommand2')
        then: 1 * recorder.call('somecommand3')
    }

    def "handling authentication failure"() {
        given:
        def recorder = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                SshServerMock.command { CommandContext c ->
                    def sudo = parseSudoCommandLine(commandline)
                    recorder(sudo.commandline)
                    c.outputStream.withWriter('UTF-8') {
                        it << sudo.prompt
                        it.flush()
                        it << '\n' << 'Sorry, try again.' << '\n'
                    }
                    c.exitCallback.onExit(1)
                }
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * recorder.call('somecommand')

        then:
        BadExitStatusException e = thrown()
        e.message.contains('exit status -1')
    }

    def "handling command failure"() {
        given:
        def recorder = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                SshServerMock.command { CommandContext c ->
                    def sudo = parseSudoCommandLine(commandline)
                    recorder(sudo.commandline)
                    c.outputStream.withWriter('UTF-8') { it << sudo.prompt << '\n' }
                    c.exitCallback.onExit(1)
                }
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * recorder.call('somecommand')

        then:
        BadExitStatusException e = thrown()
        e.message.contains('exit status 1')
    }

    @Unroll
    def "obtain a command result, #description"() {
        given:
        def recorder = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                SshServerMock.command { CommandContext c ->
                    def sudo = parseSudoCommandLine(commandline)
                    recorder(sudo.commandline)
                    c.outputStream.withWriter('UTF-8') {
                        it << sudo.prompt
                        it.flush()
                        it << '\n' << outputValue
                    }
                    c.exitCallback.onExit(0)
                }
            }
        }
        server.start()

        def resultActual

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                resultActual = executeSudo 'somecommand'
            }
        }

        then:
        1 * recorder.call('somecommand')

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
        def recorder = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                SshServerMock.command { CommandContext c ->
                    def sudo = parseSudoCommandLine(commandline)
                    recorder(sudo.commandline)
                    c.outputStream.withWriter('UTF-8') {
                        it << sudo.prompt
                        it.flush()
                        it << '\n' << 'something output'
                    }
                    c.exitCallback.onExit(0)
                }
            }
        }
        server.start()

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
        1 * recorder.call('somecommand')

        then:
        resultActual == 'something output'
    }

    def "obtain a command result via callback with settings"() {
        given:
        def recorder = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                SshServerMock.command { CommandContext c ->
                    def sudo = parseSudoCommandLine(commandline)
                    recorder(sudo.commandline)
                    c.outputStream.withWriter('UTF-8') {
                        it << sudo.prompt
                        it.flush()
                        it << '\n' << 'something output'
                    }
                    c.exitCallback.onExit(0)
                }
            }
        }
        server.start()

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
        1 * recorder.call('somecommand')

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

        def recorder = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                SshServerMock.command { CommandContext c ->
                    def sudo = parseSudoCommandLine(commandline)
                    recorder(sudo.commandline)
                    c.outputStream.withWriter('UTF-8') {
                        it << sudo.prompt
                        it.flush()
                        it << '\n' << outputValue
                    }
                    c.exitCallback.onExit(0)
                }
            }
        }
        server.start()

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeSudo 'somecommand'
            }
        }

        then:
        1 * recorder.call('somecommand')

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

}
