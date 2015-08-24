package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.helper.Helper.*

class ShellSpec extends Specification {

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        ssh = Ssh.newService()
        ssh.remotes {
            localhost {
                host = hostName()
                user = userName()
                identity = privateKey()
            }
        }
    }

    def 'should execute the shell'() {
        when:
        ssh.run {
            session(ssh.remotes.localhost) {
                shell(interaction: {
                    when(partial: ~/.*[$%#] */) {
                        standardInput << 'exit 0' << '\n'
                    }
                })
            }
        }

        then:
        noExceptionThrown()
    }

}
