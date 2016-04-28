package org.hidetake.groovy.ssh.interaction

import groovy.transform.EqualsAndHashCode

/**
 * A rule of interaction with the stream.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
class Rule {

    final Map<String, Object> condition

    final StreamRule streamRule
    final BufferRule bufferRule

    final Closure action

    def Rule(Map<String, Object> condition1, Closure action1) {
        condition = condition1
        action = action1

        streamRule = StreamRule.Factory.create(condition.from)
        bufferRule = BufferRule.Factory.create(condition)
    }

    MatchResult match(Stream stream, Buffer buffer) {
        if (streamRule.matches(stream)) {
            def matchResult = bufferRule.match(buffer)
            if (matchResult != null) {
                new MatchResult(this, matchResult)
            } else {
                null
            }
        } else {
            null
        }
    }

    String toString() {
        "${Rule.simpleName}${condition}"
    }
}
