package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.*

/**
 * Check if host authentication works with real OS environment.
 *
 * @author Hidetake Iwata
 */
class HostAuthenticationSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        ssh.remotes {
            localhostECDSA {
                host = hostName()
                user = userName()
                identity = privateKeyRSA()
                knownHosts = knownHostsECDSA()
            }
        }
    }

    def 'should check known_hosts that contains ECDSA host key'() {
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

}
