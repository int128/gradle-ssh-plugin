package org.hidetake.groovy.ssh.interaction

import spock.lang.Specification

class InteractionHandlerSpec extends Specification {

    def 'property _ should be the wildcard'() {
        expect:
        InteractionHandler._ instanceof Wildcard
    }

    def 'property standardOutput should be a stream kind'() {
        expect:
        InteractionHandler.standardOutput instanceof Stream
    }

    def 'property standardError should be a stream kind'() {
        expect:
        InteractionHandler.standardError instanceof Stream
    }

    def 'property standardInput should be an output stream given by constructor'() {
        given:
        def standardInputMock = Mock(OutputStream)
        def interactionHandler = new InteractionHandler(standardInputMock)

        expect:
        interactionHandler.standardInput == standardInputMock
    }

}
