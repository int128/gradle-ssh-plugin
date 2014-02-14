package org.hidetake.gradle.ssh.integration

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.hidetake.gradle.ssh.internal.DryRunOperationHandler
import org.hidetake.gradle.ssh.internal.DryRunSshService
import spock.lang.Specification

class DryRunOperationHandlerSpec extends Specification {

    Project project
    DryRunOperationHandler handler

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

        handler = Spy(DryRunOperationHandler)
        DryRunSshService.instance.handlerFactory = { handler }
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
        1 * handler.shell([:], _)
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
        1 * handler.execute([:], 'ls -l', _)
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
        1 * handler.execute([pty: true], 'ls -l', _)
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
        1 * handler.execute([:], 'ls -l', _)
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
        1 * handler.execute([pty: true], 'ls -l', _)
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
        1 * handler.executeBackground([:], 'ls -l')
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
        1 * handler.executeBackground([pty: true], 'ls -l')
    }

}
