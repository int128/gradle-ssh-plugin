package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Timeout

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemotes

@Timeout(10)
class ShellSpec extends Specification {

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        ssh = Ssh.newService()
        createRemotes(ssh)
    }

    def 'should execute the shell'() {
        when:
        ssh.run {
            session(ssh.remotes.Default) {
                shell(interaction: {
                    when(partial: ~/.*[$%#]\W*/, from: standardOutput) {
                        standardInput << 'uname -a' << '\n'

                        when(partial: ~/.*[$%#]\W*/, from: standardOutput) {
                            standardInput << 'exit 0' << '\n'
                        }
                        when(line: _, from: standardOutput) {}
                    }
                    when(line: _, from: standardOutput) {}
                })
            }
        }

        then:
        noExceptionThrown()
    }

}
