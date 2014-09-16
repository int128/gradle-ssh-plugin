package org.hidetake.gradle.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.codehaus.groovy.tools.Utilities
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.plugin.SshTask
import org.hidetake.gradle.ssh.test.SshServerMock
import org.hidetake.gradle.ssh.test.SshServerMock.CommandContext
import org.hidetake.groovy.ssh.api.session.BadExitStatusException
import org.hidetake.groovy.ssh.internal.operation.DefaultOperations
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class CommandExecutionSpec extends Specification {

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

        def recorder = Mock(Closure)
        server.commandFactory = Mock(CommandFactory) {
            createCommand(_) >> { String commandline ->
                SshServerMock.command { CommandContext c ->
                    recorder(commandline)
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

    def "handling command failure"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute 'somecommand'
                }
            }
        }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { CommandContext c ->
                c.exitCallback.onExit(1)
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        TaskExecutionException e = thrown()

        and:
        BadExitStatusException cause = e.cause as BadExitStatusException
        cause.exitStatus == 1
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
            1 * createCommand('somecommand') >> SshServerMock.command { CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << outputValue }
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
        'lines'                | 'some result\nsecond line'   | "some result${NL}second line"
        'lines with line sep'  | 'some result\nsecond line\n' | "some result${NL}second line"
    }

    def "obtain a command result via callback"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute('somecommand') { result ->
                        project.ext.resultActual = result
                    }
                }
            }
        }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'something output' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        project.ext.resultActual == 'something output'
    }

    def "obtain a command result via callback with settings"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute('somecommand', pty: true) { result ->
                        project.ext.resultActual = result
                    }
                }
            }
        }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'something output' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        project.ext.resultActual == 'something output'
    }

    @Unroll
    @ConfineMetaClassChanges(DefaultOperations)
    def "logging, #description"() {
        given:
        def logger = Mock(Logger) {
            isInfoEnabled() >> true
        }
        DefaultOperations.metaClass.static.getLog = { -> logger }

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute 'somecommand'
                }
            }
        }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << outputValue }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

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

        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute('somecommand', logging: logging)
                }
            }
        }

        server.commandFactory = Mock(CommandFactory) {
            1 * createCommand('somecommand') >> SshServerMock.command { CommandContext c ->
                c.outputStream.withWriter('UTF-8') { it << 'some message' }
                c.exitCallback.onExit(0)
            }
        }
        server.start()

        when:
        project.tasks.testTask.execute()

        then:
        (logging ? 1 : 0) * logger.info('some message')

        where:
        logging << [true, false]
    }

}
