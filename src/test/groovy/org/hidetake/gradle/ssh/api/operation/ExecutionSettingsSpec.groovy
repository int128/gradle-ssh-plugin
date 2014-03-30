package org.hidetake.gradle.ssh.api.operation

import spock.lang.Specification

class ExecutionSettingsSpec extends Specification {

    def "merge with empty"() {
        given:
        def settings = new ExecutionSettings(pty: true)

        when:
        def merged = settings + [:]

        then:
        merged == settings
    }

    def "merge 1 key"() {
        given:
        def settings = new ExecutionSettings(pty: true)

        when:
        def merged = settings + [pty: false]

        then:
        !merged.pty
        merged.logging
        merged.interaction == null
    }

    def "merge 2 keys"() {
        given:
        def settings = new ExecutionSettings(pty: true)
        final closure = { x, y -> x + y }

        when:
        def merged = settings + [pty: false, interaction: closure]

        then:
        !merged.pty
        merged.logging
        merged.interaction(-3, 5) == 2
    }

}
