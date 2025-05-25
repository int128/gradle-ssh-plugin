package org.hidetake.groovy.ssh.core

import spock.lang.Specification

class RemoteSpec extends Specification {

    def "remote should be instantiated with settings"() {
        given:
        def remote = new Remote(name: 'testServer', user: 'admin', host: '1.2.3.4')

        expect:
        remote.name == 'testServer'
        remote.user == 'admin'
        remote.host == '1.2.3.4'
    }

    def "name should be generated if name of settings is not given"() {
        given:
        def remote = new Remote(user: 'admin', host: '1.2.3.4')

        expect:
        remote.name =~ /^Remote\d+$/
        remote.user == 'admin'
        remote.host == '1.2.3.4'
    }

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
