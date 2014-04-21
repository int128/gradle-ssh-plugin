package org.hidetake.gradle.ssh.api.ssh

import spock.lang.Specification
import spock.lang.Unroll

class ConnectionSettingsSpec extends Specification {

    def "merge with empty"() {
        given:
        def settings = new ConnectionSettings(identity: new File('id_rsa'))

        when:
        def merged = settings + new ConnectionSettings()

        then:
        merged == settings
    }

    def "merge 1 key"() {
        given:
        def settings = new ConnectionSettings(identity: new File('id_rsa'))

        when:
        def merged = settings + new ConnectionSettings(identity: new File('id_dsa'))

        then:
        merged.identity == new File('id_dsa')
        merged.password == null
        merged.knownHosts == null
    }

    def "merge 2 keys"() {
        given:
        def settings = new ConnectionSettings(identity: new File('id_rsa'))

        when:
        def merged = settings + new ConnectionSettings(identity: new File('id_dsa'), password: 'pw')

        then:
        merged.identity == new File('id_dsa')
        merged.password == 'pw'
        merged.knownHosts == null
    }

    @Unroll
    def "merge identity and passphrase"() {
        given:
        def settings = new ConnectionSettings(identity: identityX, passphrase: passphraseX)

        when:
        def merged = settings + new ConnectionSettings(identity: identityY, passphrase: passphraseY)

        then:
        merged.identity == identityA
        merged.passphrase == passphraseA

        where:
        identityX | passphraseX | identityY | passphraseY || identityA | passphraseA
        null            | null  | null            | null  || null            | null
        null            | 'pwX' | null            | null  || null            | null
        null            | null  | null            | 'pwY' || null            | null
        null            | 'pwX' | null            | 'pwY' || null            | null
        new File('idX') | null  | null            | null  || new File('idX') | null
        new File('idX') | 'pwX' | null            | null  || new File('idX') | 'pwX'
        new File('idX') | null  | null            | 'pwY' || new File('idX') | null
        new File('idX') | 'pwX' | null            | 'pwY' || new File('idX') | 'pwX'
        null            | null  | new File('idY') | null  || new File('idY') | null
        null            | 'pwX' | new File('idY') | null  || new File('idY') | null
        null            | null  | new File('idY') | 'pwY' || new File('idY') | 'pwY'
        null            | 'pwX' | new File('idY') | 'pwY' || new File('idY') | 'pwY'
        new File('idX') | null  | new File('idY') | null  || new File('idY') | null
        new File('idX') | 'pwX' | new File('idY') | null  || new File('idY') | null
        new File('idX') | null  | new File('idY') | 'pwY' || new File('idY') | 'pwY'
        new File('idX') | 'pwX' | new File('idY') | 'pwY' || new File('idY') | 'pwY'
    }

}
