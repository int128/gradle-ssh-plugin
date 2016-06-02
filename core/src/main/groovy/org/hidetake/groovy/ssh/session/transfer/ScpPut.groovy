package org.hidetake.groovy.ssh.session.transfer

import org.hidetake.groovy.ssh.session.SessionExtension

trait ScpPut implements SessionExtension {

    void scpPut(InputStream stream, String remotePath) {
        def m = remotePath =~ '(.*/)(.+?)'
        if (m.matches()) {
            def remoteBase = m.group(1)
            def remoteFilename = m.group(2)
            def helper = new ScpPutHelper(operations, mergedSettings, remoteBase)
            helper.executeSingle(remoteFilename, stream.bytes)
        } else {
            throw new IllegalArgumentException("Remote path must be an absolute path: $remotePath")
        }
    }

    void scpPut(Iterable<File> baseLocalFiles, String baseRemotePath) {
        def helper = new ScpPutHelper(operations, mergedSettings, baseRemotePath)
        helper.add(baseLocalFiles)
        helper.execute()
    }

}
