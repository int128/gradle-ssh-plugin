package org.hidetake.groovy.ssh.test.os

import com.jcraft.jsch.agentproxy.AgentProxyException
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.*

/**
 * Check if user authentication works with real OS environment.
 *
 * @author Hidetake Iwata
 */
class UserAuthenticationSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        ssh.remotes {
            localhostECDSA {
                host = hostName()
                user = userName()
                identity = privateKeyECDSA()
            }
            localhostRSAWithPassphrase {
                host = hostName()
                user = userName()
                identity = privateKeyRSAWithPassphrase()
                passphrase = passphraseOfPrivateKey()
            }
            localhostWithAgent {
                host = hostName()
                user = userName()
                agent = true
            }
        }
    }

    def 'should authenticate by ECDSA key'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.localhostECDSA) {
                execute "expr $x + $y"
            }
        } as int

        then:
        r == (x + y)
    }

    def 'should authenticate by pass-phrased RSA key'() {
        given:
        def x = randomInt()
        def y = randomInt()

        when:
        def r = ssh.run {
            session(ssh.remotes.localhostRSAWithPassphrase) {
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
