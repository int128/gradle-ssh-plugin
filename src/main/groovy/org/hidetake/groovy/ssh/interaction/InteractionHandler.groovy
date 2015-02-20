package org.hidetake.groovy.ssh.interaction

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * A handler of the {@link org.hidetake.groovy.ssh.core.settings.OperationSettings#interaction} closure.
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

    private final List<InteractionRule> interactionRules = []

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
        interactionRules << new InteractionRule(condition, Matcher.generate(condition), action)
    }

    /**
     * Evaluate the closure.
     *
     * @param closure
     * @return interaction rules declared by the closure
     */
    def evaluate(@DelegatesTo(InteractionHandler) Closure closure) {
        interactionRules.clear()
        callWithDelegate(closure, this)

        List<InteractionRule> snapshot = []
        snapshot.addAll(interactionRules)
        interactionRules.clear()
        snapshot.asImmutable()
    }
}
