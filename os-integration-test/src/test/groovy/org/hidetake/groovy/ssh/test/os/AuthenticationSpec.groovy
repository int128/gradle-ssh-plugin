package org.hidetake.groovy.ssh.test.os

import com.jcraft.jsch.JSchException
import com.jcraft.jsch.agentproxy.AgentProxyException
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.helper.Helper.*

/**
 * Check if authentication works with real OS environment.
 *
 * @author Hidetake Iwata
 */
class AuthenticationSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        ssh.remotes {
            localhostWithPassphrase {
                host = hostName()
                user = userName()
                identity = privateKeyWithPassphrase()
                passphrase = passphraseOfPrivateKey()
            }
            localhostWithAgent {
                host = hostName()
                user = userName()
                agent = true
            }
        }
    }

    def 'should authenticate by pass-phrased private key'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.localhostWithPassphrase) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    def 'should fail if agent is not available'() {
        when:
        ssh.run {
            session(ssh.remotes.localhostWithAgent) {
                execute "id"
            }
        }

        then:
        thrown(AgentProxyException)
    }

}
