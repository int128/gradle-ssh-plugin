package org.hidetake.groovy.ssh.connection

import spock.lang.Specification
import spock.lang.Unroll

class ConnectionSettingsSpec extends Specification {

    @Unroll
    def "merge identity and passphrase"() {
        given:
        def settings1 = new ConnectionSettings.With(identity: identityX, passphrase: passphraseX)
        def settings2 = new ConnectionSettings.With(identity: identityY, passphrase: passphraseY)

        when:
        def settings = new ConnectionSettings.With(settings1, settings2)

        then:
        settings.identity == identityA
        settings.passphrase == passphraseA

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

    @Unroll
    def "merge identity and passphrase of 3 settings should be as follows"() {
        given:
        def settings1 = new ConnectionSettings.With(identity: identityX, passphrase: passphraseX)
        def settings2 = new ConnectionSettings.With(identity: identityY, passphrase: passphraseY)
        def settings3 = new ConnectionSettings.With(identity: identityZ, passphrase: passphraseZ)

        when:
        def settings = new ConnectionSettings.With(settings1, settings2, settings3)

        then:
        settings.identity == identityA
        settings.passphrase == passphraseA

        where:
        identityX | passphraseX | identityY | passphraseY | identityZ | passphraseZ || identityA | passphraseA
        null            | null  | null            | null  | null            | null  || null            | null
        null            | 'pwX' | null            | null  | null            | null  || null            | null
        null            | null  | null            | 'pwY' | null            | null  || null            | null
        null            | 'pwX' | null            | 'pwY' | null            | null  || null            | null
        new File('idX') | null  | null            | null  | null            | null  || new File('idX') | null
        new File('idX') | 'pwX' | null            | null  | null            | null  || new File('idX') | 'pwX'
        new File('idX') | null  | null            | 'pwY' | null            | null  || new File('idX') | null
        new File('idX') | 'pwX' | null            | 'pwY' | null            | null  || new File('idX') | 'pwX'
        null            | null  | new File('idY') | null  | null            | null  || new File('idY') | null
        null            | 'pwX' | new File('idY') | null  | null            | null  || new File('idY') | null
        null            | null  | new File('idY') | 'pwY' | null            | null  || new File('idY') | 'pwY'
        null            | 'pwX' | new File('idY') | 'pwY' | null            | null  || new File('idY') | 'pwY'
        new File('idX') | null  | new File('idY') | null  | null            | null  || new File('idY') | null
        new File('idX') | 'pwX' | new File('idY') | null  | null            | null  || new File('idY') | null
        new File('idX') | null  | new File('idY') | 'pwY' | null            | null  || new File('idY') | 'pwY'
        new File('idX') | 'pwX' | new File('idY') | 'pwY' | null            | null  || new File('idY') | 'pwY'

        null            | null  | null            | null  | null            | 'pwZ' || null            | null
        null            | 'pwX' | null            | null  | null            | 'pwZ' || null            | null
        null            | null  | null            | 'pwY' | null            | 'pwZ' || null            | null
        null            | 'pwX' | null            | 'pwY' | null            | 'pwZ' || null            | null
        new File('idX') | null  | null            | null  | null            | 'pwZ' || new File('idX') | null
        new File('idX') | 'pwX' | null            | null  | null            | 'pwZ' || new File('idX') | 'pwX'
        new File('idX') | null  | null            | 'pwY' | null            | 'pwZ' || new File('idX') | null
        new File('idX') | 'pwX' | null            | 'pwY' | null            | 'pwZ' || new File('idX') | 'pwX'
        null            | null  | new File('idY') | null  | null            | 'pwZ' || new File('idY') | null
        null            | 'pwX' | new File('idY') | null  | null            | 'pwZ' || new File('idY') | null
        null            | null  | new File('idY') | 'pwY' | null            | 'pwZ' || new File('idY') | 'pwY'
        null            | 'pwX' | new File('idY') | 'pwY' | null            | 'pwZ' || new File('idY') | 'pwY'
        new File('idX') | null  | new File('idY') | null  | null            | 'pwZ' || new File('idY') | null
        new File('idX') | 'pwX' | new File('idY') | null  | null            | 'pwZ' || new File('idY') | null
        new File('idX') | null  | new File('idY') | 'pwY' | null            | 'pwZ' || new File('idY') | 'pwY'
        new File('idX') | 'pwX' | new File('idY') | 'pwY' | null            | 'pwZ' || new File('idY') | 'pwY'

        null            | null  | null            | null  | new File('idZ') | null  || new File('idZ') | null
        null            | 'pwX' | null            | null  | new File('idZ') | null  || new File('idZ') | null
        null            | null  | null            | 'pwY' | new File('idZ') | null  || new File('idZ') | null
        null            | 'pwX' | null            | 'pwY' | new File('idZ') | null  || new File('idZ') | null
        new File('idX') | null  | null            | null  | new File('idZ') | null  || new File('idZ') | null
        new File('idX') | 'pwX' | null            | null  | new File('idZ') | null  || new File('idZ') | null
        new File('idX') | null  | null            | 'pwY' | new File('idZ') | null  || new File('idZ') | null
        new File('idX') | 'pwX' | null            | 'pwY' | new File('idZ') | null  || new File('idZ') | null
        null            | null  | new File('idY') | null  | new File('idZ') | null  || new File('idZ') | null
        null            | 'pwX' | new File('idY') | null  | new File('idZ') | null  || new File('idZ') | null
        null            | null  | new File('idY') | 'pwY' | new File('idZ') | null  || new File('idZ') | null
        null            | 'pwX' | new File('idY') | 'pwY' | new File('idZ') | null  || new File('idZ') | null
        new File('idX') | null  | new File('idY') | null  | new File('idZ') | null  || new File('idZ') | null
        new File('idX') | 'pwX' | new File('idY') | null  | new File('idZ') | null  || new File('idZ') | null
        new File('idX') | null  | new File('idY') | 'pwY' | new File('idZ') | null  || new File('idZ') | null
        new File('idX') | 'pwX' | new File('idY') | 'pwY' | new File('idZ') | null  || new File('idZ') | null

        null            | null  | null            | null  | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        null            | 'pwX' | null            | null  | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        null            | null  | null            | 'pwY' | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        null            | 'pwX' | null            | 'pwY' | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        new File('idX') | null  | null            | null  | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        new File('idX') | 'pwX' | null            | null  | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        new File('idX') | null  | null            | 'pwY' | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        new File('idX') | 'pwX' | null            | 'pwY' | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        null            | null  | new File('idY') | null  | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        null            | 'pwX' | new File('idY') | null  | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        null            | null  | new File('idY') | 'pwY' | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        null            | 'pwX' | new File('idY') | 'pwY' | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        new File('idX') | null  | new File('idY') | null  | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        new File('idX') | 'pwX' | new File('idY') | null  | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        new File('idX') | null  | new File('idY') | 'pwY' | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
        new File('idX') | 'pwX' | new File('idY') | 'pwY' | new File('idZ') | 'pwZ' || new File('idZ') | 'pwZ'
    }

    def "result of ToString() does not contain any credential"() {
        given:
        def settings = new ConnectionSettings.With(
                user: 'theUser',
                password: 'thePassword',
                identity: 'theIdentity',
                passphrase: 'thePassphrase'
        )

        when:
        def result = settings.toString()

        then:
        result.contains('theUser')
        !result.contains('thePassword')
        !result.contains('theIdentity')
        !result.contains('thePassphrase')
    }

    def "result of ToString() contains identity if it is a File"() {
        given:
        def settings = new ConnectionSettings.With(
                user: 'theUser',
                password: 'thePassword',
                identity: new File('theIdentity'),
                passphrase: 'thePassphrase'
        )

        when:
        def result = settings.toString()

        then:
        result.contains('theUser')
        !result.contains('thePassword')
        result.contains('theIdentity')
        !result.contains('thePassphrase')
    }

}
