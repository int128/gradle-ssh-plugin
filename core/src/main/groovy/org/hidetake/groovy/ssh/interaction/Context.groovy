package org.hidetake.groovy.ssh.interaction

/**
 * A context class of stream interaction.
 * This should be pushed into the stack and replaced on rule matched.
 *
 * @author Hidetake Iwata
 */
class Context {
    final List<Rule> rules

    private long lineNumber = 0

    def Context(List<Rule> rules1) {
        rules = rules1
    }

    /**
     * Find a rule for line match.
     *
     * @param stream
     * @param line
     * @return a rule if it is found, null otherwise
     */
    Rule findRuleForLine(Stream stream, String line) {
        lineNumber++
        rules.find { it.matcher(stream, Event.Line, lineNumber, line) }
    }

    /**
     * Find a rule for partial match.
     *
     * @param stream
     * @param partial
     * @return a rule if it is found, null otherwise
     */
    Rule findRuleForPartial(Stream stream, String partial) {
        rules.find { it.matcher(stream, Event.Partial, lineNumber, partial) }
    }

    @Override
    String toString() {
        "${Context.simpleName}$rules"
    }
}
