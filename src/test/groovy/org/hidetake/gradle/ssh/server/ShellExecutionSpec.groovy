package org.hidetake.gradle.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.common.Factory
import org.apache.sshd.server.PasswordAuthenticator
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.hidetake.gradle.ssh.test.SshServerMock
import org.hidetake.gradle.ssh.test.SshServerMock.CommandContext
import spock.lang.Specification

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


    def "exit 0"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    shell {}
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
                    shell {}
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
        e.cause.message.contains('exit status 1')
    }

}
