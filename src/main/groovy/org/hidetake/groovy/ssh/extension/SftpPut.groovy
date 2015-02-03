package org.hidetake.groovy.ssh.extension

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.SftpException
import org.hidetake.groovy.ssh.session.SessionHandler

import static org.hidetake.groovy.ssh.operation.SftpException.Error.*

/**
 * An extension class to put a file or directory via SFTP.
 *
 * @author hidetake.org
 */
@Category(SessionHandler)
@Slf4j
class SftpPut {
    /**
     * Put file(s) or content to the remote host.
     *
     * @param options file, files, into
     */
    void put(HashMap options) {
        assert options.into, 'into must be given'

        if (options.file) {
            put(options.file, options.into)
        } else if (options.files) {
            put(options.files, options.into)
        } else if (options.text) {
            sftp(sftpPutContent.curry(options.text.toString().bytes, options.into))
        } else if (options.bytes) {
            assert options.bytes instanceof byte[], 'bytes must be an array of byte'
            sftp(sftpPutContent.curry(options.bytes, options.into))
        } else {
            throw new IllegalArgumentException('options of put() must contains file, files, text or bytes')
        }
    }

    private static final sftpPutContent = { byte[] content, String remoteFile ->
        putContent(content, remoteFile)
    }

    /**
     * Put a file or directory to the remote host.
     *
     * @param local
     * @param remote
     */
    void put(File local, String remote) {
        assert remote, 'remote path must be given'
        assert local,  'local file must be given'
        sftp(sftpPutRecursive.curry([local], remote))
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
        sftp(sftpPutRecursive.curry([new File(local)], remote))
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
        sftp(sftpPutRecursive.curry(local, remote))
    }

    private static final sftpPutRecursive = { Iterable<File> localFiles, String remoteFile ->
        final Closure putInternal
        putInternal = { File localPath, String remotePath ->
            if (localPath.directory) {
                def remoteDir = "$remotePath/${localPath.name}"
                try {
                    mkdir(remoteDir)
                } catch (SftpException e) {
                    if (e.error == SSH_FX_FAILURE) {
                        log.info("Remote directory already exists: ${e.localizedMessage}")
                    } else {
                        throw new RuntimeException(e)
                    }
                }
                localPath.eachFile(putInternal.rcurry(remoteDir))
            } else {
                putFile(localPath.path, remotePath)
            }
        }

        localFiles.each { localFile -> putInternal(localFile, remoteFile) }
    }
}
