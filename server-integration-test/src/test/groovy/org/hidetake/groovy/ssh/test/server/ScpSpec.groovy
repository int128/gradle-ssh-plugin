package org.hidetake.groovy.ssh.test.server

import org.apache.sshd.server.command.ScpCommandFactory
import org.hidetake.groovy.ssh.session.transfer.FileTransferMethod
import spock.lang.Timeout

@Timeout(10)
class ScpSpec extends AbstractFileTransferSpecification {

    def setupSpec() {
        server.commandFactory = new ScpCommandFactory()
        server.start()
    }

    def setup() {
        ssh.settings {
            fileTransfer = FileTransferMethod.scp
        }
    }

}
