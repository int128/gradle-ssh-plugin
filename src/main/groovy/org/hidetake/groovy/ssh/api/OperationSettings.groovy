package org.hidetake.groovy.ssh.api

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.gradle.api.logging.LogLevel
import org.hidetake.groovy.ssh.extension.SftpGet
import org.hidetake.groovy.ssh.extension.SftpPut
import org.hidetake.groovy.ssh.extension.SudoExecution

@EqualsAndHashCode
@ToString
class OperationSettings extends Settings<OperationSettings> {
    /**
     * Dry-run flag.
     * If <code>true</code>, performs no action.
     */
    Boolean dryRun

    /**
     * PTY allocation flag.
     * If <code>true</code>, PTY will be allocated on command execution.
     */
    Boolean pty

    /**
     * Logging flag.
     * If <code>false</code>, performs no logging.
     */
    Boolean logging

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

    /**
     * Stream interaction.
     * @see org.hidetake.groovy.ssh.api.interaction.InteractionHandler
     */
    Closure interaction

    /**
     * Extension classes to mixin to {@link org.hidetake.groovy.ssh.api.session.SessionHandler}.
     */
    List<Class> extensions = []

    static final DEFAULT = new OperationSettings(
            dryRun: false,
            pty: false,
            logging: true,
            outputLogLevel: LogLevel.QUIET,
            errorLogLevel: LogLevel.ERROR,
            encoding: 'UTF-8',
            extensions: [SudoExecution, SftpGet, SftpPut]
    )

    OperationSettings plus(OperationSettings right) {
        new OperationSettings(
                dryRun:         findNotNull(right.dryRun, dryRun),
                pty:            findNotNull(right.pty, pty),
                logging:        findNotNull(right.logging, logging),
                outputLogLevel: findNotNull(right.outputLogLevel, outputLogLevel),
                errorLogLevel:  findNotNull(right.errorLogLevel, errorLogLevel),
                encoding:       findNotNull(right.encoding, encoding),
                interaction:    findNotNull(right.interaction, interaction),
                extensions:     extensions + right.extensions
        )
    }
}
