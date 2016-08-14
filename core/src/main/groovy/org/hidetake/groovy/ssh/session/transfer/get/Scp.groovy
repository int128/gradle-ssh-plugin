package org.hidetake.groovy.ssh.session.transfer.get

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.util.FileTransferProgress

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
class Scp implements Provider {

    final Operations operations
    final CompositeSettings mergedSettings

    def Scp(Operations operations1, CompositeSettings mergedSettings1) {
        operations = operations1
        mergedSettings = mergedSettings1
    }

    void get(String remotePath, RecursiveReceiver receiver) {
        log.debug("Requesting SCP GET: $operations.remote.name:$remotePath")

        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(logging: LoggingMethod.none))
        def command = operations.command(settings, "scp -f -r $remotePath")
        command.addInteraction {
            sendNull(standardInput)

            when(line: ~/C(\d+) (\d+) (.+)/, from: standardOutput) { Matcher m ->
                def size = m.group(2) as long
                def filename = m.group(3)

                log.debug("Found file with $size bytes on $operations.remote.name: $filename")
                def localFile = receiver.createFile(filename)
                sendNull(standardInput)

                def remaining = new Remaining(size, operations.remote, filename)
                when(bytes: remaining.counter, from: standardOutput) { byte[] bytes ->
                    log.trace("Received $bytes.length bytes from $operations.remote.name: $filename")
                    localFile?.append(bytes)
                    remaining.burn(bytes.length)
                }

                when(partial: '\0', from: standardOutput) {
                    log.trace("Got NULL from $operations.remote.name: $filename")
                    if (localFile) {
                        log.info("Received file from $operations.remote.name: $filename -> $localFile")
                    }
                    sendNull(standardInput)
                    popContext()
                }
            }

            when(line: ~/D(\d+) \d+ (.+)/, from: standardOutput) { Matcher m ->
                def dirname = m.group(2)
                log.debug("Entering directory on $operations.remote.name: $dirname")
                receiver.enterDirectory(dirname)
                sendNull(standardInput)
            }

            when(line: 'E', from: standardOutput) {
                log.debug("Leaving directory on $operations.remote.name")
                receiver.leaveDirectory()
                sendNull(standardInput)
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

    void get(String remotePath, FileReceiver receiver) {
        get(remotePath, receiver as WritableReceiver)
    }

    void get(String remotePath, StreamReceiver receiver) {
        get(remotePath, receiver as WritableReceiver)
    }

    void get(String remoteFile, WritableReceiver receiver) {
        log.debug("Requesting SCP GET: $operations.remote.name:$remoteFile")

        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(logging: LoggingMethod.none))
        def command = operations.command(settings, "scp -f $remoteFile")
        command.addInteraction {
            sendNull(standardInput)

            when(line: ~/C(\d+) (\d+) (.+)/, from: standardOutput) { Matcher m ->
                def size = m.group(2) as long
                def filename = m.group(3)

                log.debug("Found file with $size bytes on $operations.remote.name: $filename")
                sendNull(standardInput)

                def remaining = new Remaining(size, operations.remote, filename)
                when(bytes: remaining.counter, from: standardOutput) { byte[] bytes ->
                    log.trace("Received $bytes.length bytes from $operations.remote.name: $filename")
                    receiver.write(bytes)
                    remaining.burn(bytes.length)
                }

                when(partial: '\0', from: standardOutput) {
                    log.trace("Got NULL from $operations.remote.name: $filename")
                    log.info("Received file from $operations.remote.name: $remoteFile")
                    sendNull(standardInput)
                    popContext()
                }
            }

            when(line: _) { String line ->
                log.error("Failed SCP GET: $operations.remote.name:$remoteFile")
                throw new IllegalStateException("SCP command returned error: $line")
            }
        }

        int exitStatus = command.startSync()
        if (exitStatus == 0) {
            log.debug("Success SCP GET: $operations.remote.name:$remoteFile")
        } else {
            log.error("Failed SCP GET: $operations.remote.name:$remoteFile")
            throw new BadExitStatusException("SCP command returned exit status $exitStatus", exitStatus)
        }
    }

    private void sendNull(OutputStream standardInput) {
        log.trace("Sending NULL to $operations.remote.name")
        standardInput.write(0)
        standardInput.flush()
    }

    /**
     * A helper class to manage remaining of file transfer.
     */
    private static class Remaining {
        final AtomicLong counter

        private final progress
        private final remote
        private final path

        def Remaining(long size, Remote remote1, String path1) {
            remote = remote1
            path = path1
            counter = new AtomicLong(size)
            progress = new FileTransferProgress(size, { percent ->
                log.info("Receiving $percent from $remote.name: $path")
            })
        }

        def burn(int size) {
            counter.addAndGet(-size)
            progress.report(size)
            log.trace("Remaining $counter bytes to transfer: $path")
        }
    }

}
