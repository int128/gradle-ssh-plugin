package org.hidetake.groovy.ssh.operation

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.LoggingMethod
import org.hidetake.groovy.ssh.core.settings.SettingsHelper
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

/**
 * Settings for {@link Shell}.
 *
 * @author Hidetake Iwata
 */
trait ShellSettings {
    /**
     * Ignores the exit status of the command or shell.
     */
    Boolean ignoreError

    /**
     * PTY allocation flag.
     * If <code>true</code>, PTY will be allocated on command execution.
     */
    Boolean pty

    /**
     * Use agentForwarding flag.
     * If <code>true</code>, agent will be forwarded to remote host.
     */
    Boolean agentForwarding

    /**
     * A logging method of the remote command or shell.
     */
    LoggingMethod logging

    /**
     * An output stream to forward the standard output.
     */
    OutputStream outputStream

    /**
     * Encoding of input and output stream.
     */
    String encoding

    /**
     * Stream interaction.
     * @see org.hidetake.groovy.ssh.interaction.InteractionHandler
     */
    Closure interaction


    @EqualsAndHashCode
    static class With implements ShellSettings, ToStringProperties {
        def With() {}
        def With(ShellSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }

        static final ShellSettings DEFAULT = new ShellSettings.With(
                ignoreError: false,
                pty: false,
                agentForwarding: false,
                logging: LoggingMethod.slf4j,
                encoding: 'UTF-8',
        )
    }
}
