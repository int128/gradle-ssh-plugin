package org.hidetake.gradle.ssh.internal.operation.interaction

import org.hidetake.gradle.ssh.api.operation.interaction.Stream
import org.hidetake.gradle.ssh.api.operation.interaction.Wildcard
import spock.lang.Specification

class InteractionDelegateSpec extends Specification {

    def '_ is the wildcard'() {
        expect:
        InteractionDelegate._ instanceof Wildcard
    }

    def 'standard output constant'() {
        expect:
        InteractionDelegate.standardOutput instanceof Stream
    }

    def 'standard error constant'() {
        expect:
        InteractionDelegate.standardError instanceof Stream
    }

    def 'standard input constant'() {
        given:
        def interactionDelegate = new InteractionDelegate(Mock(OutputStream))

        expect:
        interactionDelegate.standardInput == standardInputMock
    }

    def 'evaluate() returns an empty list'() {
        given:
        def interactionDelegate = new InteractionDelegate(Mock(OutputStream))

        when:
        def interactionRules = interactionDelegate.evaluate {
        }

        then:
        interactionRules == []
    }

    def 'when() adds an interaction rule'() {
        given:
        def interactionDelegate = new InteractionDelegate(Mock(OutputStream))

        def interactionRule = new InteractionRule(key: 'value', {true}, {})
        GroovyMock(InteractionRule, global: true)
        1 * InteractionRule.create(_ as Map, _) >> interactionRule

        when:
        def interactionRules = interactionDelegate.evaluate {
            when(key: 'value') {}
        }

        then:
        interactionRules == [interactionRule]
    }

    def 'multiple when()'() {
        given:
        def interactionDelegate = new InteractionDelegate(Mock(OutputStream))

        GroovyMock(InteractionRule, global: true)
        InteractionRule.create(_ as Map, _) >> { Map c, a -> new InteractionRule(c, null, null) }

        when:
        def interactionRules = interactionDelegate.evaluate {
            when(key1: 'value1') {}
            when(key2: 'value2') {}
            when(key3: 'value3') {}
        }

        then:
        interactionRules == [
                new InteractionRule(key1: 'value1', null, null),
                new InteractionRule(key2: 'value2', null, null),
                new InteractionRule(key3: 'value3', null, null),
        ]
    }

    def 'results of evaluate() are dependent'() {
        given:
        def interactionDelegate = new InteractionDelegate(Mock(OutputStream))

        GroovyMock(InteractionRule, global: true)
        InteractionRule.create(_ as Map, _) >> { Map c, a -> new InteractionRule(c, null, null) }

        when:
        def interactionRules1 = interactionDelegate.evaluate {
            when(key1: 'value1') {}
            when(key2: 'value2') {}
        }
        def interactionRules2 = interactionDelegate.evaluate {
            when(key3: 'value3') {}
        }

        then:
        interactionRules1 == [
                new InteractionRule(key1: 'value1', null, null),
                new InteractionRule(key2: 'value2', null, null),
        ]
        interactionRules2 == [
                new InteractionRule(key3: 'value3', null, null),
        ]
    }

}
