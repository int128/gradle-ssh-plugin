package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.SftpException as JschSftpException

class SftpNoSuchFileException extends SftpException {
    def SftpNoSuchFileException(String contextMessage, JschSftpException cause) {
        super(contextMessage, cause, SftpError.SSH_FX_NO_SUCH_FILE)
    }
}
