package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.junit.experimental.categories.Category
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.*

/**
 * Check if authentication works with real SSH agent.
 *
 * @author Hidetake Iwata
 */
@Category(RequireAgent)
class UserAuthenticationWithAgentSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        ssh.remotes {
            localhostWithAgent {
                host = hostNameForPrivilegeAccess()
                user = userNameForPrivilegeAccess()
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

    def 'login to localhost should fail if agent forwarding is disabled'() {
        when:
        ssh.run {
            session(ssh.remotes.localhostWithAgent) {
                execute "ssh localhost id"
            }
        }

        then:
        thrown(BadExitStatusException)
    }

    def 'login to localhost should succeed if agent forwarding is enabled'() {
        when:
        def id = ssh.run {
            session(ssh.remotes.localhostWithAgent) {
                execute "ssh localhost id", agentForwarding: true
            }
        }

        then:
        id.contains(ssh.remotes.localhostWithAgent.user)
    }

}
