package org.hidetake.gradle.ssh.internal.task

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.internal.operation.Handler
import org.hidetake.gradle.ssh.plugin.SshTask
import org.hidetake.gradle.ssh.internal.operation.OperationProxy
import spock.lang.Specification

class DryRunSpec extends Specification {

    Project project
    Handler handler

    def setup() {
        project = ProjectBuilder.builder().build()
        project.with {
            apply plugin: 'ssh'
            ssh {
                knownHosts = allowAnyHosts
                dryRun = true
            }
            remotes {
                testServer {
                    host = 'localhost'
                    user = 'user'
                }
            }
            task(type: SshTask, 'testTask') {
            }
        }

        handler = Mock(Handler)
        DryRun.instance.handler = handler
    }


    def "invoke a shell"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                shell {
                    interaction {
                    }
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.shell(ShellSettings.DEFAULT, _)
    }

    def "execute a command"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                execute('ls -l')
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.execute(ExecutionSettings.DEFAULT, 'ls -l', OperationProxy.NULL_CLOSURE)
    }

    def "execute a command with options"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                execute('ls -l', pty: true)
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.execute(new ExecutionSettings(pty: true), 'ls -l', OperationProxy.NULL_CLOSURE)
    }

    def "execute a command with an interaction closure"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                execute('ls -l') {
                    interaction {
                    }
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.execute(ExecutionSettings.DEFAULT, 'ls -l', _)
    }

    def "execute a command with options and an interaction closure"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                execute('ls -l', pty: true) {
                    interaction {
                    }
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.execute(new ExecutionSettings(pty: true), 'ls -l', _)
    }

    def "execute a command in background"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                executeBackground('ls -l')
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.executeBackground(ExecutionSettings.DEFAULT, 'ls -l')
    }

    def "execute a command with options in background"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                executeBackground('ls -l', pty: true)
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.executeBackground(new ExecutionSettings(pty: true), 'ls -l')
    }

}
