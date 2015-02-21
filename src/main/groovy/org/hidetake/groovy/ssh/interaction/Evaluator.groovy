package org.hidetake.groovy.ssh.interaction

import groovy.transform.TupleConstructor

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * An evaluator of interaction rules.
 *
 * @author Hidetake Iwata
 */
@TupleConstructor
class Evaluator {
    final OutputStream standardInput

    /**
     * Evaluate the interaction DSL closure.
     *
     * @param closure interaction DSL closure
     * @return interaction rules
     */
    List<Rule> evaluate(@DelegatesTo(InteractionHandler) Closure closure) {
        def handler = new InteractionHandler(standardInput)
        callWithDelegate(closure, handler)
        handler.rules.asImmutable()
    }
}
