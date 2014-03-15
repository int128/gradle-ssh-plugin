package org.hidetake.gradle.ssh.internal.operation

import com.jcraft.jsch.ChannelExec
import groovy.transform.TupleConstructor
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.hidetake.gradle.ssh.api.interaction.Stream
import org.hidetake.gradle.ssh.api.operation.ExecutionHandler
import org.hidetake.gradle.ssh.internal.interaction.Engine
import org.hidetake.gradle.ssh.internal.interaction.InteractionDelegate
import org.hidetake.gradle.ssh.internal.interaction.LineOutputStream

@TupleConstructor
class ExecutionDelegate implements ExecutionHandler {
    final ChannelExec channel
    final OutputStream standardInput
    final LineOutputStream standardOutput
    final LineOutputStream standardError

    private static final logger = Logging.getLogger(ExecutionDelegate)

    /**
     * Create an instance for the channel.
     *
     * @param channel the channel
     * @param charset character set for streams
     * @return an instance
     */
    static create(ChannelExec channel, String charset) {
        def standardOutputStream = new LineOutputStream(charset)
        def standardErrorStream = new LineOutputStream(charset)
        channel.outputStream = standardOutputStream
        channel.errStream = standardErrorStream
        new ExecutionDelegate(channel, channel.outputStream, standardOutputStream, standardErrorStream)
    }

    void enableLogging(LogLevel standardOutputLevel, LogLevel standardErrorLevel) {
        standardOutput.loggingListeners.add { String message -> logger.log(standardOutputLevel, message) }
        standardError.loggingListeners.add { String message -> logger.log(standardErrorLevel, message) }
    }

    @Override
    void interaction(Closure closure) {
        def delegate = new InteractionDelegate(standardInput)
        def rules = delegate.evaluate(closure)
        def engine = new Engine(delegate)

        engine.alterInteractionRules(rules)
        engine.attach(standardOutput, Stream.StandardOutput)
        engine.attach(standardError, Stream.StandardError)
    }
}
