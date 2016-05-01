package org.hidetake.groovy.ssh.test.os

import com.jcraft.jsch.agentproxy.AgentProxyException
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.junit.experimental.categories.Category
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemote
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
        createRemote(ssh, 'testServer')
    }

    @Category(RequireEcdsaUserKey)
    def 'should authenticate by ECDSA key'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.testServer) {
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
            session(ssh.remotes.testServer) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    def 'should fail if agent is not available'() {
        given:
        ssh.remotes.testServer.agent = true

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
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
            session(ssh.remotes.testServer) {
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
            session(ssh.remotes.testServer) {
                execute "ssh localhost id"
            }
        }

        then:
        thrown(BadExitStatusException)
    }

    @Category(RequireAgent)
    def 'login to localhost should succeed if agent forwarding is enabled'() {
        when:
        def id = ssh.run {
            session(ssh.remotes.testServer) {
                execute "ssh localhost id", agentForwarding: true
            }
        }

        then:
        id.contains(ssh.remotes.testServer.user)
    }

}
