package org.hidetake.gradle.ssh.extension

import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.plugin.session.SessionHandler

/**
 * An extension class to put a file or directory via SFTP.
 *
 * @author hidetake.org
 */
@Category(SessionHandler)
@Slf4j
class SftpPut {
    /**
     * Put a file or directory to the remote host.
     *
     * @param local
     * @param remote
     */
    void put(File local, String remote) {
        assert remote, 'remote path must be given'
        assert local,  'local file must be given'
        operations.sftp(sftpPutRecursive.curry([local], remote))
    }

    /**
     * Put a file or directory to the remote host.
     *
     * @param local
     * @param remote
     */
    void put(String local, String remote) {
        assert remote, 'remote path must be given'
        assert local,  'local path must be given'
        operations.sftp(sftpPutRecursive.curry([new File(local)], remote))
    }

    /**
     * Put a collection of a file or directory to the remote host.
     *
     * @param local
     * @param remote
     */
    void put(Iterable<File> local, String remote) {
        assert remote, 'remote path must be given'
        assert local,  'local path must be given'
        operations.sftp(sftpPutRecursive.curry(local, remote))
    }

    private static final sftpPutRecursive = { Iterable<File> localFiles, String remoteFile ->
        final Closure putInternal
        putInternal = { File localPath, String remotePath ->
            if (localPath.directory) {
                def remoteDir = "$remotePath/${localPath.name}"
                mkdir(remoteDir)
                localPath.eachFile(putInternal.rcurry(remoteDir))
            } else {
                putFile(localPath.path, remotePath)
            }
        }

        localFiles.each { localFile -> putInternal(localFile, remoteFile) }
    }
}
