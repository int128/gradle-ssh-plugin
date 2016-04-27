package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * A class to process lines or partial strings by predefined rules.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Processor {
    private final Closure interactionClosure

    private final OutputStream standardInput

    private final Deque<Context> contextStack = new ArrayDeque<>()

    def Processor(Closure interactionClosure1, OutputStream standardInput1) {
        interactionClosure = interactionClosure1
        standardInput = standardInput1
        assert interactionClosure
        assert standardInput
    }

    void start() {
        contextStack.clear()
        contextStack.push(new Context(evaluateInteractionClosure(interactionClosure)))
        log.trace("Initialized context#${contextStack.size()}: ${contextStack.first}")
    }

    void processLine(Stream stream, String line) {
        def context = contextStack.first
        def innerClosure = context.findRuleForLine(stream, line)
        if (innerClosure) {
            def rules = evaluateInteractionClosure(innerClosure)
            if (!rules.empty) {
                def innerContext = new Context(rules)
                contextStack.push(innerContext)
                log.trace("Entering context#${contextStack.size()}: $innerContext")
            }
        } else {
            log.trace("No rule matched: from: $stream, line: $line")
        }
    }

    void processPartial(Stream stream, String partial) {
        def context = contextStack.first
        def innerClosure = context.findRuleForPartial(stream, partial)
        if (innerClosure) {
            def rules = evaluateInteractionClosure(innerClosure)
            if (!rules.empty) {
                def innerContext = new Context(rules)
                contextStack.push(innerContext)
                log.trace("Entering context#${contextStack.size()}: $innerContext")
            }
        } else {
            log.trace("No rule matched: from: $stream, partial: $partial")
        }
    }

    private evaluateInteractionClosure(Closure interactionClosure) {
        def handler = new InteractionHandler(standardInput)
        callWithDelegate(interactionClosure, handler)
        handler.rules
    }
}
