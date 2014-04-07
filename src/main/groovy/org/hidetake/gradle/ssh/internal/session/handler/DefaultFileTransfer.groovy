package org.hidetake.gradle.ssh.internal.session.handler

import com.jcraft.jsch.ChannelSftp.LsEntry
import com.jcraft.jsch.SftpException
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.operation.Operations
import org.hidetake.gradle.ssh.api.session.handler.FileTransfer

/**
 * A default implementation of {@link FileTransfer}.
 *
 * @author hidetake.org
 */
@Slf4j
class DefaultFileTransfer implements FileTransfer {
    @Override
    void get(String givenRemote, String givenLocal) {
        assert operations instanceof Operations
        operations.sftp {
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
    }

    @Override
    void put(String givenLocal, String givenRemote) {
        assert operations instanceof Operations
        operations.sftp {
            final Closure putInternal
            putInternal = { File local, String remote ->
                if (local.directory) {
                    def remoteDir = "$remote/${local.name}"
                    mkdir(remoteDir)
                    local.eachFile(putInternal.rcurry(remoteDir))
                } else {
                    putFile(local.path, remote)
                }
            }

            putInternal(new File(givenLocal), givenRemote)
        }
    }

}
