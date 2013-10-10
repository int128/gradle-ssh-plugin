package org.hidetake.gradle.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.Environment
import org.apache.sshd.server.PasswordAuthenticator
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.hidetake.gradle.ssh.test.ServerBasedTestHelper
import org.hidetake.gradle.ssh.test.ServerBasedTestHelper.CommandContext
import spock.lang.Specification
import spock.lang.Unroll

class CommandExecutionTest extends Specification {

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

    def commandInteraction(Closure closure) {
        new ServerBasedTestHelper.AbstractCommand() {
            @Override
            void start(Environment env) {
                closure(context)
            }
        }
    }


    def "execute commands sequentially"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute 'somecommand1'
                    execute 'somecommand2'
                    execute 'somecommand3'
                }
            }
        }

        def recorder = new ServerBasedTestHelper.CommandRecorder()
        server.commandFactory = recorder
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        recorder.commands == ['somecommand1', 'somecommand2', 'somecommand3']
    }

    def "handling command failure"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute 'somecommand'
                }
            }
        }

        def recorder = new ServerBasedTestHelper.CommandRecorder(1)
        server.commandFactory = recorder
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        recorder.commands == ['somecommand']
        TaskExecutionException e = thrown()
        e.cause.message.contains('exit status 1')
    }

    @Unroll
    def "obtain a command result, #description"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    project.ext.resultActual = execute 'somecommand'
                }
            }
        }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> commandInteraction { CommandContext c ->
                c.outputStream.withWriter('UTF-8') {
                    it << outputValue
                }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

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
        def loggerMock = Mock(Logger) {
            isEnabled(_) >> true
        }

        project.with {
            ssh {
                outputLogLevel = LogLevel.INFO
                logger = loggerMock
            }
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute 'somecommand'
                }
            }
        }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> commandInteraction { CommandContext c ->
                c.outputStream.withWriter('UTF-8') {
                    it << outputValue
                }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        logMessages.each {
            1 * loggerMock.log(LogLevel.INFO, it)
        }

        where:
        description            | outputValue                  | logMessages
        'a line'               | 'some result'                | ['some result']
        'a line with line sep' | 'some result\n'              | ['some result']
        'lines'                | 'some result\nsecond line'   | ['some result', 'second line']
        'lines with line sep'  | 'some result\nsecond line\n' | ['some result', 'second line']
    }

}
