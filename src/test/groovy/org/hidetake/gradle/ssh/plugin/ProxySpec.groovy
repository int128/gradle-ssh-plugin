package org.hidetake.gradle.ssh.plugin

import spock.lang.Specification

import static ProxyType.SOCKS

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
        result.contains('theProxy')
        result.contains('theHost')
        result.contains('theUser')
        !result.contains('thePassword')
        result.contains(SOCKS.toString())
    }

}
