package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * A class to process received bytes by predefined rules.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Processor {
    private final Closure initialInteractionClosure
    private final OutputStream standardInput
    private final String encoding

    private final buffers = new EnumMap<Stream, Buffer>(Stream)
    private final contextStack = new ArrayDeque<Context>()

    def Processor(Closure interactionClosure1, OutputStream standardInput1, String encoding1) {
        initialInteractionClosure = interactionClosure1
        standardInput = standardInput1
        encoding = encoding1
    }

    synchronized void start(Stream stream) {
        if (contextStack.empty) {
            contextStack.push(new Context(evaluateInteractionClosure(initialInteractionClosure)))
            log.trace("Initialized context#$currentContextDepth: $currentContext")
        }
        buffers.put(stream, new Buffer(encoding))
    }

    void receive(Stream stream, byte[] receivedBytes, int length) {
        buffers.get(stream).append(receivedBytes, length)
        repeatMatch(stream)
    }

    void end(Stream stream) {
        repeatMatch(stream)
        def buffer = buffers.get(stream)
        if (buffer.size() > 0) {
            log.trace("Matching ${buffer.size()} bytes left in buffer of $stream by adding new-line")
            buffer.append('\n')
            repeatMatch(stream)

            log.trace("${buffer.size()} bytes left in buffer of $stream at last")
        }
    }

    private void repeatMatch(Stream stream) {
        while (true) {
            def matchResult = currentContext.match(stream, buffers.get(stream))
            if (matchResult) {
                log.trace("Rule matched for $stream on context#$currentContextDepth: $matchResult")
                def rules = evaluateInteractionClosure(matchResult.actionWithResult)
                if (!rules.empty) {
                    def innerContext = new Context(rules)
                    contextStack.push(innerContext)
                    log.trace("Entering context#$currentContextDepth: $innerContext")
                }
            } else {
                log.trace("No more rule matched for $stream on context#$currentContextDepth")
                break
            }
        }
    }

    private getCurrentContext() {
        assert !contextStack.empty, 'start() must be called at first'
        contextStack.first
    }

    private getCurrentContextDepth() {
        contextStack.size()
    }

    private evaluateInteractionClosure(Closure interactionClosure) {
        def handler = new InteractionHandler(standardInput)
        callWithDelegate(interactionClosure, handler)

        if (handler.popContext) {
            if (!handler.when.empty) {
                throw new IllegalStateException("popContext() should not be called with when(): $handler.when")
            }
            if (currentContextDepth < 2) {
                throw new IllegalStateException("popContext() should not be called on top context: currentContextDepth=$currentContextDepth")
            }
            log.trace("Leaving context#$currentContextDepth")
            contextStack.pop()
        }

        handler.when
    }
}
