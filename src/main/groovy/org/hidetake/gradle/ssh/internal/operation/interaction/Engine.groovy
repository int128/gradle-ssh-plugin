package org.hidetake.gradle.ssh.internal.operation.interaction

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.operation.interaction.Stream
import org.hidetake.gradle.ssh.internal.operation.interaction.Matcher.Event

/**
 * A rule engine for processing stream events.
 *
 * @author hidetake.org
 */
@Slf4j
class Engine {
    private final InteractionDelegate interactionDelegate

    protected List<InteractionRule> interactionRules = []

    private depth = new Counter()

    def Engine(InteractionDelegate interactionDelegate1) {
        interactionDelegate = interactionDelegate1
        assert interactionDelegate
    }

    void attach(LineOutputStream lineOutputStream, Stream stream) {
        def counter = new Counter()
        lineOutputStream.listenLine { String line -> processLine(stream, counter, line) }
        lineOutputStream.listenPartial { String block -> processPartial(stream, counter, block) }
    }

    void alterInteractionRules(List<InteractionRule> alternative) {
        interactionRules = alternative
        depth++
        log.debug("Rules have been altered (depth=$depth, rules=$interactionRules)")
    }

    void processLine(Stream stream, Counter lineNumber, String text) {
        lineNumber++
        def rule = interactionRules.find { it.matcher(stream, Event.Line, lineNumber.value, text) }
        if (rule) {
            log.debug("Rule matched at line $lineNumber from $stream: $rule")
            def evaluated = interactionDelegate.evaluate(rule.action.curry(text))
            if (!evaluated.empty) {
                alterInteractionRules(evaluated)
                lineNumber.reset()
            }
        } else {
            log.debug("No rule matched at line $lineNumber from $stream")
        }
    }

    boolean processPartial(Stream stream, Counter lineNumber, String text) {
        def rule = interactionRules.find { it.matcher(stream, Event.Partial, lineNumber.value, text) }
        if (rule) {
            log.debug("Rule matched at line $lineNumber from $stream: $rule")
            def evaluated = interactionDelegate.evaluate(rule.action.curry(text))
            if (!evaluated.empty) {
                alterInteractionRules(evaluated)
                lineNumber.reset()
            }
            true
        } else {
            false
        }
    }

    @TupleConstructor
    static class Counter {
        private long value = 0

        long getValue() {
            value
        }

        def next() {
            value++
            this
        }

        void reset() {
            value = 0
        }

        String toString() {
            value.toString()
        }
    }
}
