package org.hidetake.groovy.ssh.session.transfer.get

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.Operations

import static org.hidetake.groovy.ssh.util.Utility.currySelf

/**
 * A helper class for SFTP get operation.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Sftp {

    final Operations operations

    def Sftp(Operations operations1) {
        operations = operations1
    }

    void get(String remotePath, FileReceiver receiver) {
        operations.sftp {
            getFile(remotePath, receiver.destination.path)
            log.info("Received file from $operations.remote.name: $remotePath -> $receiver.destination")
        }
    }

    void get(String remotePath, StreamReceiver receiver) {
        operations.sftp {
            getContent(remotePath, receiver.stream)
            log.info("Received content from $operations.remote.name: $remotePath")
        }
    }

    void get(String remotePath, RecursiveReceiver receiver) {
        operations.sftp {
            def remoteAttrs = stat(remotePath)
            if (remoteAttrs.dir) {
                log.debug("Entering directory on $remote.name: $remotePath")
                cd(remotePath)
                receiver.enterDirectory(remotePath.find(~'[^/]+/?$'))

                currySelf { Closure self ->
                    def entries = ls('.')
                    entries.findAll { !it.attrs.dir }.each { child ->
                        def localFile = receiver.createFile(child.filename)
                        if (localFile) {
                            getFile(child.filename, localFile.path)
                            log.info("Received file from $operations.remote.name: $child.filename -> $localFile")
                        }
                    }
                    entries.findAll { it.attrs.dir && !(it.filename in ['.', '..']) }.each { child ->
                        log.debug("Entering directory on $remote.name: $child.filename")
                        cd(child.filename)
                        receiver.enterDirectory(child.filename)
                        self()
                        log.debug("Leaving directory on $remote.name: $child.filename")
                        cd('..')
                        receiver.leaveDirectory()
                    }
                }()

                log.info("Received directory from $operations.remote.name: $remotePath -> $receiver.destination")
            } else {
                getFile(remotePath, receiver.destination.path)
                log.info("Received file from $operations.remote.name: $remotePath -> $receiver.destination")
            }
        }
    }

}
