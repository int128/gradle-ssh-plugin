package org.hidetake.gradle.ssh.integration

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.SshTask
import org.hidetake.gradle.ssh.internal.DryRunOperationHandler
import org.hidetake.gradle.ssh.internal.DryRunSshService
import spock.lang.Specification

class SshPluginSpec extends Specification {

    Project project

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
        }
    }

    def "dry run a task"() {
        given:
        project.with {
            task(type: SshTask, 'testTask') {
                session(remotes.testServer) {
                    execute 'ls -l'
                }
            }
        }

        def handler = Spy(DryRunOperationHandler)
        DryRunSshService.instance.handlerFactory = { handler }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.execute([:], 'ls -l')
    }

    def "dry run a task with sshexec"() {
        given:
        project.with {
            task('testTask') << {
                sshexec {
                    session(remotes.testServer) {
                        execute 'ls -l'
                    }
                }
            }
        }

        def handler = Spy(DryRunOperationHandler)
        DryRunSshService.instance.handlerFactory = { handler }

        when:
        project.tasks.testTask.execute()

        then:
        1 * handler.execute([:], 'ls -l')
    }

}
