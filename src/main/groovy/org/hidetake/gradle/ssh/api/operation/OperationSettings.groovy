package org.hidetake.gradle.ssh.api.operation

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.Settings

@EqualsAndHashCode
@ToString
class OperationSettings extends Settings<OperationSettings> {
    /**
     * Dry-run flag.
     * If <code>true</code>, performs no action.
     */
    Boolean dryRun

    /**
     * Log level for standard output of commands.
     */
    LogLevel outputLogLevel

    /**
     * Log level for standard error of commands.
     */
    LogLevel errorLogLevel

    /**
     * Encoding of input and output stream.
     */
    String encoding

    static final DEFAULT = new OperationSettings(
            dryRun: false,
            outputLogLevel: LogLevel.QUIET,
            errorLogLevel: LogLevel.ERROR,
            encoding: 'UTF-8'
    )

    OperationSettings plus(OperationSettings right) {
        new OperationSettings(
                dryRun:         findNotNull(right.dryRun, dryRun),
                outputLogLevel: findNotNull(right.outputLogLevel, outputLogLevel),
                errorLogLevel:  findNotNull(right.errorLogLevel, errorLogLevel),
                encoding:       findNotNull(right.encoding, encoding)
        )
    }
}
