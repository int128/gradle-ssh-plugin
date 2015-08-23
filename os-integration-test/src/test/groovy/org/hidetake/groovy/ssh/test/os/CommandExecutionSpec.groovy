package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.helper.Utilities.*

class CommandExecutionSpec extends Specification {

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        ssh = Ssh.newService()
        ssh.remotes {
            localhost {
                role 'testServers'
                host = 'localhost'
                user = userName()
                identity = privateKey()
            }
        }
    }

    def 'should execute the command'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.localhost) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    def 'should execute the command with console logging with slf4j'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.localhost) {
                execute "expr $x + $y", logging: 'slf4j'
            }
        } as int

        then:
        r == (x + y)
    }

    def 'should execute commands sequentially'() {
        given:
        def x = randomInt()
        def y = randomInt()
        def remoteWorkDir = temporaryFolder.newFolder().path

        when:
        def a
        def b
        ssh.run {
            session(ssh.remotes.localhost) {
                execute "expr $x + $y > $remoteWorkDir/A"
                execute "expr $x + `cat $remoteWorkDir/A` > $remoteWorkDir/B"
                a = get from: "$remoteWorkDir/A"
                b = get from: "$remoteWorkDir/B"
            }
        }

        then:
        a as int == (x + y)
        b as int == (x + x + y)
    }

    def 'should execute commands by multi-line string'() {
        given:
        def x = randomInt()
        def y = randomInt()
        def remoteWorkDir = temporaryFolder.newFolder().path

        when:
        def a
        def b
        ssh.run {
            session(ssh.remotes.localhost) {
                execute """
expr $x + $y > $remoteWorkDir/A
expr $x + `cat $remoteWorkDir/A` > $remoteWorkDir/B
"""
                a = get from: "$remoteWorkDir/A"
                b = get from: "$remoteWorkDir/B"
            }
        }

        then:
        a as int == (x + y)
        b as int == (x + x + y)
    }

    def 'should execute commands in each dedicated environment'() {
        when:
        def r = ssh.run {
            session(ssh.remotes.localhost) {
                execute "export testdata=dummy"
                execute 'echo "testdata is $testdata"'
            }
        }

        then:
        r == 'testdata is '
    }

    def 'should execute the command with the PTY allocation in foreground'() {
        when:
        def envWithoutPty = ssh.run {
            session(ssh.remotes.localhost) {
                execute 'env'
            }
        }

        and:
        def envWithPty = ssh.run {
            session(ssh.remotes.localhost) {
                execute 'env', pty: true
            }
        }

        then:
        !envWithoutPty.contains('SSH_TTY=')
        envWithPty.contains('SSH_TTY=')
    }

    def 'should execute the command with the PTY allocation in background'() {
        when:
        def envWithoutPty
        def envWithPty

        ssh.run {
            session(ssh.remotes.localhost) {
                executeBackground('env') { result ->
                    envWithoutPty = result
                }
            }
            session(ssh.remotes.localhost) {
                executeBackground('env', pty: true) { result ->
                    envWithPty = result
                }
            }
        }

        then:
        !envWithoutPty.contains('SSH_TTY=')
        envWithPty.contains('SSH_TTY=')
    }

    def 'should execute commands concurrently'() {
        given:
        def remoteWorkDir = temporaryFolder.newFolder().path

        when:
        ssh.run {
            // task should start sessions concurrently
            session(ssh.remotes.localhost) {
                executeBackground "sleep 2 && echo 2 >> $remoteWorkDir/result"
            }
            session(ssh.remotes.localhost) {
                executeBackground "sleep 3 && echo 3 >> $remoteWorkDir/result"
                executeBackground "sleep 1 && echo 1 >> $remoteWorkDir/result"
                executeBackground "echo 0 >> $remoteWorkDir/result"
            }
        }

        // all commands should be completed at this point
        def result = ssh.run {
            session(ssh.remotes.localhost) {
                get from: "$remoteWorkDir/result"
            }
        }

        then:
        result.readLines() == ['0', '1', '2', '3']
    }

    def 'should throw an exception due to the error exit status'() {
        when:
        ssh.run {
            session(ssh.remotes.localhost) {
                execute 'exit 1'
            }
        }

        then:
        RuntimeException e = thrown()
        e.localizedMessage.contains('status 1')
    }

    def 'should throw an exception due to the error exit status on background'() {
        when:
        ssh.run {
            session(ssh.remotes.localhost) {
                executeBackground 'exit 1'
            }
        }

        then:
        RuntimeException e = thrown()
        e.localizedMessage == 'Error in background command execution'
    }

    def 'should write output of the command to the file'() {
        given:
        def localWorkDir = temporaryFolder.newFolder().path
        def x = randomInt()
        def y = randomInt()

        when:
        def resultFile = new File("$localWorkDir/result")
        resultFile.withOutputStream { stream ->
            ssh.run {
                session(ssh.remotes.localhost) {
                    execute "expr $x + $y", outputStream: stream
                }
            }
        }

        then:
        resultFile.text as int == (x + y)
    }

    def 'should write output of the command to the standard output'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        ssh.run {
            session(ssh.remotes.localhost) {
                execute "expr $x + $y", outputStream: System.out
            }
        }

        then:
        noExceptionThrown()
    }

    def 'should write error of the command to the file'() {
        given:
        def localWorkDir = temporaryFolder.newFolder().path

        when:
        def resultFile = new File("$localWorkDir/result")
        resultFile.withOutputStream { stream ->
            ssh.run {
                session(ssh.remotes.localhost) {
                    execute "cat hoge", ignoreError: true, errorStream: stream
                }
            }
        }

        then:
        resultFile.text.contains('hoge')
    }

}
