package org.hidetake.gradle.ssh.extension

import com.jcraft.jsch.ChannelSftp.LsEntry
import com.jcraft.jsch.SftpException
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.plugin.session.SessionHandler

/**
 * An extension class to get a file or directory via SFTP.
 *
 * @author hidetake.org
 */
@Category(SessionHandler)
@Slf4j
class SftpGet {
    /**
     * Get a file or directory from the remote host.
     *
     * @param remote
     * @param local
     */
    void get(String remote, File local) {
        assert remote, 'remote path must be given'
        assert local,  'local file must be given'
        operations.sftp(sftpGetRecursive.curry(remote, local))
    }

    /**
     * Get a file or directory from the remote host.
     *
     * @param remote
     * @param local
     */
    void get(String remote, String local) {
        assert remote, 'remote path must be given'
        assert local,  'local path must be given'
        operations.sftp(sftpGetRecursive.curry(remote, new File(local)))
    }

    private static final sftpGetRecursive = { String remoteFile, File localFile ->
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
            getFile(remoteFile, localFile.path)
        } catch (SftpException e) {
            if (e.message.startsWith('not supported to get directory')) {
                log.debug(e.localizedMessage)
                log.debug('Starting to get a directory recursively')
                getDirectory(remoteFile, localFile)
            } else {
                throw new RuntimeException(e)
            }
        }
    }
}
