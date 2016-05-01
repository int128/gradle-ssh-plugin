package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.experimental.categories.Category
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemote
import static org.hidetake.groovy.ssh.test.os.Fixture.randomInt

/**
 * Check if host authentication works with real OS environment.
 *
 * @author Hidetake Iwata
 */
class HostAuthenticationSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        createRemote(ssh, 'testServer')
    }

    @Category(RequireEcdsaHostKey)
    def 'should check known_hosts that contains ECDSA host key'() {
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

}
