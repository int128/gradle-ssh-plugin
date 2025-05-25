package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

/**
 * A listener of lines and partial strings from streams.
 * It notifies events to {@link Processor}s on start, received bytes and end of stream.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Listener {
    private final List<Processor> processors = []

    void add(Processor processor) {
        processors.add(processor)
    }

    void start(Stream stream) {
        processors*.start(stream)
    }

    void receive(Stream stream, byte[] bytes, int length) {
        processors*.receive(stream, bytes, length)
    }

    void end(Stream stream) {
        processors*.end(stream)
    }
}
