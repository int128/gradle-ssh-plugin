package org.hidetake.gradle.ssh.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DryRunSpec extends Specification {

    Project project

    def setup() {
        project = ProjectBuilder.builder().build()
        project.with {
            apply plugin: 'org.hidetake.ssh'
            ssh.settings {
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


    def "dry-run shell should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                shell(interaction: {})
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
    }

    def "dry-run shell with options should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                shell(logging: 'none')
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
    }

    def "dry-run command should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                execute('ls -l')
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
    }

    def "dry-run command with callback should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                execute('ls -l') {
                    project.ext.callbackExecuted = true
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
        project.ext.callbackExecuted == true
    }

    def "dry-run command with options should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                execute('ls -l', pty: true)
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
    }

    def "dry-run command with options and callback should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                execute('ls -l', pty: true) {
                    project.ext.callbackExecuted = true
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
        project.ext.callbackExecuted == true
    }

    def "dry-run command in background should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                executeBackground('ls -l')
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
    }

    def "dry-run command in background with callback should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                executeBackground('ls -l') {
                    project.ext.callbackExecuted = true
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
        project.ext.callbackExecuted == true
    }

    def "dry-run command with options in background should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                executeBackground('ls -l', pty: true)
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
    }

    def "dry-run command with options and callback in background should work without server"() {
        given:
        project.tasks.testTask.with {
            session(project.remotes.testServer) {
                executeBackground('ls -l', pty: true) {
                    project.ext.callbackExecuted = true
                }
            }
        }

        when:
        project.tasks.testTask.execute()

        then:
        project.tasks.testTask.didWork
        project.ext.callbackExecuted == true
    }

}
