package org.hidetake.groovy.ssh.operation

import com.jcraft.jsch.SftpException as JschSftpException

class SftpFailureException extends SftpException {
    SftpFailureException(String contextMessage, JschSftpException cause) {
        super(contextMessage, cause, SftpError.SSH_FX_FAILURE)
    }
}
