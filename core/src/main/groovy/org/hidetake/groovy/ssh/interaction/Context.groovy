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

    private long lineNumber = 0

    def Context(List<Rule> rules1) {
        rules = rules1
    }

    /**
     * Find a matched rule for the line.
     *
     * @param stream
     * @param line
     * @return a closure curried with String or {@link java.util.regex.Pattern} if matched, or null otherwise
     */
    Closure findRuleForLine(Stream stream, String line) {
        lineNumber++
        for (Rule rule : rules) {
            def matcher = rule.matcher(stream, Event.Line, lineNumber, line)
            if (matcher != null) {
                log.trace("Rule matched: from: $stream, line: $line -> $rule")
                return rule.action.curry(matcher)
            }
        }
        return null
    }

    /**
     * Find a matched rule for the partial string.
     *
     * @param stream
     * @param partial
     * @return a closure curried with String or {@link java.util.regex.Pattern} if matched, or null otherwise
     */
    Closure findRuleForPartial(Stream stream, String partial) {
        for (Rule rule : rules) {
            def matcher = rule.matcher(stream, Event.Partial, lineNumber, partial)
            if (matcher != null) {
                log.trace("Rule matched: from: $stream, partial: $partial -> $rule")
                return rule.action.curry(matcher)
            }
        }
        return null
    }

    @Override
    String toString() {
        "${Context.simpleName}$rules"
    }

}
