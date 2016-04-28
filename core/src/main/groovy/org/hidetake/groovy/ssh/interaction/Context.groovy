package org.hidetake.groovy.ssh.interaction

import groovy.util.logging.Slf4j

/**
 * A context class of stream interaction.
 * This should be pushed into the stack and replaced on rule matched.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Context {

    private final List<Rule> rules

    def Context(List<Rule> rules1) {
        rules = rules1
    }

    MatchResult match(Stream stream, Buffer buffer) {
        rules.findResult { rule ->
            rule.match(stream, buffer)
        }
    }

    @Override
    String toString() {
        "${Context.simpleName}$rules"
    }
}
