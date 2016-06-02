package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.session.SessionExtension

@Slf4j
trait ScpGet implements SessionExtension {

    void scpGet(String remotePath, OutputStream stream) {
        assert remotePath, 'remote path must be given'
        assert stream, 'output stream must be given'
        def helper = new ScpGetHelper(operations, mergedSettings)
        helper.getFileContent(remotePath, stream)
    }

    void scpGet(String remotePath, File localFile) {
        assert remotePath, 'remote path must be given'
        assert localFile,  'local file must be given'
        def helper = new ScpGetHelper(operations, mergedSettings)
        if (localFile.directory) {
            log.debug("Receiving file recursively from $remote.name: $remotePath -> $localFile")
            helper.get(remotePath, localFile)
            log.info("Received file recursively from $remote.name: $remotePath -> $localFile")
        } else {
            log.debug("Receiving file from $remote.name: $remotePath -> $localFile")
            localFile.withOutputStream { stream ->
                helper.getFileContent(remotePath, stream)
            }
            log.info("Received file from $remote.name: $remotePath -> $localFile")
        }
    }

}
