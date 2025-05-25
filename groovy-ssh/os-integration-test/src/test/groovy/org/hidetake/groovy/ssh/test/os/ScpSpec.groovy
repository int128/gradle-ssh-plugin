package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.session.transfer.FileTransferMethod
import spock.lang.Timeout

/**
 * Check if file transfer works with SCP command of OpenSSH.
 *
 * @author Hidetake Iwata
 */
@Timeout(10)
class ScpSpec extends AbstractFileTransferSpec {

    def setup() {
        ssh.settings {
            fileTransfer = FileTransferMethod.scp
        }
    }

}
