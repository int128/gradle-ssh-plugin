package org.hidetake.groovy.ssh.extension.helper

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.session.BadExitStatusException

import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Matcher

/**
 * A helper class for SCP get operation.
 *
 * See also <a href="https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works">How the SCP protocol works</a>.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class ScpGetHelper {
    final Operations operations
    final CompositeSettings mergedSettings

    def ScpGetHelper(Operations operations1, CompositeSettings mergedSettings1) {
        operations = operations1
        mergedSettings = mergedSettings1
    }

    /**
     * Get remote files into local directory recursively.
     *
     * @param remotePath
     * @param localDir
     */
    void get(String remotePath, File localDir) {
        listDirectoryEntries(remotePath, true, new ScpGetCallback<File>() {
            final localDirStack = new ArrayDeque<File>([localDir])

            def getCurrentLocalDir() {
                localDirStack.peek()
            }

            @Override
            File foundFile(String name, long size, int mode) {
                def localFile = new File(currentLocalDir, name)
                localFile.delete()
                localFile
            }

            @Override
            void receivedFileContent(byte[] bytes, File context) {
                context.append(bytes)
                log.trace("Wrote $bytes.length bytes into $context.path")
            }

            @Override
            void enterDirectory(String name, int mode) {
                localDirStack.push(new File(currentLocalDir, name))
                currentLocalDir.mkdir()
                log.trace("Created local directory: $currentLocalDir.path")
            }

            @Override
            void leaveDirectory() {
                localDirStack.pop()
            }
        })
    }

    /**
     * Get content of remote file.
     *
     * @param remotePath
     * @param stream
     */
    void getFileContent(String remotePath, OutputStream stream) {
        listDirectoryEntries(remotePath, false, new ScpGetCallback() {
            @Override
            void receivedFileContent(byte[] bytes, def context) {
                stream.write(bytes)
            }
        })
    }

    void listDirectoryEntries(String remotePath, boolean recursive, ScpGetCallback callback) {
        log.debug("Requesting SCP GET: $operations.remote.name:$remotePath")

        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(logging: LoggingMethod.none))
        def flag = recursive ? '-fr' : '-f'
        def command = operations.command(settings, "scp $flag $remotePath")
        command.addInteraction {
            log.trace("Sending NULL to $operations.remote.name")
            standardInput.write(0)
            standardInput.flush()

            when(line: ~/C(\d+) (\d+) (.+)/, from: standardOutput) { Matcher m ->
                def mode = m.group(1) as int
                def size = m.group(2) as long
                def path = m.group(3)

                log.debug("Found file with $size bytes on $operations.remote.name: $path")
                def localFile = callback.foundFile(path, size, mode)

                log.trace("Sending NULL to $operations.remote.name")
                standardInput.write(0)
                standardInput.flush()

                final remaining = new AtomicLong(size)
                when(bytes: remaining, from: standardOutput) { byte[] b ->
                    log.trace("Received $b.length bytes from $operations.remote.name: $path")
                    callback.receivedFileContent(b, localFile)
                    remaining.addAndGet(-b.length)
                    log.trace("Remaining $remaining bytes to transfer: $path")
                }

                when(partial: '\0', from: standardOutput) {
                    log.trace("Got NULL from $operations.remote.name: $path")

                    log.trace("Sending NULL to $operations.remote.name")
                    standardInput.write(0)
                    standardInput.flush()

                    popContext()
                }
            }

            when(line: ~/D(\d+) \d+ (.+)/, from: standardOutput) { Matcher m ->
                def mode = m.group(1) as int
                def path = m.group(2)

                log.debug("Entering directory on $operations.remote.name: $path")
                callback.enterDirectory(path, mode)

                log.trace("Sending NULL to $operations.remote.name")
                standardInput.write(0)
                standardInput.flush()
            }

            when(line: 'E', from: standardOutput) {
                log.debug("Leaving directory on $operations.remote.name")
                callback.leaveDirectory()

                log.trace("Sending NULL to $operations.remote.name")
                standardInput.write(0)
                standardInput.flush()
            }

            when(line: _) { String line ->
                log.error("Failed SCP GET: $operations.remote.name:$remotePath")
                throw new IllegalStateException("SCP command returned error: $line")
            }
        }

        int exitStatus = command.startSync()
        if (exitStatus == 0) {
            log.debug("Success SCP GET: $operations.remote.name:$remotePath")
        } else {
            log.error("Failed SCP GET: $operations.remote.name:$remotePath")
            throw new BadExitStatusException("SCP command returned exit status $exitStatus", exitStatus)
        }
    }
}
