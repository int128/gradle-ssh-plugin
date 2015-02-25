package org.hidetake.groovy.ssh.session

import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.operation.Operations
import spock.lang.Specification
import spock.lang.Unroll

class SessionExtensionSpec extends Specification {

    static trait ExtensionTrait1 {
        private final something = 'something'
        def performSomething(String command) {
            execute "$command $something"
        }
    }

    static trait ExtensionTrait2 {
        private final another = 'another'
        def performAnother(String command) {
            performSomething "$command $another"
        }
    }

    static class ExtensionClass1 {
        private final something = 'something'
        def performSomething(String command) {
            execute "$command $something"
        }
    }

    static class ExtensionClass2 {
        private final another = 'another'
        def performAnother(String command) {
            performSomething "$command $another"
        }
    }

    static final extensionMap1 = [performSomething: { command -> execute "$command something" }]

    static final extensionMap2 = [performAnother: { command -> performSomething "$command another" }]


    @Unroll
    def "method in the #type should be available in the session"() {
        given:
        def operations = Mock(Operations)
        def operationSettings = OperationSettings.DEFAULT + new OperationSettings(extensions: extensions)
        def defaultSessionHandler = SessionHandler.create(operations, operationSettings)

        when:
        def result = defaultSessionHandler.with {
            performSomething('command')
        }

        then: 1 * operations.execute(operationSettings, 'command something', null) >> 'result'
        then: result == 'result'

        where:
        type    | extensions
        'trait' | [ExtensionTrait1]
        'class' | [ExtensionClass1]
        'map'   | [extensionMap1]
    }

    @Unroll
    def "method in #type should be available in the session"() {
        given:
        def operations = Mock(Operations)
        def operationSettings = OperationSettings.DEFAULT + new OperationSettings(extensions: extensions)
        def defaultSessionHandler = SessionHandler.create(operations, operationSettings)

        when:
        def result = defaultSessionHandler.with {
            performAnother('command')
        }

        then: 1 * operations.execute(operationSettings, 'command another something', null) >> 'result'
        then: result == 'result'

        where:
        type              | extensions
        'traits'          | [ExtensionTrait1, ExtensionTrait2]
        'classes'         | [ExtensionClass1, ExtensionClass2]
        'maps'            | [extensionMap1,   extensionMap2]
        'trait and class' | [ExtensionTrait1, ExtensionClass2]
        'trait and map'   | [ExtensionTrait1, extensionMap2]
        'class and trait' | [ExtensionClass1, ExtensionTrait2]
        'class and map'   | [ExtensionClass1, extensionMap2]

        'maps (reverse ordered)' | [extensionMap2, extensionMap1]

        // These combinations will fail:
        //'map and trait' | [extensionMap1,   ExtensionTrait2]
        //'map and class' | [extensionMap1,   ExtensionClass2]
    }

    @Unroll
    def "method in #type should be invisible in reverse order"() {
        given:
        def operations = Mock(Operations)
        def operationSettings = OperationSettings.DEFAULT + new OperationSettings(extensions: extensions)
        def defaultSessionHandler = SessionHandler.create(operations, operationSettings)

        when:
        defaultSessionHandler.with {
            performAnother('command')
        }

        then:
        MissingMethodException e = thrown()
        e.method == 'performSomething'

        where:
        type              | extensions
        'traits'          | [ExtensionTrait2, ExtensionTrait1]
        'classes'         | [ExtensionClass2, ExtensionClass1]
        'trait and class' | [ExtensionTrait2, ExtensionClass1]
        'class and trait' | [ExtensionClass2, ExtensionTrait1]
    }

    @Unroll
    def "private property in the #type should be invisible in the session"() {
        given:
        def operations = Mock(Operations)
        def operationSettings = OperationSettings.DEFAULT + new OperationSettings(extensions: extensions)
        def defaultSessionHandler = SessionHandler.create(operations, operationSettings)

        when:
        defaultSessionHandler.with {
            something
        }
        then:
        MissingPropertyException e1 = thrown()
        e1.property == 'something'

        when:
        defaultSessionHandler.with {
            another
        }
        then:
        MissingPropertyException e2 = thrown()
        e2.property == 'another'

        where:
        type    | extensions
        'trait' | [ExtensionTrait1]
        'class' | [ExtensionClass1]
    }

}
