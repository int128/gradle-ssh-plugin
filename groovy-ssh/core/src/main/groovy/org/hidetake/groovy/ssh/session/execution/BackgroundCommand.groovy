package org.hidetake.groovy.ssh.session.execution

import groovy.util.logging.Slf4j

/**
 * Provides the non-blocking command execution.
 * Each method returns immediately and executes the commandLine concurrently.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait BackgroundCommand implements Command {
    @Deprecated
    void executeBackground(HashMap map = [:], String commandLine) {
        log.warn("Deprecated: executeBackground is no longer supported. Use execute instead.")
        execute(map, commandLine)
    }

    @Deprecated
    void executeBackground(HashMap map = [:], List<String> commandLineArgs) {
        log.warn("Deprecated: executeBackground is no longer supported. Use execute instead.")
        execute(map, commandLineArgs)
    }

    @Deprecated
    void executeBackground(HashMap map = [:], String commandLine, Closure callback) {
        log.warn("Deprecated: executeBackground is no longer supported. Use execute instead.")
        execute(map, commandLine, callback)
    }

    @Deprecated
    void executeBackground(HashMap map = [:], List<String> commandLineArgs, Closure callback) {
        log.warn("Deprecated: executeBackground is no longer supported. Use execute instead.")
        execute(map, commandLineArgs, callback)
    }
}
