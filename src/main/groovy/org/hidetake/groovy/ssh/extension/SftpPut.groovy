package org.hidetake.groovy.ssh.extension

import groovy.transform.ToString
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
    @ToString
    private static class PutOptions {
        def from
        def text
        def bytes
        def into

        static usage = '''put() accepts following signatures:
put(from: String or File, into: String)  // put a file or directory
put(from: Iterable<File>, into: String) // put files or directories
put(from: InputStream, into: String)     // put a stream into the remote file
put(text: String, into: String)          // put a string into the remote file
put(bytes: byte[], into: String)         // put a byte array into the remote file'''

        static create(HashMap map) {
            try {
                assert map.into, 'into must be given'
                new PutOptions(map)
            } catch (MissingPropertyException e) {
                throw new IllegalArgumentException(usage, e)
            } catch (AssertionError e) {
                throw new IllegalArgumentException(usage, e)
            }
        }
    }

    /**
     * Put file(s) or content to the remote host.
     */
    void put(HashMap map) {
        def options = PutOptions.create(map)
        if (options.from) {
            put(options.from, options.into)
        } else if (options.text) {
            def stream = new ByteArrayInputStream(options.text.toString().bytes)
            put(stream, options.into)
        } else if (options.bytes) {
            def stream = new ByteArrayInputStream(options.bytes as byte[])
            put(stream, options.into)
        } else {
            throw new IllegalArgumentException(PutOptions.usage)
        }
    }

    private static final sftpPutContent = { InputStream stream, String remoteFile ->
        putContent(stream, remoteFile)
    }

    /**
     * Put a file to the remote host.
     *
     * @param local
     * @param remote
     */
    void put(InputStream stream, String remote) {
        assert remote, 'remote path must be given'
        assert stream, 'input stream must be given'
        sftp(sftpPutContent.curry(stream, remote))
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
