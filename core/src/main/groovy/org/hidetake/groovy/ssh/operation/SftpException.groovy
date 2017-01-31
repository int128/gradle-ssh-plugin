package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.SftpException as JschSftpException

/**
 * Represents SFTP error.
 *
 * @author Hidetake Iwata
 */
class SftpException extends Exception {
    final SftpError error

    def SftpException(String contextMessage, JschSftpException cause) {
        this(contextMessage, cause, SftpError.find(cause.id))
    }

    def SftpException(String contextMessage, JschSftpException cause, SftpError error) {
        super("$contextMessage: (${error.name()}: ${error.message}): ${cause.message}", cause)
        this.error = error
    }
}
