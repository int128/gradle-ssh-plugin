package org.hidetake.groovy.ssh.session.transfer

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.SftpFailureException
import org.hidetake.groovy.ssh.session.SessionExtension

import static org.hidetake.groovy.ssh.util.Utility.currySelf

/**
 * An extension class to put a file or directory.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait FilePut extends SessionExtension {
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

    /**
     * Put a file to the remote host.
     *
     * @param stream
     * @param remotePath
     */
    void put(InputStream stream, String remotePath) {
        assert stream, 'input stream must be given'
        assert remotePath, 'remote path must be given'
        if (mergedSettings.fileTransfer == FileTransferMethod.sftp) {
            sftp {
                putContent(stream, remotePath)
            }
        } else if (mergedSettings.fileTransfer == FileTransferMethod.scp) {
            def m = remotePath =~ '(.*/)(.+?)'
            if (m.matches()) {
                def remoteBase = m.group(1)
                def remoteFilename = m.group(2)
                def helper = new ScpPutHelper(operations, mergedSettings, remoteBase)
                helper.executeSingle(remoteFilename, stream.bytes)
            } else {
                throw new IllegalArgumentException("Remote path must be an absolute path: $remotePath")
            }
        } else {
            throw new IllegalStateException("Unknown file transfer method: ${mergedSettings.fileTransfer}")
        }
        log.info("Sent content to $remote.name: $remotePath")
    }

    /**
     * Put a file or directory to the remote host.
     *
     * @param localFile
     * @param remotePath
     */
    void put(File localFile, String remotePath) {
        assert remotePath, 'remote path must be given'
        assert localFile, 'local file must be given'
        put([localFile], remotePath)
    }

    /**
     * Put a file or directory to the remote host.
     *
     * @param localPath
     * @param remotePath
     */
    void put(String localPath, String remotePath) {
        assert remotePath, 'remote path must be given'
        assert localPath, 'local path must be given'
        put(new File(localPath), remotePath)
    }

    /**
     * Put a collection of a file or directory to the remote host.
     *
     * @param localFiles
     * @param remotePath
     */
    void put(Iterable<File> localFiles, String remotePath) {
        assert remotePath, 'remote path must be given'
        assert localFiles, 'local files must be given'
        if (mergedSettings.fileTransfer == FileTransferMethod.sftp) {
            sftpPutRecursive(localFiles, remotePath)
        } else if (mergedSettings.fileTransfer == FileTransferMethod.scp) {
            scpPutRecursive(localFiles, remotePath)
        } else {
            throw new IllegalStateException("Unknown file transfer method: ${mergedSettings.fileTransfer}")
        }
    }

    private void sftpPutRecursive(Iterable<File> baseLocalFiles, String baseRemotePath) {
        sftp {
            currySelf { Closure self, Iterable<File> localFiles, String remotePath ->
                localFiles.findAll { !it.directory }.each { localFile ->
                    putFile(localFile.path, remotePath)
                    log.info("Sent file to $remote.name: $localFile.path -> $remotePath")
                }
                localFiles.findAll { it.directory }.each { localDir ->
                    log.debug("Entering directory on $remote.name: $localDir.path")
                    def remoteDir = "$remotePath/${localDir.name}"
                    try {
                        mkdir(remoteDir)
                    } catch (SftpFailureException ignore) {
                        log.info("Remote directory already exists on $remote.name: $remoteDir")
                    }

                    self.call(self, localDir.listFiles().toList(), remoteDir)
                    log.debug("Leaving directory on $remote.name: $localDir.path")
                }
            }.call(baseLocalFiles, baseRemotePath)
        }
    }

    private void scpPutRecursive(Iterable<File> baseLocalFiles, String baseRemotePath) {
        def helper = new ScpPutHelper(operations, mergedSettings, baseRemotePath)
        helper.add(baseLocalFiles)
        helper.execute()
    }
}