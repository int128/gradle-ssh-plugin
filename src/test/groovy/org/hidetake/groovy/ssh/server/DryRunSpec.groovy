package org.hidetake.groovy.ssh.server

import spock.lang.Specification

import static org.hidetake.groovy.ssh.Ssh.ssh

class DryRunSpec extends Specification {

    def setup() {
        ssh.settings {
            knownHosts = allowAnyHosts
            dryRun = true
        }
        ssh.remotes {
            testServer {
                host = 'localhost'
                user = 'user'
            }
        }
    }

    def cleanup() {
        ssh.remotes.clear()
        ssh.proxies.clear()
        ssh.settings.reset()
    }


    def "invoke a shell"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell(interaction: {})
            }
        }

        then:
        noExceptionThrown()
    }

    def "invoke a shell with options"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell(logging: false)
            }
        }

        then:
        noExceptionThrown()
    }

    def "execute a command"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute('ls -l')
            }
        }

        then:
        noExceptionThrown()
    }

    def "execute a command with callback"() {
        given:
        def callbackExecuted = false

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute('ls -l') {
                    callbackExecuted = true
                }
            }
        }

        then:
        callbackExecuted
    }

    def "execute a command with options"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute('ls -l', pty: true)
            }
        }

        then:
        noExceptionThrown()
    }

    def "execute a command with options and callback"() {
        given:
        def callbackExecuted = false

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute('ls -l', pty: true) {
                    callbackExecuted = true
                }
            }
        }

        then:
        callbackExecuted
    }

    def "execute a command in background"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground('ls -l')
            }
        }

        then:
        noExceptionThrown()
    }

    def "execute a command in background with callback"() {
        given:
        def callbackExecuted = false

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground('ls -l') {
                    callbackExecuted = true
                }
            }
        }

        then:
        callbackExecuted
    }

    def "execute a command with options in background"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground('ls -l', pty: true)
            }
        }

        then:
        noExceptionThrown()
    }

    def "execute a command with options and callback in background"() {
        given:
        def callbackExecuted = false

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeBackground('ls -l', pty: true) {
                    callbackExecuted = true
                }
            }
        }

        then:
        callbackExecuted
    }

}
