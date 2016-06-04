package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.operation.SftpNoSuchFileException
import org.hidetake.groovy.ssh.session.SessionExtension

import static org.hidetake.groovy.ssh.util.Utility.currySelf

/**
 * An extension class to remove a file or directory via SFTP.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait SftpRemove implements SessionExtension {
    private static enum PathType {
        File, Directory, NotFound
    }

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
            paths.collect { path ->
                PathType pathType
                try {
                    pathType = stat(path).dir ? PathType.Directory : PathType.File
                } catch (SftpNoSuchFileException ignore) {
                    pathType = PathType.NotFound
                }

                switch (pathType) {
                    case PathType.File:
                        rm(path)
                        log.info("Removed file on $remote.name: $path")
                        break

                    case PathType.Directory:
                        currySelf { Closure self, String directory ->
                            log.debug("Entering directory on $remote.name: $directory")
                            ls(directory).each { child ->
                                def fullPath = "$directory/$child.filename"
                                if (!child.attrs.dir) {
                                    rm(fullPath)
                                } else if (child.filename in ['.', '..']) {
                                    // ignore directory entries
                                } else {
                                    self.call(self, fullPath)
                                }
                            }
                            rmdir(directory)
                            log.debug("Leaving directory on $remote.name: $directory")
                        }.call(path)
                        log.info("Removed directory on $remote.name: $path")
                        break

                    case PathType.NotFound:
                        log.warn("No such file or directory on $remote.name: $path")
                        break
                }
                pathType
            }.any { it in [PathType.File, PathType.Directory] }
        }
    }
}
