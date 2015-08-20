package org.hidetake.groovy.ssh.interaction

import spock.lang.Specification

class EvaluatorSpec extends Specification {

    def 'evaluate() should return an empty list if it has no when()'() {
        given:
        def evaluator = new Evaluator(Mock(OutputStream))

        when:
        def interactionRules = evaluator.evaluate {
        }

        then:
        interactionRules == []
    }

    def 'when() should add an interaction rule'() {
        given:
        def evaluator = new Evaluator(Mock(OutputStream))

        when:
        def interactionRules = evaluator.evaluate {
            when(line: 'value') {}
        }

        then:
        interactionRules.size() == 1
        interactionRules[0].condition == [line: 'value']
    }

    def 'when() should add interaction rules'() {
        given:
        def evaluator = new Evaluator(Mock(OutputStream))

        when:
        def interactionRules = evaluator.evaluate {
            when(line: 'value1') {}
            when(nextLine: 'value2') {}
            when(partial: 'value3') {}
        }

        then:
        interactionRules.size() == 3
        interactionRules[0].condition == [line: 'value1']
        interactionRules[1].condition == [nextLine: 'value2']
        interactionRules[2].condition == [partial: 'value3']
    }

    def 'evaluate() should return independent results'() {
        given:
        def evaluator = new Evaluator(Mock(OutputStream))

        when:
        def interactionRules1 = evaluator.evaluate {
            when(line: 'value1') {}
            when(nextLine: 'value2') {}
        }
        def interactionRules2 = evaluator.evaluate {
            when(partial: 'value3') {}
        }

        then:
        interactionRules1.size() == 2
        interactionRules1[0].condition == [line: 'value1']
        interactionRules1[1].condition == [nextLine: 'value2']

        interactionRules2.size() == 1
        interactionRules2[0].condition == [partial: 'value3']
    }

}
