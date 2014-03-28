package org.hidetake.gradle.ssh.api.operation
/**
 * An exception class thrown if the remote command returns bad exit status.
 *
 * @author hidetake.org
 */
class BadExitStatusException extends RuntimeException {
    final int exitStatus

    def BadExitStatusException(String message, int exitStatus) {
        super(message)
        this.exitStatus = exitStatus
    }
}
