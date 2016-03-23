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
        globalSettings = CompositeSettings.DEFAULT + new CompositeSettings(dryRun: true)
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
        1 * operations.sftp(closure) >> 'something'

        then:
        result == 'something'
    }

}
