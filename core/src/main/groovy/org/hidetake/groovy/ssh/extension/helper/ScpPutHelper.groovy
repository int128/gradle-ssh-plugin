package org.hidetake.groovy.ssh.extension.helper

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.session.BadExitStatusException

/**
 * A helper class for SCP put operation.
 *
 * See also <a href="https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works">How the SCP protocol works</a>.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class ScpPutHelper {
    final Operations operations
    final CompositeSettings mergedSettings

    def ScpPutHelper(Operations operations1, CompositeSettings mergedSettings1) {
        operations = operations1
        mergedSettings = mergedSettings1
    }

    /**
     * Put local files into remote path recursively.
     *
     * @param localFiles
     * @param remotePath
     */
    void put(Iterable<File> localFiles, String remotePath) {
        localFiles.findAll { !it.directory }.each { localFile ->
            localFile.withInputStream { stream ->
                createFile(remotePath, localFile.name, stream, localFile.length())
            }
        }

        localFiles.findAll { it.directory }.each { localDir ->
            log.debug("Entering directory $localDir.path")
            createDirectory(remotePath, localDir.name)

            def remoteDir = "$remotePath/$localDir.name"
            put(localDir.listFiles().toList(), remoteDir)
            log.debug("Leaving directory $localDir.path")
        }
    }

    /**
     * Create a file via SCP.
     *
     * @param remoteDir
     * @param remoteFile
     * @param stream should be closed by caller
     * @param size
     */
    void createFile(String remoteDir, String remoteFile, InputStream stream, long size) {
        log.debug("Requesting SCP CREATE FILE: $operations.remote.name:$remoteDir/$remoteFile")
        def instruction = "C0644 $size $remoteFile"

        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(logging: LoggingMethod.none))
        def operation = operations.command(settings, "scp -t $remoteDir")
        operation.addInteraction {
            when(partial: '\0', from: standardOutput) {
                log.debug("[1] Sending SCP instruction to $operations.remote.name: $instruction")
                standardInput << instruction << '\n'
                standardInput.flush()

                when(partial: '\0', from: standardOutput) {
                    log.debug("[2] Sending $size bytes to $operations.remote.name")
                    standardInput << stream
                    standardInput.write(0)
                    standardInput.flush()

                    when(partial: '\0', from: standardOutput) {
                        log.debug("[3] Sending E to $operations.remote.name")
                        standardInput << 'E' << '\n'
                        standardInput.flush()

                        when(partial: '\0', from: standardOutput) {
                        }
                        when(line: _) { String line ->
                            log.error("Failed SCP GET: $operations.remote.name:$remoteFile")
                            throw new IllegalStateException("SCP command returned error: $line")
                        }
                    }
                    when(line: _) { String line ->
                        log.error("Failed SCP GET: $operations.remote.name:$remoteFile")
                        throw new IllegalStateException("SCP command returned error: $line")
                    }
                }
                when(line: _) { String line ->
                    log.error("Failed SCP GET: $operations.remote.name:$remoteFile")
                    throw new IllegalStateException("SCP command returned error: $line")
                }
            }
            when(line: _) { String line ->
                log.error("Failed SCP GET: $operations.remote.name:$remoteFile")
                throw new IllegalStateException("SCP command returned error: $line")
            }
        }

        int exitStatus = operation.startSync()
        if (exitStatus == 0) {
            log.debug("Success SCP CREATE FILE: $operations.remote.name:$remoteDir/$remoteFile")
        } else {
            log.error("Failed SCP CREATE FILE: $operations.remote.name:$remoteDir/$remoteFile")
            throw new BadExitStatusException("SCP command returned exit status $exitStatus", exitStatus)
        }
    }

    /**
     * Create a directory via SCP.
     *
     * @param remoteBase
     * @param remoteDir
     */
    void createDirectory(String remoteBase, String remoteDir) {
        log.debug("Requesting SCP CREATE DIRECTORY: $operations.remote.name:$remoteDir")
        def instruction = "D0755 0 $remoteDir"

        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(logging: LoggingMethod.none))
        def command = operations.command(settings, "scp -t -r $remoteBase")
        command.addInteraction {
            when(partial: '\0', from: standardOutput) {
                log.debug("[1] Sending SCP instruction to $operations.remote.name: $instruction")
                standardInput << instruction << '\n'
                standardInput.flush()

                when(partial: '\0', from: standardOutput) {
                    log.debug("[2] Sending E to $operations.remote.name")
                    standardInput << 'E' << '\n'
                    standardInput.flush()
                }
                when(line: _) { String line ->
                    log.error("Failed SCP GET: $operations.remote.name:$remoteDir")
                    throw new IllegalStateException("SCP command returned error: $line")
                }
            }
            when(line: _) { String line ->
                log.error("Failed SCP GET: $operations.remote.name:$remoteDir")
                throw new IllegalStateException("SCP command returned error: $line")
            }
        }

        int exitStatus = command.startSync()
        if (exitStatus == 0) {
            log.debug("Success SCP CREATE DIRECTORY: $operations.remote.name:$remoteDir")
        } else {
            log.error("Failed SCP CREATE DIRECTORY: $operations.remote.name:$remoteDir")
            throw new BadExitStatusException("SCP command returned exit status $exitStatus", exitStatus)
        }
    }
}
