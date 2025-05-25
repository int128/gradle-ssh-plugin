package org.hidetake.groovy.ssh.session

import org.hidetake.groovy.ssh.core.settings.GlobalSettings
import org.hidetake.groovy.ssh.core.settings.PerServiceSettings
import org.hidetake.groovy.ssh.operation.Operations
import spock.lang.Specification

class SessionHandlerSpec extends Specification {

    def defaultSessionHandler
    Operations operations

    def setup() {
        operations = Mock(Operations)
        defaultSessionHandler = SessionHandler.create(operations, new GlobalSettings(), new PerServiceSettings())
    }

    def "sftp should return value of the closure"() {
        given:
        def closure = Mock(Closure)

        when:
        def result = defaultSessionHandler.with {
            sftp(closure)
        }

        then:
        1 * operations.sftp(_, closure) >> 'something'

        then:
        result == 'something'
    }

}
