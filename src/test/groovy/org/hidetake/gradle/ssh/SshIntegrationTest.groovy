package org.hidetake.gradle.ssh

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.internal.OperationEventLogger
import spock.lang.Specification

class SshIntegrationTest extends Specification {


    def "dry run sshtask"() {
        given:
        def project = createProject()
        def task = project.tasks.sshTask
        task.dryRun = true

        def loggerMock = GroovySpy(OperationEventLogger.logger.class, global: true) {
            isEnabled(_) >> true
        }

        when:
        task.perform()

        then:
        1 * loggerMock.log(_, { it.contains("ls -l") } as String)
    }

    def "dry run task with sshexec"() {
        given:
        def project = createProject()
        def task = project.tasks.sshExecTask
        project.with {
            ssh {
                dryRun = true
            }
        }

        def loggerMock = GroovySpy(OperationEventLogger.logger.class, global: true) {
            isEnabled(_) >> true
        }

        when:
        task.execute()

        then:
        1 * loggerMock.log(_, { it.contains("ls -l") } as String)
    }



    private Project createProject() {
        ProjectBuilder.builder().build().with {
            apply plugin: 'ssh'

            ssh {
                config StrictHostKeyChecking: false
            }

            remotes {
                webServer {
                    host = 'localhost'
                    user = 'webuser'
                }

            }

            task(type: SshTask, 'sshTask') {
                session(remotes.webServer) {
                    execute "ls -l"
                }
            }

            task('sshExecTask') << {
                sshexec {
                    session(remotes.webServer) {
                        execute "ls -l"
                    }
                }
            }

            it
        }

    }
}
