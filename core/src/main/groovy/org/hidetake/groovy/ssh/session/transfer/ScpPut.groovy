package org.hidetake.groovy.ssh.session.transfer

import org.hidetake.groovy.ssh.session.SessionExtension
import org.hidetake.groovy.ssh.session.transfer.scp.Instructions
import org.hidetake.groovy.ssh.session.transfer.scp.Sender

trait ScpPut implements SessionExtension {

    void scpPut(InputStream stream, String remotePath) {
        assert stream, 'input stream must be given'
        assert remotePath, 'remote path must be given'
        def sender = new Sender(operations, mergedSettings)
        def instructions = new Instructions(stream.bytes, remotePath)
        sender.send(instructions)
    }

    void scpPut(Iterable<File> localFiles, String remotePath) {
        assert remotePath, 'remote path must be given'
        assert localFiles, 'local files must be given'
        def sender = new Sender(operations, mergedSettings)
        def instructions = new Instructions(localFiles, remotePath)
        sender.send(instructions)
    }

}
