package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

/**
 * A listener of lines and partial strings from streams.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Listener {
    private final List<Processor> processors = []

    void add(Processor processor) {
        processors.add(processor)
    }

    void start() {
        processors*.start()
    }

    void processLine(Stream stream, String line) {
        log.trace("Finding match: from: $stream, line: $line")
        processors*.processLine(stream, line)
    }

    void processPartial(Stream stream, String partial) {
        log.trace("Finding match: from: $stream, partial: $partial")
        processors*.processPartial(stream, partial)
    }
}
