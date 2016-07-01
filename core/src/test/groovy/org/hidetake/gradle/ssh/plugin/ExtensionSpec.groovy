package org.hidetake.gradle.ssh.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ExtensionSpec extends Specification {

    Project project

    def setup() {
        project = ProjectBuilder.builder().withName('extensionSpec').build()
        project.with {
            apply plugin: 'org.hidetake.ssh'
            ssh.settings {
                dryRun = true
                extensions << [example: { project.name }]
            }
            remotes {
                testServer {
                    host = 'localhost'
                    user = 'user'
                }
            }
        }
    }

    def "extension should be able to access to the project"() {
        given:
        project.with {
            task('testTask') << {
                project.ext.result = ssh.run {
                    session(remotes.testServer) {
                        example()
                    }
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.result == 'extensionSpec'
    }

}
