package org.hidetake.gradle.ssh.internal.interaction

import org.hidetake.gradle.ssh.internal.interaction.Engine.Counter
import org.hidetake.gradle.ssh.plugin.interaction.Stream
import spock.lang.Specification

class EngineSpec extends Specification {

    def 'received a line, no rule matched'() {
        given:
        def counter = Spy(Counter)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> false }, {})
        def interactionDelegate = Mock(InteractionDelegate)
        def engine = new Engine(interactionDelegate)
        engine.alterInteractionRules([rule1])

        when:
        engine.processLine(Stream.StandardOutput, counter, 'unknownLine')

        then:
        0 * interactionDelegate.evaluate(_)
        0 * counter.reset()
        engine.interactionRules == [rule1]
    }

    def 'received a line, rule matched, action has no rule'() {
        given:
        def counter = Spy(Counter)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> true }, {})
        def interactionDelegate = Mock(InteractionDelegate)
        def engine = new Engine(interactionDelegate)
        engine.alterInteractionRules([rule1])

        when:
        engine.processLine(Stream.StandardOutput, counter, 'someLine')

        then:
        1 * interactionDelegate.evaluate(_) >> []

        then:
        0 * counter.reset()
        engine.interactionRules == [rule1]
    }

    def 'received a line, rule matched, action declares new rules'() {
        given:
        def counter = Spy(Counter)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> true }, {})
        def rule2 = new InteractionRule(key: 'rule2', { a, b, c, d -> false }, {})
        def interactionDelegate = Mock(InteractionDelegate)
        def engine = new Engine(interactionDelegate)
        engine.alterInteractionRules([rule1])

        when:
        engine.processLine(Stream.StandardOutput, counter, 'someLine')

        then:
        1 * interactionDelegate.evaluate(_) >> [rule2]

        then:
        1 * counter.reset()
        engine.interactionRules.size() == 1
        engine.interactionRules[0].condition == [key: 'rule2']
    }


    def 'offered partial block, no rule matched'() {
        given:
        def counter = Spy(Counter)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> false }, {})
        def interactionDelegate = Mock(InteractionDelegate)
        def engine = new Engine(interactionDelegate)
        engine.alterInteractionRules([rule1])

        when:
        boolean matched = engine.processPartial(Stream.StandardOutput, counter, 'unknownLine')

        then:
        0 * interactionDelegate.evaluate(_)
        0 * counter.reset()

        !matched
        engine.interactionRules == [rule1]
    }

    def 'offered partial block, rule matched, action has no rule'() {
        given:
        def counter = Spy(Counter)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> true }, {})
        def interactionDelegate = Mock(InteractionDelegate)
        def engine = new Engine(interactionDelegate)
        engine.alterInteractionRules([rule1])

        when:
        boolean matched = engine.processPartial(Stream.StandardOutput, counter, 'someLine')

        then:
        1 * interactionDelegate.evaluate(_) >> []

        then:
        0 * counter.reset()

        matched
        engine.interactionRules == [rule1]
    }

    def 'offered partial block, rule matched, action declares new rules'() {
        given:
        def counter = Spy(Counter)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> true }, {})
        def rule2 = new InteractionRule(key: 'rule2', { a, b, c, d -> false }, {})
        def interactionDelegate = Mock(InteractionDelegate)
        def engine = new Engine(interactionDelegate)
        engine.alterInteractionRules([rule1])

        when:
        boolean matched = engine.processPartial(Stream.StandardOutput, counter, 'someLine')

        then:
        1 * interactionDelegate.evaluate(_) >> [rule2]

        then:
        1 * counter.reset()

        matched
        engine.interactionRules.size() == 1
        engine.interactionRules[0].condition == [key: 'rule2']
    }

}
