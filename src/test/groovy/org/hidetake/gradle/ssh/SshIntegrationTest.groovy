package org.hidetake.gradle.ssh

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class SshIntegrationTest extends Specification {


    def "dry run sshtask"() {
        given:
        def project = createProject()
        def task = project.tasks.sshTask
        task.dryRun = true

        def loggerMock = Mock(Logger)
        task.logger = loggerMock
        loggerMock.isEnabled(_) >> true

        when:
        task.perform()

        then:
        1 * loggerMock.log(_, { it.contains("ls -l") })
    }

    def "dry run task with sshexec"() {
        given:
        def project = createProject()
        def task = project.tasks.sshExecTask
        def globalSpec = project.convention.plugins.ssh.sshSpec
        def loggerMock = GroovySpy(globalSpec.logger.class, global: true)
        globalSpec.dryRun = true


        when:
        task.execute()

        then:
        1 * loggerMock.log(_, { it.contains("ls -l") })
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
