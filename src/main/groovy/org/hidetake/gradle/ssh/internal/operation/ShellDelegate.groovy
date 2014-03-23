package org.hidetake.gradle.ssh.internal.operation

import com.jcraft.jsch.ChannelShell
import groovy.transform.TupleConstructor
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.hidetake.gradle.ssh.api.operation.ShellHandler
import org.hidetake.gradle.ssh.api.operation.interaction.Stream
import org.hidetake.gradle.ssh.internal.operation.interaction.Engine
import org.hidetake.gradle.ssh.internal.operation.interaction.InteractionDelegate
import org.hidetake.gradle.ssh.internal.operation.interaction.LineOutputStream

@TupleConstructor
class ShellDelegate implements ShellHandler {
    final ChannelShell channel
    final OutputStream standardInput
    final LineOutputStream standardOutput

    private static final logger = Logging.getLogger(ShellDelegate)

    /**
     * Create an instance for the channel.
     *
     * @param channel the channel
     * @param charset character set for streams
     * @return an instance
     */
    static create(ChannelShell channel, String charset) {
        def standardOutputStream = new LineOutputStream(charset)
        channel.outputStream = standardOutputStream
        new ShellDelegate(channel, channel.outputStream, standardOutputStream)
    }

    void enableLogging(LogLevel standardOutputLevel) {
        standardOutput.loggingListeners.add { String message -> logger.log(standardOutputLevel, message) }
    }

    @Override
    void interaction(Closure closure) {
        def delegate = new InteractionDelegate(standardInput)
        def rules = delegate.evaluate(closure)
        def engine = new Engine(delegate)

        engine.alterInteractionRules(rules)
        engine.attach(standardOutput, Stream.StandardOutput)
    }
}
