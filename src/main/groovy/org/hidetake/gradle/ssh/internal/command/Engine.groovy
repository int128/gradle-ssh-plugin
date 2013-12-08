package org.hidetake.gradle.ssh.internal.command

import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.command.Stream
import org.hidetake.gradle.ssh.internal.command.InteractionRule.Event

/**
 * A rule engine for processing stream events.
 *
 * @author hidetake.org
 */
@TupleConstructor
@Slf4j
class Engine {
    final InteractionDelegate interactionDelegate

    private List<InteractionRule> interactionRules
    private depth = new Counter()

    void attach(LineOutputStream lineOutputStream, Stream stream) {
        def counter = new Counter()
        lineOutputStream.lineListeners.add { String line -> processLine(stream, counter, line) }
        lineOutputStream.partialListeners.add { String block -> processPartial(stream, counter, block) }
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
