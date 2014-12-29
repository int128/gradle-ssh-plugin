package org.hidetake.groovy.ssh.core.settings

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import spock.lang.Specification

class OperationSettingsSpec extends Specification {

    def "merge with empty"() {
        given:
        def settings = new OperationSettings(pty: true)

        when:
        def merged = settings + new OperationSettings()

        then:
        merged == settings
    }

    def "merge 1 key"() {
        given:
        def settings = new OperationSettings(pty: true)

        when:
        def merged = settings + new OperationSettings(pty: false)

        then:
        !merged.pty
        merged.logging == null
        merged.interaction == null
    }

    def "merge 2 keys"() {
        given:
        def settings = new OperationSettings(pty: true)
        final closure = { x, y -> x + y }

        when:
        def merged = settings + new OperationSettings(pty: false, interaction: closure)

        then:
        !merged.pty
        merged.logging == null
        merged.interaction(-3, 5) == 2
    }

}
