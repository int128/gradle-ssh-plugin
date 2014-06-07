package org.hidetake.gradle.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.common.Factory
import org.apache.sshd.server.PasswordAuthenticator
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.internal.operation.DefaultOperations
import org.hidetake.gradle.ssh.plugin.SshTask
import org.hidetake.gradle.ssh.plugin.session.BadExitStatusException
import org.hidetake.gradle.ssh.test.SshServerMock
import org.hidetake.gradle.ssh.test.SshServerMock.CommandContext
import spock.lang.Specification
import spock.lang.Unroll

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class ShellExecutionSpec extends Specification {

    SshServer server
    Project project

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            _ * authenticate('someuser', 'somepassword', _) >> true
        }

        project = ProjectBuilder.builder().build()
        project.with {
            apply plugin: 'ssh'
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

    def teardown() {
        server.stop(true)
    }


    def "exit 0"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    shell(interaction: {})
                }
            }
        }

        def factoryMock = Mock(Factory)
        server.shellFactory = factoryMock
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        1 * factoryMock.create() >> SshServerMock.command { CommandContext c ->
            c.exitCallback.onExit(0)
        }
    }

    def "exit 1"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    shell(interaction: {})
                }
            }
        }

        def factoryMock = Mock(Factory)
        server.shellFactory = factoryMock
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        1 * factoryMock.create() >> SshServerMock.command { CommandContext c ->
            c.exitCallback.onExit(1)
        }

        then:
        TaskExecutionException e = thrown()

        and:
        BadExitStatusException cause = e.cause as BadExitStatusException
        cause.exitStatus == 1
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
                    shell(interaction: {})
                }
            }
        }

        server.shellFactory = Mock(Factory) {
            1 * create() >> SshServerMock.command { CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << outputValue }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        logMessages.each { 1 * logger.log(LogLevel.INFO, it) }

        where:
        description            | outputValue                  | logMessages
        'a line'               | 'some result'                | ['some result']
        'a line with line sep' | 'some result\n'              | ['some result']
        'lines'                | 'some result\nsecond line'   | ['some result', 'second line']
        'lines with line sep'  | 'some result\nsecond line\n' | ['some result', 'second line']
    }

    @Unroll
    def "toggle logging = #logging"() {
        given:
        def logger = GroovySpy(Logging.getLogger(DefaultOperations).class, global: true) {
            isEnabled(_) >> true
        }

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    shell(logging: logging)
                }
            }
        }

        server.shellFactory = Mock(Factory) {
            1 * create() >> SshServerMock.command { CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'some message' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        (logging ? 1 : 0) * logger.log(_, 'some message')

        where:
        logging << [true, false]
    }

}
