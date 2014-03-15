package org.hidetake.gradle.ssh.server

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.api.session.SessionHandlerFactory
import org.hidetake.gradle.ssh.internal.session.SessionDelegate
import org.hidetake.gradle.ssh.plugin.SshTask
import org.hidetake.gradle.ssh.registry.Registry
import org.hidetake.gradle.ssh.test.ConfineRegistryChanges
import spock.lang.Specification

@ConfineRegistryChanges
class DryRunSpec extends Specification {

    Project project
    Operations handler

    def setup() {
        handler = Mock(Operations)
        Registry.instance[SessionHandlerFactory] = Mock(SessionHandlerFactory) {
            1 * create() >> Registry.instance[SessionHandlerFactory].create(handler)
        }

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
        1 * handler.execute(ExecutionSettings.DEFAULT, 'ls -l', SessionDelegate.NULL_CLOSURE)
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
        1 * handler.execute(new ExecutionSettings(pty: true), 'ls -l', SessionDelegate.NULL_CLOSURE)
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
