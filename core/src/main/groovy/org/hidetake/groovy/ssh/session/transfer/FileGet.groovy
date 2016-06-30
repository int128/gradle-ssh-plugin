package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.session.SessionExtension
import org.hidetake.groovy.ssh.session.transfer.get.*

/**
 * An extension class to get a file or directory.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait FileGet implements SessionExtension {

    /**
     * Get file(s) or content from the remote host.
     *
     * @param map <code>from</code> and <code>into</code>
     * @returns content as a string if <code>into</into> is not given
     */
    def get(HashMap map) {
        final usage = """Got $map but not following signatures:
get(from: String, into: String)             // get a file or directory
get(from: String, into: File)               // get a file or directory
get(from: String, into: File, filter: {})   // get a files by file filter
get(from: String, into: OutputStream)       // get a file into the stream
get(from: String)                           // get a file and return the content"""

        if (map.containsKey('from')) {
            assert map.from != null, 'from must not be null'
            if (map.containsKey('into')) {
                assert map.into != null, 'into must not be null'
                try {
                    if (map.containsKey('filter')) {
                        assert map.filter != null, 'filter must not be null'
                        //noinspection GroovyAssignabilityCheck
                        getInternal(map.from as String, map.into, map.filter as Closure)
                    } else {
                        //noinspection GroovyAssignabilityCheck
                        getInternal(map.from as String, map.into)
                    }
                } catch (MissingMethodException e) {
                    throw new IllegalArgumentException(usage, e)
                }
            } else {
                def stream = new ByteArrayOutputStream()
                getInternal(map.from as String, stream)
                new String(stream.toByteArray())
            }
        } else {
            throw new IllegalArgumentException(usage)
        }
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remotePath
     * @param stream
     */
    private void getInternal(String remotePath, OutputStream stream) {
        getInternalWithReceiver(remotePath, new StreamReceiver(stream))
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remotePath
     * @param localPath
     */
    private void getInternal(String remotePath, String localPath) {
        getInternal(remotePath, new File(localPath))
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remotePath
     * @param localPath
     */
    private void getInternal(String remotePath, String localPath, Closure<Boolean> filter) {
        getInternal(remotePath, new File(localPath), filter)
    }

    /**
     * Get a file from the remote host.
     *
     * @param remotePath
     * @param localFile
     */
    private void getInternal(String remotePath, File localFile) {
        if (localFile.directory) {
            getInternalWithReceiver(remotePath, new RecursiveReceiver(localFile, null))
        } else {
            getInternalWithReceiver(remotePath, new FileReceiver(localFile))
        }
    }

    /**
     * Get a file from the remote host.
     *
     * @param remotePath
     * @param localFile
     */
    private void getInternal(String remotePath, File localFile, Closure<Boolean> filter) {
        if (localFile.directory) {
            getInternalWithReceiver(remotePath, new RecursiveReceiver(localFile, filter))
        } else {
            if (filter.call(localFile)) {
                getInternalWithReceiver(remotePath, new FileReceiver(localFile))
            } else {
                log.debug("Skipped transfer because filter returned false: $remotePath -> $localFile")
            }
        }
    }

    private void getInternalWithReceiver(String remotePath, def receiver) {
        if (mergedSettings.fileTransfer == FileTransferMethod.sftp) {
            //noinspection GroovyAssignabilityCheck
            new Sftp(operations).get(remotePath, receiver)
        } else if (mergedSettings.fileTransfer == FileTransferMethod.scp) {
            //noinspection GroovyAssignabilityCheck
            new Scp(operations, mergedSettings).get(remotePath, receiver)
        } else {
            throw new IllegalStateException("Unknown file transfer method: ${mergedSettings.fileTransfer}")
        }
    }

}