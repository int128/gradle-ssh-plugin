package org.hidetake.gradle.ssh.plugin

import spock.lang.Specification

class RemoteSpec extends Specification {

    def "result of toString() does not contain any credential"() {
        given:
        def remote = new Remote('theRemote')
        remote.user = 'theUser'
        remote.password = 'thePassword'
        remote.identity = new File('theIdentity')
        remote.passphrase = 'thePassphrase'

        when:
        def result = remote.toString()

        then:
        !result.contains('thePassword')
        !result.contains('thePassphrase')
    }

}
