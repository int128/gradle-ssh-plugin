package org.hidetake.gradle.ssh.internal.interaction

import org.hidetake.gradle.ssh.plugin.interaction.InteractionHandler

/**
 * A delegate class for interaction closure.
 *
 * @author hidetake.org
 */
class InteractionDelegate implements InteractionHandler {
    final OutputStream standardInput

    private final List<InteractionRule> interactionRules = []

    def InteractionDelegate(OutputStream standardInput1) {
        standardInput = standardInput1
        assert standardInput
    }

    @Override
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
    def evaluate(Closure closure) {
        interactionRules.clear()
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()

        List<InteractionRule> snapshot = []
        snapshot.addAll(interactionRules)
        interactionRules.clear()
        snapshot.asImmutable()
    }
}
