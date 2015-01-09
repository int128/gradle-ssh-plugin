package org.hidetake.groovy.ssh

import spock.lang.Specification

class SshClassSpec extends Specification {

    def "version property should be the product version"() {
        when:
        def version = Ssh.version

        then:
        version.matches(/groovy-ssh-(@version@|SNAPSHOT|[0-9\.]+)/)
    }

}
