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

    def 'rules should be empty at first'() {
        given:
        def interactionHandler = new InteractionHandler(Mock(OutputStream))

        expect:
        interactionHandler.when == []
    }

    def 'when() should add an interaction rule'() {
        given:
        def interactionHandler = new InteractionHandler(Mock(OutputStream))

        when:
        interactionHandler.when(line: 'value') {}

        then:
        interactionHandler.when.size() == 1
        interactionHandler.when[0].condition == [line: 'value']
    }

    def 'when() should add interaction rules'() {
        given:
        def interactionHandler = new InteractionHandler(Mock(OutputStream))

        when:
        interactionHandler.when(line: 'value1') {}
        interactionHandler.when(partial: 'value3') {}

        then:
        interactionHandler.when.size() == 2
        interactionHandler.when[0].condition == [line: 'value1']
        interactionHandler.when[1].condition == [partial: 'value3']
    }

}
