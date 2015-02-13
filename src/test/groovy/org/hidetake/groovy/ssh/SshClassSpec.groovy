package org.hidetake.groovy.ssh

import spock.lang.Specification

class SshClassSpec extends Specification {

    def "version property should be the product version"() {
        when:
        def version = Ssh.product.version

        then:
        version.matches(/@version@|SNAPSHOT|[0-9\.]+/)
    }

    def "name property should be the product name"() {
        when:
        def version = Ssh.product.version

        then:
        !version.empty
    }

}
