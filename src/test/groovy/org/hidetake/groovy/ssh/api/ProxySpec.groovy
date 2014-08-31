package org.hidetake.groovy.ssh.api

import spock.lang.Specification

import static org.hidetake.groovy.ssh.api.ProxyType.SOCKS

class ProxySpec extends Specification {

    def "result of toString() does not contain password"() {
        given:
        def proxy = new org.hidetake.groovy.ssh.api.Proxy('theProxy')
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
