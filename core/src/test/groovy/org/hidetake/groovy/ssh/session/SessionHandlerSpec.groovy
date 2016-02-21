package org.hidetake.groovy.ssh.session

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.operation.Operations
import spock.lang.Specification

class SessionHandlerSpec extends Specification {

    def defaultSessionHandler
    Operations operations
    OperationSettings operationSettings

    def setup() {
        operations = Mock(Operations)
        operationSettings = OperationSettings.DEFAULT + new OperationSettings(dryRun: true)
        defaultSessionHandler = SessionHandler.create(operations, operationSettings)
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
