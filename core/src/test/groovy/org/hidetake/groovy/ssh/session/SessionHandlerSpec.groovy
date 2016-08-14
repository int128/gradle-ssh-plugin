package org.hidetake.groovy.ssh.session

import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.operation.Operations
import spock.lang.Specification

class SessionHandlerSpec extends Specification {

    def defaultSessionHandler
    Operations operations
    CompositeSettings globalSettings

    def setup() {
        operations = Mock(Operations)
        globalSettings = new CompositeSettings.With(CompositeSettings.With.DEFAULT, new CompositeSettings.With(dryRun: true))
        defaultSessionHandler = SessionHandler.create(operations, globalSettings)
    }

    def "sftp should return value of the closure"() {
        given:
        def closure = Mock(Closure)

        when:
        def result = defaultSessionHandler.with {
            sftp(closure)
        }

        then:
        1 * operations.sftp(globalSettings, closure) >> 'something'

        then:
        result == 'something'
    }

}
