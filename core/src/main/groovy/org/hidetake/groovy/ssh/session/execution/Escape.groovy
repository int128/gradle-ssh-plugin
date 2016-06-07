package org.hidetake.groovy.ssh.session.execution

/**
 * Shell escape utility.
 *
 * @author Hidetake Iwata
 */
class Escape {

    /**
     * Escape command arguments.
     * This method quotes each argument with single-quote.
     * @param arguments
     * @return
     */
    static String escape(List<String> arguments) {
        arguments.collect { /'${it.replaceAll(~/'/, /'\\''/)}'/ }.join(/ /)
    }

}
