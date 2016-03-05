package org.hidetake.groovy.ssh.extension

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.session.SessionExtension

import static org.hidetake.groovy.ssh.util.Utility.currySelf

/**
 * An extension class to remove a file or directory via SFTP.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait SftpRemove implements SessionExtension {
    void remove(String... paths) {
        sftp {
            paths.each { path ->
                if (!stat(path).dir) {
                    rm(path)
                    log.info("Removed file on $remote.name: $path")
                } else {
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
                }
            }
        }
    }
}
