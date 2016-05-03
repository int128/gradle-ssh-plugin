package org.hidetake.groovy.ssh.interaction

/**
 * A handler of the interaction closure.
 *
 * @author Hidetake Iwata
 */
class InteractionHandler {
    /**
     * Wildcard for condition expression.
     */
    static final _ = Wildcard.instance

    static final standardOutput = Stream.StandardOutput

    static final standardError = Stream.StandardError

    /**
     * A standard input for the remote command.
     */
    final OutputStream standardInput

    final List<Rule> when = []
    boolean popContext = false

    def InteractionHandler(OutputStream standardInput1) {
        standardInput = standardInput1
        assert standardInput
    }

    /**
     * Declare an interaction rule.
     *
     * @param condition see {@link StreamRule} and {@link BufferRule} for details
     * @param action closure called with result ({@link String}, {@link java.util.regex.Matcher} or {@code byte[]}) when condition is satisfied
     */
    void when(Map condition, Closure action) {
        assert condition, 'at least one rule must be given'
        assert action, 'closure must be given'
        when.add(new Rule(condition, action))
    }

    /**
     * Pop context stack of {@link Processor}.
     * This should not be used with {@link #when(java.util.Map, groovy.lang.Closure)}.
     */
    void popContext() {
        popContext = true
    }
}
