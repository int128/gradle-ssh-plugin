package org.hidetake.gradle.ssh.server

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.operation.ExecutionSettings
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.operation.ShellSettings
import org.hidetake.gradle.ssh.api.session.SessionHandler
import org.hidetake.gradle.ssh.internal.session.SessionDelegate
import org.hidetake.gradle.ssh.plugin.SshTask
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.gradle.ssh.test.RegistryHelper.factoryOf

@ConfineMetaClassChanges(SessionHandler)
class DryRunSpec extends Specification {

    Project project
    Operations handler

    def setup() {
        handler = Mock(Operations)
        factoryOf(SessionHandler) << Mock(SessionHandler.Factory) {
            1 * create(_) >> new SessionDelegate(handler)
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
                shell()
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.shell(ShellSettings.DEFAULT)
    }

    def "invoke a shell with options"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                shell(logging: false)
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.shell(new ShellSettings(logging: false))
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
        1 * handler.execute(ExecutionSettings.DEFAULT, 'ls -l')
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
        1 * handler.execute(new ExecutionSettings(pty: true), 'ls -l')
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
