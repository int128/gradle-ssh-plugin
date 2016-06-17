package org.hidetake.groovy.ssh.test.os

import com.jcraft.jsch.agentproxy.AgentProxyException
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.junit.experimental.categories.Category
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemotes
import static org.hidetake.groovy.ssh.test.os.Fixture.randomInt

/**
 * Check if user authentication works with real OS environment.
 *
 * @author Hidetake Iwata
 */
class UserAuthenticationSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        createRemotes(ssh)
    }

    @Category(RequireEcdsaUserKey)
    def 'should authenticate by ECDSA key'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.RequireEcdsaUserKey) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    @Category(RequireKeyWithPassphrase)
    def 'should authenticate by pass-phrased RSA key'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.RequireKeyWithPassphrase) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    def 'should fail if agent is not available'() {
        given:
        ssh.remotes.Default.agent = true

        when:
        ssh.run {
            session(ssh.remotes.Default) {
                execute "id"
            }
        }

        then:
        thrown(AgentProxyException)
    }

    @Category(RequireAgent)
    def 'should authenticate with SSH agent'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.RequireAgent) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    @Category(RequireAgent)
    def 'login to localhost should fail if agent forwarding is disabled'() {
        when:
        ssh.run {
            session(ssh.remotes.RequireAgent) {
                execute "ssh -o StrictHostKeyChecking=no localhost id"
            }
        }

        then:
        thrown(BadExitStatusException)
    }

    @Category(RequireAgent)
    def 'login to localhost should succeed if agent forwarding is enabled'() {
        when:
        def id = ssh.run {
            session(ssh.remotes.RequireAgent) {
                execute "ssh -o StrictHostKeyChecking=no localhost id", agentForwarding: true
            }
        }

        then:
        id.contains(ssh.remotes.RequireAgent.user)
    }

}
