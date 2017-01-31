package org.hidetake.groovy.ssh.session.transfer.put

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.operation.SftpException
import org.hidetake.groovy.ssh.session.transfer.FileTransferSettings

import static org.hidetake.groovy.ssh.operation.SftpError.SSH_FX_FAILURE
import static org.hidetake.groovy.ssh.operation.SftpError.SSH_FX_FILE_ALREADY_EXISTS

/**
 * Recursive SFTP PUT executor.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Sftp implements Provider {

    private final Operations operations
    final FileTransferSettings mergedSettings

    def Sftp(Operations operations1, FileTransferSettings mergedSettings1) {
        operations = operations1
        mergedSettings = mergedSettings1
    }

    void put(Instructions instructions) {
        def directoryStack = [instructions.base]
        operations.sftp(mergedSettings) {
            instructions.each { instruction ->
                def remotePath = directoryStack.join('/')

                log.trace("Processing instruction: $instruction")
                switch (instruction) {
                    case File:
                        def file = instruction as File
                        putFile(file.path, remotePath)
                        log.info("Sent file to $remote.name: $file -> $remotePath")
                        break

                    case StreamContent:
                        def content = instruction as StreamContent
                        def remoteFile = "$remotePath/$content.name"
                        putContent(content.stream, remoteFile)
                        log.info("Sent content to $remote.name: $remoteFile")
                        break

                    case EnterDirectory:
                        def directory = instruction as EnterDirectory
                        def remoteDir = "$remotePath/$directory.name"
                        try {
                            mkdir(remoteDir)
                        } catch (SftpException e) {
                            if (e.error in [SSH_FX_FILE_ALREADY_EXISTS, SSH_FX_FAILURE]) {
                                log.info("Remote directory already exists on $remote.name: $remoteDir")
                            } else {
                                throw e
                            }
                        }
                        directoryStack.push(directory.name)
                        break

                    case LeaveDirectory:
                        directoryStack.pop()
                        break

                    default:
                        throw new IllegalStateException("Unknown instruction type: $instruction")
                }
            }
        }
    }

}