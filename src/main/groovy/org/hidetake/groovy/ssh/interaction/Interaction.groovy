package org.hidetake.groovy.ssh.interaction

import org.hidetake.groovy.ssh.operation.LineOutputStream

/**
 * An entry point of interaction support classes.
 *
 * @author Hidetake Iwata
 */
class Interaction {

    /**
     * Enable interaction support.
     *
     * @param interaction interaction DSL closure
     * @param standardInput standard input stream
     * @param standardOutput standard output stream
     * @param standardError standard error stream (optional)
     */
    static void enable(Closure interaction,
                       OutputStream standardInput,
                       LineOutputStream standardOutput,
                       LineOutputStream standardError = null) {
        def evaluator = new Evaluator(standardInput)
        def engine = new Engine(evaluator, interaction)

        standardOutput.listenLine { String line -> engine.processLine(Stream.StandardOutput, line) }
        standardOutput.listenPartial { String block -> engine.processPartial(Stream.StandardOutput, block) }

        if (standardError) {
            standardError.listenLine { String line -> engine.processLine(Stream.StandardError, line) }
            standardError.listenPartial { String block -> engine.processPartial(Stream.StandardError, block) }
        }
    }

}
