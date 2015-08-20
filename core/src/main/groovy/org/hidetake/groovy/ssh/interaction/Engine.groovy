package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

/**
 * A rule engine for processing stream events.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Engine {
    private final Evaluator evaluator

    private final Closure interaction

    private List<Rule> rules = []

    private long depth = 0

    private long lineNumber = 0

    /**
     * Constructor.
     *
     * @param evaluator
     * @param interaction interaction DSL closure for initial state
     */
    def Engine(Evaluator evaluator, Closure interaction) {
        this.evaluator = evaluator
        this.interaction = interaction
        assert evaluator
        assert interaction
    }

    void processLine(Stream stream, String text) {
        initializeRulesOnFirstTime()
        lineNumber++
        def rule = rules.find { it.matcher(stream, Event.Line, lineNumber, text) }
        if (rule) {
            log.debug("Rule matched at line $lineNumber from $stream: $rule")
            def evaluatedRules = evaluator.evaluate(rule.action.curry(text))
            if (!evaluatedRules.empty) {
                rules = evaluatedRules
                depth++
                lineNumber = 0
                log.debug("Altered interaction rules on depth $depth: $rules")
            }
        } else {
            log.debug("No rule matched at line $lineNumber from $stream")
        }
    }

    boolean processPartial(Stream stream, String text) {
        initializeRulesOnFirstTime()
        def rule = rules.find { it.matcher(stream, Event.Partial, lineNumber, text) }
        if (rule) {
            log.debug("Rule matched at line $lineNumber from $stream: $rule")
            def evaluatedRules = evaluator.evaluate(rule.action.curry(text))
            if (!evaluatedRules.empty) {
                rules = evaluatedRules
                depth++
                lineNumber = 0
                log.debug("Altered interaction rules on depth $depth: $rules")
            }
            true
        } else {
            false
        }
    }

    private void initializeRulesOnFirstTime() {
        if (depth == 0) {
            rules = evaluator.evaluate(interaction)
            depth++
            log.debug("Initialized interaction rules: $rules")
        }
    }
}
