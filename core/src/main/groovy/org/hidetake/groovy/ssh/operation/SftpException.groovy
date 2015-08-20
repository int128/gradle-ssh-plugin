package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.SftpException as JschSftpException
import groovy.transform.CompileStatic

/**
 * Represents SFTP error.
 *
 * @author Hidetake Iwata
 */
@CompileStatic
class SftpException extends Exception {
    final SftpError error

    def SftpException(String contextMessage, JschSftpException cause) {
        this(contextMessage, cause, SftpError.find(cause.id))
    }

    private def SftpException(String contextMessage, JschSftpException cause, SftpError error) {
        super("$contextMessage: (${error?.name()}: ${error?.message}): ${cause.message}", cause)
        this.error = error
    }
}
