package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.SftpException as JschSftpException

/**
 * Represents SFTP error.
 *
 * @author Hidetake Iwata
 */
class SftpException extends Exception {
    final SftpError error

    static SftpException createFrom(JschSftpException cause, String contextMessage) {
        def sftpError = SftpError.find(cause.id)
        if (sftpError == SftpError.SSH_FX_NO_SUCH_FILE) {
            new SftpNoSuchFileException(contextMessage, cause)
        } else if (sftpError == SftpError.SSH_FX_FAILURE) {
            new SftpFailureException(contextMessage, cause)
        } else {
            new SftpException(contextMessage, cause, sftpError)
        }
    }

    protected def SftpException(String contextMessage, JschSftpException cause, SftpError error) {
        super("$contextMessage: (${error.name()}: ${error.message}): ${cause.message}", cause)
        this.error = error
    }
}
