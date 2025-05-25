package org.hidetake.groovy.ssh

import spock.lang.Specification

class SshClassSpec extends Specification {

    def "version property should be the product version"() {
        expect:
        Ssh.release.version.matches(/@version@|SNAPSHOT|[0-9\.]+/)
    }

    def "name property should be the product name"() {
        expect:
        !Ssh.release.name.empty
    }

}
