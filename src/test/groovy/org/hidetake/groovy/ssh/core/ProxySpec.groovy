package org.hidetake.groovy.ssh.core

import org.hidetake.groovy.ssh.core.Proxy
import spock.lang.Specification

import static org.hidetake.groovy.ssh.core.ProxyType.SOCKS

class ProxySpec extends Specification {

    def "result of toString() does not contain password"() {
        given:
        def proxy = new Proxy('theProxy')
        proxy.host = 'theHost'
        proxy.user = 'theUser'
        proxy.password = 'thePassword'
        proxy.type = SOCKS

        when:
        def result = proxy.toString()

        then:
        !result.contains('thePassword')
    }

}
