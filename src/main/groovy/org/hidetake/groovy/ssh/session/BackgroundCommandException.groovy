package org.hidetake.groovy.ssh.session

import groovy.transform.CompileStatic

/**
 * An exception thrown if at least one background command occurs any error.
 *
 * @author hidetake.org
 */
@CompileStatic
class BackgroundCommandException extends RuntimeException {
    final List<Exception> exceptionsOfBackgroundExecution

    def BackgroundCommandException(List<Exception> exceptionsOfBackgroundExecution) {
        super('Error in background command execution')
        this.exceptionsOfBackgroundExecution = exceptionsOfBackgroundExecution
    }
}
