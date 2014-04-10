package org.hidetake.gradle.ssh.extension

import com.jcraft.jsch.ChannelSftp.LsEntry
import com.jcraft.jsch.SftpException
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.plugin.session.SessionHandler

/**
 * An extension class of file transfer.
 *
 * @author hidetake.org
 */
@Category(SessionHandler)
@Slf4j
class FileTransfer {
    /**
     * Get a file from the remote host.
     *
     * @param remote
     * @param local
     */
    void get(String remote, String local) {
        assert remote, 'remote path must be given'
        assert local,  'local path must be given'
        operations.sftp(sftpGetRecursive.curry(remote, local))
    }

    private static final sftpGetRecursive = { String givenRemote, String givenLocal ->
        final Closure getDirectory
        getDirectory = { String remoteDir, File localDir ->
            def remoteDirName = remoteDir.find(~'[^/]+/?$')
            def localChildDir = new File(localDir, remoteDirName)
            localChildDir.mkdirs()

            cd(remoteDir)
            List<LsEntry> children = ls('.')
            children.findAll { !it.attrs.dir }.each {
                getFile(it.filename, localChildDir.path)
            }
            children.findAll { it.attrs.dir }.each {
                switch (it.filename) {
                    case '.':
                    case '..':
                        log.debug("Ignored a directory entry: ${it.longname}")
                        break
                    default:
                        getDirectory(it.filename, localChildDir)
                }
            }
            cd('..')
        }

        try {
            getFile(givenRemote, givenLocal)
        } catch (SftpException e) {
            if (e.message.startsWith('not supported to get directory')) {
                log.debug(e.localizedMessage)
                log.debug('Starting to get a directory recursively')
                getDirectory(givenRemote, new File(givenLocal))
            } else {
                throw new RuntimeException(e)
            }
        }
    }

    /**
     * Put a file to the remote host.
     *
     * @param local
     * @param remote
     */
    void put(String local, String remote) {
        assert remote, 'remote path must be given'
        assert local,  'local path must be given'
        operations.sftp(sftpPutRecursive.curry(local, remote))
    }

    private static final sftpPutRecursive = { String givenLocal, String givenRemote ->
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

        putInternal(new File(givenLocal), givenRemote)
    }
}
