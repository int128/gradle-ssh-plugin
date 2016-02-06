package org.hidetake.groovy.ssh.test.os.agent

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.helper.Helper.*

/**
 * Check if authentication works with real SSH agent.
 *
 * @author Hidetake Iwata
 */
class AuthenticationSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        ssh.remotes {
            localhostWithAgent {
                host = hostName()
                user = userName()
                agent = true
            }
        }
    }

    def 'should authenticate with SSH agent'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.localhostWithAgent) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

}
