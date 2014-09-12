package org.hidetake.gradle.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.codehaus.groovy.tools.Utilities
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.internal.operation.DefaultOperations
import org.hidetake.gradle.ssh.plugin.SshTask
import org.hidetake.gradle.ssh.test.SshServerMock
import org.hidetake.gradle.ssh.test.SshServerMock.CommandContext
import spock.lang.Specification
import spock.lang.Unroll

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class SudoCommandExecutionSpec extends Specification {

    private static final NL = Utilities.eol()

    SshServer server
    Project project

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'somepassword', _) >> true
        }

        project = ProjectBuilder.builder().build()
        project.with {
            apply plugin: 'org.hidetake.ssh'
            ssh {
                knownHosts = allowAnyHosts
            }
            remotes {
                testServer {
                    host = server.host
                    port = server.port
                    user = 'someuser'
                    password = 'somepassword'
                }
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
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    executeSudo 'somecommand1'
                    executeSudo 'somecommand2'
                    executeSudo 'somecommand3'
                }
            }
        }

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
        project.tasks.testTask.execute()

        then: 1 * recorder.call('somecommand1')
        then: 1 * recorder.call('somecommand2')
        then: 1 * recorder.call('somecommand3')
    }

    def "handling authentication failure"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    executeSudo 'somecommand'
                }
            }
        }

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
        project.tasks.testTask.execute()

        then:
        1 * recorder.call('somecommand')

        then:
        TaskExecutionException e = thrown()
        e.cause.message.contains('exit status -1')
    }

    def "handling command failure"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    executeSudo 'somecommand'
                }
            }
        }

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
        project.tasks.testTask.execute()

        then:
        1 * recorder.call('somecommand')

        then:
        TaskExecutionException e = thrown()
        e.cause.message.contains('exit status 1')
    }

    @Unroll
    def "obtain a command result, #description"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    project.ext.resultActual = executeSudo 'somecommand'
                }
            }
        }

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
        project.tasks.testTask.execute()

        then:
        1 * recorder.call('somecommand')

        then:
        project.ext.resultActual == resultExpected

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
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    executeSudo('somecommand') { result ->
                        project.ext.resultActual = result
                    }
                }
            }
        }

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

        when:
        project.tasks.testTask.execute()

        then:
        1 * recorder.call('somecommand')

        then:
        project.ext.resultActual == 'something output'
    }

    def "obtain a command result via callback with settings"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    executeSudo('somecommand', pty: true) { result ->
                        project.ext.resultActual = result
                    }
                }
            }
        }

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

        when:
        project.tasks.testTask.execute()

        then:
        1 * recorder.call('somecommand')

        then:
        project.ext.resultActual == 'something output'
    }

    @Unroll
    def "logging, #description"() {
        given:
        def logger = GroovySpy(Logging.getLogger(DefaultOperations).class, global: true) {
            isEnabled(LogLevel.INFO) >> true
        }

        project.with {
            ssh {
                outputLogLevel = LogLevel.INFO
            }
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    executeSudo 'somecommand'
                }
            }
        }

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
        project.tasks.testTask.execute()

        then:
        1 * recorder.call('somecommand')

        then:
        logMessages.each {
            1 * logger.log(LogLevel.INFO, it)
        }

        where:
        description            | outputValue                  | logMessages
        'a line'               | 'some result'                | ['some result']
        'a line with line sep' | 'some result\n'              | ['some result']
        'lines'                | 'some result\nsecond line'   | ['some result', 'second line']
        'lines with line sep'  | 'some result\nsecond line\n' | ['some result', 'second line']
    }

}
