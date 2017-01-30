package org.hidetake.groovy.ssh.session.transfer.get

/**
 * Represents SCP error.
 *
 * @author Hidetake Iwata
 */
class ScpException extends IOException {
    def ScpException(String message) {
        super(message)
    }

    def ScpException(String message, Throwable cause) {
        super(message, cause)
    }
}
