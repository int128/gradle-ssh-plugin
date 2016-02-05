package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.helper.Helper.hostName
import static org.hidetake.groovy.ssh.test.helper.Helper.passphraseOfPrivateKey
import static org.hidetake.groovy.ssh.test.helper.Helper.privateKeyWithPassphrase
import static org.hidetake.groovy.ssh.test.helper.Helper.randomInt
import static org.hidetake.groovy.ssh.test.helper.Helper.userName

/**
 * Check if authentication works with real OS environment.
 *
 * @author Hidetake Iwata
 */
class AuthenticationSpec extends Specification {

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        ssh = Ssh.newService()
        ssh.remotes {
            localhostWithPassphrase {
                host = hostName()
                user = userName()
                identity = privateKeyWithPassphrase()
                passphrase = passphraseOfPrivateKey()
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

}
