package org.hidetake.gradle.ssh.server

import groovy.util.logging.Slf4j
import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.Environment
import org.apache.sshd.server.PasswordAuthenticator
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.hidetake.gradle.ssh.internal.DefaultOperationHandler
import org.hidetake.gradle.ssh.test.ServerBasedTestHelper
import org.hidetake.gradle.ssh.test.ServerBasedTestHelper.CommandContext
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
class SudoCommandExecutionTest extends Specification {

    SshServer server
    Project project

    def setup() {
        server = ServerBasedTestHelper.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'somepassword', _) >> true
        }

        project = ProjectBuilder.builder().build()
        project.with {
            apply plugin: 'ssh'
            ssh {
                config(StrictHostKeyChecking: 'no')
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

    def teardown() {
        server.stop(true)
    }

    def sudoInteraction(String commandline, Closure closure) {
        def matcher = commandline =~ /^sudo -S -p '(.+?)' (.+)$/
        assert matcher.matches()
        def groups = matcher[0] as List
        def prompt = groups[1]
        def command = groups[2]

        new ServerBasedTestHelper.AbstractCommand() {
            @Override
            void start(Environment env) {
                log.info("Sending prompt: $prompt")
                context.outputStream << prompt
                context.outputStream.flush()
                closure(command, context)
            }
        }
    }


    def "execute commands"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    executeSudo 'somecommand1'
                    executeSudo 'somecommand2'
                }
            }
        }

        def commandMock = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                sudoInteraction(commandline) { String command, CommandContext c ->
                    commandMock(command)
                    c.outputStream << '\n'
                    c.outputStream.flush()
                    c.exitCallback.onExit(0)
                }
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then: 1 * commandMock.call('somecommand1')
        then: 1 * commandMock.call('somecommand2')
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

        def commandMock = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                sudoInteraction(commandline) { String command, CommandContext c ->
                    commandMock(command)
                    c.outputStream << '\n' << 'Sorry, try again.' << '\n'
                    c.outputStream.flush()
                    c.exitCallback.onExit(1)
                }
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        1 * commandMock.call('somecommand')

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

        def commandMock = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                sudoInteraction(commandline) { String command, CommandContext c ->
                    commandMock(command)
                    c.outputStream << '\n'
                    c.outputStream.flush()
                    c.exitCallback.onExit(1)
                }
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        1 * commandMock.call('somecommand')

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

        def commandMock = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                sudoInteraction(commandline) { String command, CommandContext c ->
                    commandMock(command)
                    c.outputStream << '\n'
                    c.outputStream.flush()
                    c.outputStream.withWriter('UTF-8') {
                        it << outputValue
                    }
                    c.exitCallback.onExit(0)
                }
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        1 * commandMock.call('somecommand')

        then:
        project.ext.resultActual == resultExpected

        where:
        description            | outputValue                  | resultExpected
        'empty'                | ''                           | ''
        'a line'               | 'some result'                | 'some result'
        'a line with line sep' | 'some result\n'              | 'some result'
        'lines'                | 'some result\nsecond line'   | 'some result\nsecond line'
        'lines with line sep'  | 'some result\nsecond line\n' | 'some result\nsecond line'
    }

    @Unroll
    def "logging, #description"() {
        given:
        def logger = GroovySpy(Logging.getLogger(DefaultOperationHandler).class, global: true) {
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

        def commandMock = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                sudoInteraction(commandline) { String command, CommandContext c ->
                    commandMock(command)
                    c.outputStream << '\n'
                    c.outputStream.flush()
                    c.outputStream.withWriter('UTF-8') {
                        it << outputValue
                    }
                    c.exitCallback.onExit(0)
                }
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        1 * commandMock.call('somecommand')

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
