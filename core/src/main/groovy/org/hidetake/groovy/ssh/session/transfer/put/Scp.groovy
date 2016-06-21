package org.hidetake.groovy.ssh.session.transfer.put

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
 * Recursive SCP PUT executor.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Scp {

    private final Operations operations
    private final CompositeSettings mergedSettings

    def Scp(Operations operations1, CompositeSettings mergedSettings1) {
        operations = operations1
        mergedSettings = mergedSettings1
    }

    void put(Instructions instructions) {
        log.debug("Requesting SCP command on $operations.remote.name: $instructions.base")
        def settings = new CommandSettings.With(mergedSettings, new CommandSettings.With(logging: LoggingMethod.none))
        def flags = instructions.recursive ? '-tr' : '-t'
        def operation = operations.command(settings, "scp $flags $instructions.base")
        operation.addInteraction {
            when(partial: '\0', from: standardOutput) {
                log.trace("Got NULL from $operations.remote.name in processInteraction")
                callWithDelegate(processNextInstruction, delegate, instructions.iterator())
            }
            when(line: _) { String line ->
                log.error("Failed SCP on $operations.remote.name")
                throw new IllegalStateException("SCP command returned error: $line")
            }
        }

        int exitStatus = operation.startSync()
        if (exitStatus == 0) {
            log.debug("Success SCP command on $operations.remote.name: $instructions.base")
        } else {
            log.error("Failed SCP command on $operations.remote.name: $instructions.base")
            throw new BadExitStatusException("SCP command returned exit status $exitStatus", exitStatus)
        }
    }

    private final processNextInstruction = interactionClosure { Iterator iterator ->
        if (iterator.hasNext()) {
            def instruction = iterator.next()
            log.trace("Processing instruction: $instruction")
            switch (instruction) {
                case File:
                    callWithDelegate(createFile, delegate, iterator, instruction)
                    break
                case StreamContent:
                    callWithDelegate(createContent, delegate, iterator, instruction)
                    break
                case EnterDirectory:
                    callWithDelegate(enterDirectory, delegate, iterator, instruction)
                    break
                case LeaveDirectory:
                    callWithDelegate(leaveDirectory, delegate, iterator)
                    break
                default:
                    throw new IllegalStateException("Unknown instruction type: $instruction")
            }
        } else {
            callWithDelegate(finishCommand, delegate)
        }
    }

    private final createFile = interactionClosure { Iterator iterator, File file ->
        assert !file.directory, 'Do not call createFile with a directory'
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
                callWithDelegate(processNextInstruction, delegate, iterator)
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

    private final createContent = interactionClosure { Iterator iterator, StreamContent content ->
        def bytes = content.stream.bytes
        def size = bytes.length
        def instruction = "C0644 $size $content.name"

        log.trace("Sending SCP instruction to $operations.remote.name: $instruction")
        standardInput << instruction << '\n'
        standardInput.flush()

        when(partial: '\0', from: standardOutput) {
            log.trace("Got NULL from $operations.remote.name in createContent#1")
            log.debug("Sending $size bytes to $operations.remote.name: $content.name")
            standardInput << bytes
            standardInput.write(0)
            standardInput.flush()

            when(partial: '\0', from: standardOutput) {
                log.trace("Got NULL from $operations.remote.name in createContent#2")
                callWithDelegate(processNextInstruction, delegate, iterator)
            }
            when(line: _) { String line ->
                log.error("Failed SCP on $operations.remote.name: $content.name")
                throw new IllegalStateException("SCP command returned error: $line")
            }
        }
        when(line: _) { String line ->
            log.error("Failed SCP on $operations.remote.name: $content.name")
            throw new IllegalStateException("SCP command returned error: $line")
        }
    }

    private final enterDirectory = interactionClosure { Iterator iterator, EnterDirectory enterDirectory ->
        def instruction = "D0755 0 $enterDirectory.name"
        log.trace("Entering directory on $operations.remote.name: $instruction")
        standardInput << instruction << '\n'
        standardInput.flush()

        when(partial: '\0', from: standardOutput) {
            log.trace("Got NULL from $operations.remote.name in enterDirectory")
            callWithDelegate(processNextInstruction, delegate, iterator)
        }
        when(line: _) { String line ->
            log.error("Failed SCP on $operations.remote.name: $enterDirectory")
            throw new IllegalStateException("SCP command returned error: $line")
        }
    }

    private final leaveDirectory = interactionClosure { Iterator iterator ->
        log.debug("Leaving directory on $operations.remote.name")
        standardInput << 'E' << '\n'
        standardInput.flush()

        when(partial: '\0', from: standardOutput) {
            log.trace("Got NULL from $operations.remote.name in leaveDirectory")
            callWithDelegate(processNextInstruction, delegate, iterator)
        }
        when(line: _) { String line ->
            log.error("Failed SCP on $operations.remote.name")
            throw new IllegalStateException("SCP command returned error: $line")
        }
    }

    private final finishCommand = interactionClosure {
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

    private static <T> Closure<T> interactionClosure(@DelegatesTo(InteractionHandler) Closure<T> closure) {
        closure
    }

}
