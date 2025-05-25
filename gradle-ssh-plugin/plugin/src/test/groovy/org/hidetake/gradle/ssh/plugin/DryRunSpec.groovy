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
        }
    }


    def "dry-run shell should work without server"() {
        when:
        project.with {
            ssh.run {
                session(remotes.testServer) {
                    shell(interaction: {})
                }
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run shell with options should work without server"() {
        when:
        project.with {
            ssh.run {
                session(project.remotes.testServer) {
                    shell(logging: 'none')
                }
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run command should work without server"() {
        when:
        project.with {
            ssh.run {
                session(project.remotes.testServer) {
                    execute('ls -l')
                }
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run command with callback should work without server"() {
        when:
        project.with {
            ssh.run {
                session(project.remotes.testServer) {
                    execute('ls -l') {
                        project.ext.callbackExecuted = true
                    }
                }
            }
        }

        then:
        project.ext.callbackExecuted == true
    }

    def "dry-run command with options should work without server"() {
        when:
        project.with {
            ssh.run {
                session(project.remotes.testServer) {
                    execute('ls -l', pty: true)
                }
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run command with options and callback should work without server"() {
        when:
        project.with {
            ssh.run {
                session(project.remotes.testServer) {
                    execute('ls -l', pty: true) {
                        project.ext.callbackExecuted = true
                    }
                }
            }
        }

        then:
        project.ext.callbackExecuted == true
    }

    def "dry-run command in background should work without server"() {
        when:
        project.with {
            ssh.run {
                session(project.remotes.testServer) {
                    executeBackground('ls -l')
                }
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run command in background with callback should work without server"() {
        when:
        project.with {
            ssh.run {
                session(project.remotes.testServer) {
                    executeBackground('ls -l') {
                        project.ext.callbackExecuted = true
                    }
                }
            }
        }

        then:
        project.ext.callbackExecuted == true
    }

    def "dry-run command with options in background should work without server"() {
        when:
        project.with {
            ssh.run {
                session(project.remotes.testServer) {
                    executeBackground('ls -l', pty: true)
                }
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run command with options and callback in background should work without server"() {
        when:
        project.with {
            ssh.run {
                session(project.remotes.testServer) {
                    executeBackground('ls -l', pty: true) {
                        project.ext.callbackExecuted = true
                    }
                }
            }
        }

        then:
        project.ext.callbackExecuted == true
    }

}
