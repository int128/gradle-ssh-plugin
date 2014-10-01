package org.hidetake.gradle.ssh.server

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.plugin.SshTask
import spock.lang.Specification

class DryRunSpec extends Specification {

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
            task(type: SshTask, 'testTask') {
            }
        }
    }


    def "invoke a shell"() {
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
        project.tasks.testTask.didWork
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
        project.tasks.testTask.didWork
    }

    def "execute a command with callback"() {
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
        project.tasks.testTask.didWork
    }

    def "execute a command with options and callback"() {
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
        project.tasks.testTask.didWork
    }

    def "execute a command in background with callback"() {
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
        project.tasks.testTask.didWork
    }

    def "execute a command with options and callback in background"() {
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
