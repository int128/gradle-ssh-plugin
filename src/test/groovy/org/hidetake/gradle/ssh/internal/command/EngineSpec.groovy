package org.hidetake.gradle.ssh.internal.command

import org.hidetake.gradle.ssh.api.command.Stream
import org.hidetake.gradle.ssh.internal.command.Engine.Counter
import spock.lang.Specification

@SuppressWarnings('GroovyAccessibility')
class EngineSpec extends Specification {

    def 'received a line, no rule matched'() {
        given:
        def counter = Spy(Counter)
        def action1 = Mock(Closure)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> false }, { action1() })
        def engine = new Engine(new InteractionDelegate())
        engine.alterInteractionRules([rule1])

        when:
        engine.processLine(Stream.StandardOutput, counter, 'unknownLine')

        then:
        0 * action1.call()
        0 * counter.reset()
        engine.interactionRules == [rule1]
    }

    def 'received a line, rule matched, action has no rule'() {
        given:
        def counter = Spy(Counter)
        def action1 = Mock(Closure)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> true }, { action1() })
        def engine = new Engine(new InteractionDelegate())
        engine.alterInteractionRules([rule1])

        when:
        engine.processLine(Stream.StandardOutput, counter, 'someLine')

        then:
        1 * action1.call()
        0 * counter.reset()
        engine.interactionRules == [rule1]
    }

    def 'received a line, rule matched, action declares new rules'() {
        given:
        def counter = Spy(Counter)
        def action1 = Mock(Closure)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> true }, {
            action1()
            when(key: 'rule2') {}
        })
        def engine = new Engine(new InteractionDelegate())
        engine.alterInteractionRules([rule1])

        GroovyMock(InteractionRule, global: true)
        1 * InteractionRule.create(_ as Map, _) >> { Map c, a -> new InteractionRule(c, null, null) }

        when:
        engine.processLine(Stream.StandardOutput, counter, 'someLine')

        then:
        1 * action1.call()
        1 * counter.reset()
        engine.interactionRules == [new InteractionRule(key: 'rule2', null, null)]
    }


    def 'offered partial block, no rule matched'() {
        given:
        def counter = Spy(Counter)
        def action1 = Mock(Closure)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> false }, { action1() })
        def engine = new Engine(new InteractionDelegate())
        engine.alterInteractionRules([rule1])

        when:
        boolean matched = engine.processPartial(Stream.StandardOutput, counter, 'unknownLine')

        then:
        !matched
        0 * action1.call()
        0 * counter.reset()
        engine.interactionRules == [rule1]
    }

    def 'offered partial block, rule matched, action has no rule'() {
        given:
        def counter = Spy(Counter)
        def action1 = Mock(Closure)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> true }, { action1() })
        def engine = new Engine(new InteractionDelegate())
        engine.alterInteractionRules([rule1])

        when:
        boolean matched = engine.processPartial(Stream.StandardOutput, counter, 'someLine')

        then:
        matched
        1 * action1.call()
        0 * counter.reset()
        engine.interactionRules == [rule1]
    }

    def 'offered partial block, rule matched, action declares new rules'() {
        given:
        def counter = Spy(Counter)
        def action1 = Mock(Closure)
        def rule1 = new InteractionRule(key: 'rule1', { a, b, c, d -> true }, {
            action1()
            when(key: 'rule2') {}
        })
        def engine = new Engine(new InteractionDelegate())
        engine.alterInteractionRules([rule1])

        GroovyMock(InteractionRule, global: true)
        1 * InteractionRule.create(_ as Map, _) >> { Map c, a -> new InteractionRule(c, null, null) }

        when:
        boolean matched = engine.processPartial(Stream.StandardOutput, counter, 'someLine')

        then:
        matched
        1 * action1.call()
        1 * counter.reset()
        engine.interactionRules == [new InteractionRule(key: 'rule2', null, null)]
    }

}
