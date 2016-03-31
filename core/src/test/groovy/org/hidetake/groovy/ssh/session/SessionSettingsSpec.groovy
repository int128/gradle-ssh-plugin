package org.hidetake.groovy.ssh.session

import spock.lang.Specification
import spock.lang.Unroll

class SessionSettingsSpec extends Specification {

    @Unroll
    def "plus should be merge extensions as list"() {
        given:
        def settings1 = new SessionSettings.With(extensions: extensions1)
        def settings2 = new SessionSettings.With(extensions: extensions2)

        when:
        def settings = new SessionSettings.With(settings1, settings2)

        then:
        settings.extensions == expected

        where:
        extensions1 | extensions2   | expected
        []          | []            | []
        ['a']       | []            | ['a']
        []          | ['b']         | ['b']
        ['a']       | ['b']         | ['a', 'b']
    }

}
