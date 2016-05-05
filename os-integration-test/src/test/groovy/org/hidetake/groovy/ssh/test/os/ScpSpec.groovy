package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.extension.settings.FileTransferMethod

/**
 * Check if file transfer works with SCP command of OpenSSH.
 *
 * @author Hidetake Iwata
 */
class ScpSpec extends AbstractFileTransferSpec {

    def setup() {
        ssh.settings {
            fileTransfer = FileTransferMethod.scp
        }
    }

}
