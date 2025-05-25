package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.SftpError
import org.hidetake.groovy.ssh.operation.SftpException
import org.hidetake.groovy.ssh.session.SessionExtension

import static org.hidetake.groovy.ssh.util.Utility.currySelf

/**
 * An extension class to remove a file or directory via SFTP.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait SftpRemove implements SessionExtension {

    /**
     * Remove files or directories.
     * This method silently ignores non-existing files or directories but
     * throws an exception if any error occurs.
     *
     * @param paths files or directories
     * @return true if anything got removed, false if nothing done
     */
    boolean remove(String... paths) {
        sftp {
            paths.collect { remotePath ->
                def remoteAttrs = Helper.nullIfNoSuchFile { stat(remotePath) }
                if (remoteAttrs == null) {
                    false
                } else if (remoteAttrs.dir) {
                    log.debug("Entering directory on $remote.name: $remotePath")
                    cd(remotePath)

                    currySelf { Closure self ->
                        def entries = ls('.')
                        entries.findAll { !it.attrs.dir }.each { child ->
                            log.debug("Removing file on $remote.name: $child.filename")
                            rm(child.filename)
                        }
                        entries.findAll { it.attrs.dir && !(it.filename in ['.', '..']) }.each { child ->
                            log.debug("Entering directory on $remote.name: $child.filename")
                            cd(child.filename)
                            self()
                            log.debug("Leaving directory on $remote.name: $child.filename")
                            cd('..')
                            log.debug("Removing directory on $remote.name: $child.filename")
                            rmdir(child.filename)
                        }
                    }()

                    log.debug("Leaving directory on $remote.name: $remotePath")
                    cd('..')
                    log.debug("Removing directory on $remote.name: $remotePath")
                    rmdir(remotePath)
                    log.info("Removed directory on $remote.name: $remotePath")
                    true
                } else {
                    rm(remotePath)
                    log.info("Removed file on $remote.name: $remotePath")
                    true
                }
            }.any()
        }
    }

    private static class Helper {
        static <T> T nullIfNoSuchFile(Closure<T> closure) {
            try {
                closure()
            } catch (SftpException e) {
                if (e.error == SftpError.SSH_FX_NO_SUCH_FILE) {
                    null
                } else {
                    throw e
                }
            }
        }
    }

}
