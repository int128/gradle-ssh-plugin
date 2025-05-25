package org.hidetake.groovy.ssh.test.server

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

class DryRunSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
        ssh.remotes {
            testServer {
                host = 'localhost'
                user = 'user'
                dryRun = true
            }
        }
    }


    def "dry-run shell should work without server"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell(interaction: {})
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run shell with options should work without server"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shell(logging: 'none')
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run command should work without server"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute('ls -l')
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run command with callback should work without server"() {
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

    def "dry-run command with options should work without server"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                execute('ls -l', pty: true)
            }
        }

        then:
        noExceptionThrown()
    }

    def "dry-run command with options and callback should work without server"() {
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

}
