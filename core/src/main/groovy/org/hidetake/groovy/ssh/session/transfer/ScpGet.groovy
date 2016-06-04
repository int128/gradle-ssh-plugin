package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.session.SessionExtension
import org.hidetake.groovy.ssh.session.transfer.scp.DirectoryReceiver
import org.hidetake.groovy.ssh.session.transfer.scp.FileReceiver
import org.hidetake.groovy.ssh.session.transfer.scp.Walker

@Slf4j
trait ScpGet implements SessionExtension {

    void scpGet(String remotePath, OutputStream stream) {
        assert remotePath, 'remote path must be given'
        assert stream, 'output stream must be given'
        def walker = new Walker(operations, mergedSettings)
        walker.walk(remotePath, new FileReceiver(stream))
    }

    void scpGet(String remotePath, File localFile) {
        assert remotePath, 'remote path must be given'
        assert localFile, 'local file must be given'
        def walker = new Walker(operations, mergedSettings)
        if (localFile.directory) {
            log.debug("Receiving file recursively from $remote.name: $remotePath -> $localFile")
            walker.walk(remotePath, new DirectoryReceiver(localFile))
            log.info("Received file recursively from $remote.name: $remotePath -> $localFile")
        } else {
            log.debug("Receiving file from $remote.name: $remotePath -> $localFile")
            localFile.withOutputStream { stream ->
                walker.walk(remotePath, new FileReceiver(stream))
            }
            log.info("Received file from $remote.name: $remotePath -> $localFile")
        }
    }

}
