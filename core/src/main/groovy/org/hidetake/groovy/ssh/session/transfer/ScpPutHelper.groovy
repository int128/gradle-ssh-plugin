package org.hidetake.groovy.ssh.session.transfer

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.interaction.InteractionHandler
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.session.BadExitStatusException
import org.hidetake.groovy.ssh.util.FileTransferProgress

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * A helper class for SCP put operation.
 *
 * See also <a href="https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works">How the SCP protocol works</a>.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class ScpPutHelper {

    static class EnterDirectory {
        final String name

        def EnterDirectory(String name1) {
            name = name1
        }
    }

    @Singleton
    static class LeaveDirectory {
    }

    private final Operations operations
    private final CompositeSettings mergedSettings
    private final String remoteBase

    /**
     * A list of instructions for the SCP command.
     * This should contain {@link EnterDirectory}, {@link File} or {@link LeaveDirectory}.
     */
    private final instructions = new ArrayDeque()

    def ScpPutHelper(Operations operations1, CompositeSettings mergedSettings1, String remoteBase1) {
        operations = operations1
        mergedSettings = mergedSettings1
        remoteBase = remoteBase1
    }

    /**
     * Add an instruction to the batch.
     *
     * @param localFiles
     */
    void add(Iterable<File> localFiles) {
        localFiles.findAll { !it.directory }.each { localFile ->
            instructions.add(localFile)
        }

        localFiles.findAll { it.directory }.each { localDir ->
            instructions.add(new EnterDirectory(localDir.name))
            add(localDir.listFiles().toList())
            instructions.add(LeaveDirectory.instance)
        }
    }

    /**
     * Execute batch instructions.
     */
    void execute() {
        log.debug("Requesting SCP command on $operations.remote.name: $remoteBase")
        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(logging: LoggingMethod.none))
        def operation = operations.command(settings, "scp -t -r $remoteBase")
        operation.addInteraction(processInteraction)

        int exitStatus = operation.startSync()
        if (exitStatus == 0) {
            log.debug("Success SCP command on $operations.remote.name: $remoteBase")
        } else {
            log.error("Failed SCP command on $operations.remote.name: $remoteBase")
            throw new BadExitStatusException("SCP command returned exit status $exitStatus", exitStatus)
        }
    }

    /**
     * Execute a single instruction.
     *
     * @param remoteFilename
     * @param bytes
     */
    void executeSingle(String remoteFilename, byte[] bytes) {
        log.debug("Requesting SCP command on $operations.remote.name: $remoteBase")
        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(logging: LoggingMethod.none))
        def operation = operations.command(settings, "scp -t $remoteBase")
        operation.addInteraction {
            when(partial: '\0', from: standardOutput) {
                log.trace("Got NULL from $operations.remote.name in executeSingle#1")

                def instruction = "C0644 $bytes.length $remoteFilename"
                log.trace("Sending SCP instruction to $operations.remote.name: $instruction")
                standardInput << instruction << '\n'
                standardInput.flush()

                when(partial: '\0', from: standardOutput) {
                    log.trace("Got NULL from $operations.remote.name in executeSingle#2")
                    log.debug("Sending $bytes.length bytes to $operations.remote.name: $remoteFilename")
                    standardInput << bytes
                    standardInput.write(0)
                    standardInput.flush()

                    when(partial: '\0', from: standardOutput) {
                        log.trace("Got NULL from $operations.remote.name in executeSingle#3")
                        callWithDelegate(finishCommand, delegate)
                    }
                    when(line: _) { String line ->
                        log.error("Failed SCP on $operations.remote.name: $remoteFilename")
                        throw new IllegalStateException("SCP command returned error: $line")
                    }
                }
                when(line: _) { String line ->
                    log.error("Failed SCP on $operations.remote.name: $remoteFilename")
                    throw new IllegalStateException("SCP command returned error: $line")
                }
            }
            when(line: _) { String line ->
                log.error("Failed SCP on $operations.remote.name")
                throw new IllegalStateException("SCP command returned error: $line")
            }
        }

        int exitStatus = operation.startSync()
        if (exitStatus == 0) {
            log.debug("Success SCP command on $operations.remote.name: $remoteBase")
        } else {
            log.error("Failed SCP command on $operations.remote.name: $remoteBase")
            throw new BadExitStatusException("SCP command returned exit status $exitStatus", exitStatus)
        }
    }

    private final processInteraction = closureForInteractionHandler {
        when(partial: '\0', from: standardOutput) {
            log.trace("Got NULL from $operations.remote.name in processInteraction")
            callWithDelegate(processNextInstruction, delegate)
        }
        when(line: _) { String line ->
            log.error("Failed SCP on $operations.remote.name")
            throw new IllegalStateException("SCP command returned error: $line")
        }
    }

    private final processNextInstruction = closureForInteractionHandler {
        def instruction = instructions.poll()
        log.trace("Processing instruction: $instruction")
        switch (instruction) {
            case File:              return callWithDelegate(createFile.curry(instruction as File), delegate)
            case EnterDirectory:    return callWithDelegate(enterDirectory.curry(instruction as EnterDirectory), delegate)
            case LeaveDirectory:    return callWithDelegate(leaveDirectory, delegate)
            case null:              return callWithDelegate(finishCommand, delegate)
            default:                throw new IllegalStateException("instruction should be File, EnterDirectory or LeaveDirectory: $instruction")
        }
    }

    private final createFile = closureForInteractionHandler { File file ->
        def size = file.length()
        def instruction = "C0644 $size $file.name"

        log.trace("Sending SCP instruction to $operations.remote.name: $instruction")
        standardInput << instruction << '\n'
        standardInput.flush()

        when(partial: '\0', from: standardOutput) {
            log.trace("Got NULL from $operations.remote.name in createFile#1")
            log.debug("Sending $size bytes to $operations.remote.name: $file.name")
            file.withInputStream { stream ->
                def progress = new FileTransferProgress(size, { percent ->
                    log.info("Sending $percent to $operations.remote.name: $file.name")
                })
                def readBuffer = new byte[1024 * 1024]
                while (true) {
                    def readLength = stream.read(readBuffer)
                    if (readLength < 0) {
                        break
                    }
                    standardInput.write(readBuffer, 0, readLength)
                    progress.report(readLength)
                }

                standardInput.write(0)
                standardInput.flush()
            }

            when(partial: '\0', from: standardOutput) {
                log.trace("Got NULL from $operations.remote.name in createFile#2")
                callWithDelegate(processNextInstruction, delegate)
            }
            when(line: _) { String line ->
                log.error("Failed SCP on $operations.remote.name: $file.name")
                throw new IllegalStateException("SCP command returned error: $line")
            }
        }
        when(line: _) { String line ->
            log.error("Failed SCP on $operations.remote.name: $file.name")
            throw new IllegalStateException("SCP command returned error: $line")
        }
    }

    private final enterDirectory = closureForInteractionHandler { EnterDirectory enterDirectory ->
        def instruction = "D0755 0 $enterDirectory.name"
        log.trace("Entering directory on $operations.remote.name: $instruction")
        standardInput << instruction << '\n'
        standardInput.flush()

        when(partial: '\0', from: standardOutput) {
            log.trace("Got NULL from $operations.remote.name in enterDirectory")
            callWithDelegate(processNextInstruction, delegate)
        }
        when(line: _) { String line ->
            log.error("Failed SCP on $operations.remote.name: $enterDirectory")
            throw new IllegalStateException("SCP command returned error: $line")
        }
    }

    private final leaveDirectory = closureForInteractionHandler {
        log.debug("Leaving directory on $operations.remote.name")
        standardInput << 'E' << '\n'
        standardInput.flush()

        when(partial: '\0', from: standardOutput) {
            log.trace("Got NULL from $operations.remote.name in leaveDirectory")
            callWithDelegate(processNextInstruction, delegate)
        }
        when(line: _) { String line ->
            log.error("Failed SCP on $operations.remote.name")
            throw new IllegalStateException("SCP command returned error: $line")
        }
    }

    private final finishCommand = closureForInteractionHandler {
        log.debug("Sending E to $operations.remote.name")
        standardInput << 'E' << '\n'
        standardInput.flush()

        when(partial: '\0', from: standardOutput) {
            log.trace("Got NULL from $operations.remote.name in finishCommand")
        }
        when(line: _) { String line ->
            log.error("Failed SCP on $operations.remote.name")
            throw new IllegalStateException("SCP command returned error: $line")
        }
    }

    private static <T> Closure<T> closureForInteractionHandler(@DelegatesTo(InteractionHandler) Closure<T> closure) {
        closure
    }

}
