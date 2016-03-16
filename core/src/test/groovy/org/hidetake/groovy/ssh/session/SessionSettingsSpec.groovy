package org.hidetake.groovy.ssh.session

import spock.lang.Specification

class SessionSettingsSpec extends Specification {

    def "merge with empty"() {
        given:
        def settings = new SessionSettings(dryRun: true)

        when:
        def merged = settings + new SessionSettings()

        then:
        merged == settings
    }

    def "merge 1 key"() {
        given:
        def settings = new SessionSettings(dryRun: true)

        when:
        def merged = settings + new SessionSettings(dryRun: false)

        then:
        !merged.dryRun
        merged.extensions == []
    }

    def "merge 2 keys"() {
        given:
        def settings = new SessionSettings(dryRun: true)

        when:
        def merged = settings + new SessionSettings(dryRun: false, extensions: [SessionSettingsSpec])

        then:
        !merged.dryRun
        merged.extensions == [SessionSettingsSpec]
    }

}
