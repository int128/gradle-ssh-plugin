package org.hidetake.groovy.ssh.test.os

import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.*

/**
 * Check if command execution works with real OS commands.
 *
 * @author Hidetake Iwata
 */
class CommandSpec extends Specification {

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        ssh = Ssh.newService()
        createRemotes(ssh)
    }

    def 'should execute the command'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.Default) {
                execute(['expr', x, '+', y]*.toString())
            }
        } as int

        then:
        r == (x + y)
    }

    def 'should escape command line arguments'() {
        when:
        def r = ssh.run {
            session(ssh.remotes.Default) {
                execute(['perl', '-e', /print 'current: ', time, "\n"/])
            }
        }

        then:
        r =~ /current: \d+/
    }

    def 'should execute commands by multi-line string'() {
        given:
        def x = randomInt()
        def y = randomInt()
        def remoteA = remoteTmpPath()
        def remoteB = remoteTmpPath()

        when:
        def a
        def b
        ssh.run {
            session(ssh.remotes.Default) {
                execute """
expr $x + $y > $remoteA
expr $x + `cat $remoteA` > $remoteB
"""
                a = get from: remoteA
                b = get from: remoteB
            }
        }

        then:
        a as int == (x + y)
        b as int == (x + x + y)
    }

    def 'should execute commands in each dedicated environment'() {
        when:
        def r = ssh.run {
            session(ssh.remotes.Default) {
                execute "export testdata=dummy"
                execute 'echo "testdata is $testdata"'
            }
        }

        then:
        r == 'testdata is '
    }

    def 'should execute the command with the PTY allocation in foreground'() {
        when:
        def envWithPty = ssh.run {
            session(ssh.remotes.Default) {
                execute 'env', pty: true
            }
        }

        then:
        envWithPty.contains('SSH_TTY=')
    }

    def 'should execute the command without the PTY allocation in foreground'() {
        when:
        def envWithoutPty = ssh.run {
            session(ssh.remotes.Default) {
                execute 'env'
            }
        }

        then:
        !envWithoutPty.contains('SSH_TTY=')
    }

    def 'should execute the command with the PTY allocation in background'() {
        when:
        def envWithoutPty
        def envWithPty

        ssh.run {
            session(ssh.remotes.Default) {
                executeBackground('env') { result ->
                    envWithoutPty = result
                }
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
        def remoteX = remoteTmpPath()

        when:
        ssh.run {
            // task should start sessions concurrently
            session(ssh.remotes.Default) {
                executeBackground "sleep 3 && echo C >> $remoteX"
            }
            session(ssh.remotes.Default) {
                executeBackground "sleep 5 && echo D >> $remoteX"
                executeBackground "sleep 1 && echo B >> $remoteX"
                executeBackground "sleep 0 && echo A >> $remoteX"
            }
        }

        // all commands should be completed at this point
        def result = ssh.run {
            session(ssh.remotes.Default) {
                get from: remoteX
            }
        }

        then:
        result.readLines() == ['A', 'B', 'C', 'D']
    }

    def 'should throw an exception due to the error exit status'() {
        when:
        ssh.run {
            session(ssh.remotes.Default) {
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
            session(ssh.remotes.Default) {
                executeBackground 'exit 1'
            }
        }

        then:
        RuntimeException e = thrown()
        e.localizedMessage == 'Error in background command execution'
    }

    def 'should write output of the command to the file'() {
        given:
        def resultFile = temporaryFolder.newFile()
        def x = randomInt()
        def y = randomInt()

        when:
        resultFile.withOutputStream { stream ->
            ssh.run {
                session(ssh.remotes.Default) {
                    execute "expr $x + $y", outputStream: stream
                }
            }
        }

        then:
        resultFile.text as int == (x + y)
    }

    def 'should write error of the command to the file'() {
        given:
        def resultFile = temporaryFolder.newFile()

        when:
        resultFile.withOutputStream { stream ->
            ssh.run {
                session(ssh.remotes.Default) {
                    execute "cat hoge", ignoreError: true, errorStream: stream
                }
            }
        }

        then:
        resultFile.text.contains('hoge')
    }

    def 'should execute command via standard input'() {
        when:
        def actual = ssh.run {
            session(ssh.remotes.Default) {
                execute '/bin/sh', inputStream: '''#!/bin/sh
echo 1
echo 2
'''
            }
        }

        then:
        actual == "1${Utilities.eol()}2"
    }

}
