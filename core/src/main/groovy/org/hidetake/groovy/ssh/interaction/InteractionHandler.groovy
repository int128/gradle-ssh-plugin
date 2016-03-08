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

    final List<Rule> rules = []

    def InteractionHandler(OutputStream standardInput1) {
        standardInput = standardInput1
        assert standardInput
    }

    /**
     * Declare an interaction rule.
     *
     * @param condition map of condition
     * @param action the action performed if condition satisfied
     */
    void when(Map condition, Closure action) {
        assert condition, 'at least one rule must be given'
        assert action, 'closure must be given'
        rules.add(new Rule(condition, action))
    }
}
