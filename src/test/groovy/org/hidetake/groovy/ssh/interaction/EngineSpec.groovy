package org.hidetake.groovy.ssh.interaction

import spock.lang.Specification

class EngineSpec extends Specification {

    private static final dummyAction = {}
    private static final dummyInteraction = {}

    private static matcher(Closure<Boolean> closure = { -> false }) {
        { Stream stream, Event event, long lineNumber, String text -> closure() }
    }


    def 'initial properties should be following'() {
        when:
        def evaluator = Mock(Evaluator)
        def interaction = Mock(Closure)
        def engine = new Engine(evaluator, interaction)

        then:
        engine.interaction == interaction
        engine.lineNumber == 0
        engine.depth == 0
        engine.rules == []
    }


    def 'lineNumber and depth should be +1 if it received a line'() {
        given:
        def matcher1 = Mock(Closure)
        def rule1 = new Rule(key: 'rule1', matcher(matcher1), dummyAction)

        def evaluator = Mock(Evaluator)
        def engine = new Engine(evaluator, dummyInteraction)

        when:
        engine.processLine(Stream.StandardOutput, 'unknownLine')

        then:
        1 * evaluator.evaluate(_) >> [rule1]

        then:
        1 * matcher1.call() >> false

        then:
        engine.lineNumber == 1
        engine.depth == 1
        engine.rules == [rule1]

        when:
        engine.processLine(Stream.StandardOutput, 'unknownLine')

        then:
        engine.lineNumber == 2
    }

    def 'depth should be kept if it received a line but current rule has no child rule'() {
        given:
        def matcher1 = Mock(Closure)
        def rule1 = new Rule(key: 'rule1', matcher(matcher1), dummyAction)

        def evaluator = Mock(Evaluator)
        def engine = new Engine(evaluator, dummyInteraction)

        when:
        engine.processLine(Stream.StandardOutput, 'someLine')

        then:
        1 * evaluator.evaluate(_) >> [rule1]

        then:
        1 * matcher1.call() >> true

        then:
        1 * evaluator.evaluate(_) >> []

        then:
        engine.lineNumber == 1
        engine.depth == 1
        engine.rules == [rule1]
    }

    def 'depth should be +1 if it received a line and current rule has child rules'() {
        given:
        def matcher1 = Mock(Closure)
        def rule1 = new Rule(key: 'rule1', matcher(matcher1), dummyAction)
        def rule2 = new Rule(key: 'rule2', matcher(), dummyAction)

        def evaluator = Mock(Evaluator)
        def engine = new Engine(evaluator, dummyInteraction)

        when:
        engine.processLine(Stream.StandardOutput, 'someLine')

        then:
        1 * evaluator.evaluate(_) >> [rule1]

        then:
        1 * matcher1.call() >> true

        then:
        1 * evaluator.evaluate(_) >> [rule2]

        then:
        engine.lineNumber == 0
        engine.depth == 2
        engine.rules == [rule2]
    }


    def 'lineNumber and depth should kept if it received a partial block but none matched'() {
        given:
        def matcher1 = Mock(Closure)
        def rule1 = new Rule(key: 'rule1', matcher(matcher1), dummyAction)

        def evaluator = Mock(Evaluator)
        def engine = new Engine(evaluator, dummyInteraction)

        when:
        boolean matched = engine.processPartial(Stream.StandardOutput, 'unknownLine')

        then:
        1 * evaluator.evaluate(_) >> [rule1]

        then:
        1 * matcher1.call() >> false

        then:
        !matched
        engine.lineNumber == 0
        engine.depth == 1
        engine.rules == [rule1]
    }

    def 'depth should be kept if it received a partial block but current rule has no child rule'() {
        given:
        def matcher1 = Mock(Closure)
        def rule1 = new Rule(key: 'rule1', matcher(matcher1), dummyAction)

        def evaluator = Mock(Evaluator)
        def engine = new Engine(evaluator, dummyInteraction)

        when:
        boolean matched = engine.processPartial(Stream.StandardOutput, 'someLine')

        then:
        1 * evaluator.evaluate(_) >> [rule1]

        then:
        1 * matcher1.call() >> true

        then:
        1 * evaluator.evaluate(_) >> []

        then:
        matched
        engine.lineNumber == 0
        engine.depth == 1
        engine.rules == [rule1]
    }

    def 'depth should be +1 if it received a partial block and current rule has child rules'() {
        given:
        def matcher1 = Mock(Closure)
        def rule1 = new Rule(key: 'rule1', matcher(matcher1), dummyAction)
        def rule2 = new Rule(key: 'rule2', matcher(), dummyAction)

        def evaluator = Mock(Evaluator)
        def engine = new Engine(evaluator, dummyInteraction)

        when:
        boolean matched = engine.processPartial(Stream.StandardOutput, 'someLine')

        then:
        1 * evaluator.evaluate(_) >> [rule1]

        then:
        1 * matcher1.call() >> true

        then:
        1 * evaluator.evaluate(_) >> [rule2]

        then:
        matched
        engine.lineNumber == 0
        engine.depth == 2
        engine.rules == [rule2]
    }

}
