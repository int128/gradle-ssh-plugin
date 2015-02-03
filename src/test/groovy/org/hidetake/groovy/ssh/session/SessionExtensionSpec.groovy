package org.hidetake.groovy.ssh.session

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.operation.Operations
import spock.lang.Specification

class SessionExtensionSpec extends Specification {

    static trait ExampleExtension implements SessionExtension {
        int performSomething(int x, int y) {
            x + y
        }
    }

    def "methods in the trait should be available in the session"() {
        given:
        def operations = Mock(Operations)
        def operationSettings = OperationSettings.DEFAULT + new OperationSettings(extensions: [ExampleExtension])
        def defaultSessionHandler = SessionHandler.create(operations, operationSettings)

        when:
        def result = defaultSessionHandler.with {
            performSomething(100, 200)
        }

        then:
        result == 300
    }

}
