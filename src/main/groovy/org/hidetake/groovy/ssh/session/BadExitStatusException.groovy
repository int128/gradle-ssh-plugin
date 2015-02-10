package org.hidetake.groovy.ssh.session

import groovy.transform.CompileStatic

/**
 * An exception class thrown if the remote command returns bad exit status.
 *
 * @author hidetake.org
 */
@CompileStatic
class BadExitStatusException extends RuntimeException {
    final int exitStatus

    def BadExitStatusException(String message, int exitStatus) {
        super(message)
        this.exitStatus = exitStatus
    }
}
