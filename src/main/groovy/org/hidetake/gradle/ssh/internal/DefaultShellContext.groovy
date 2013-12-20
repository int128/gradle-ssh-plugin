package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.ChannelShell
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.hidetake.gradle.ssh.api.CommandContext
import org.hidetake.gradle.ssh.api.command.Stream
import org.hidetake.gradle.ssh.internal.command.Engine
import org.hidetake.gradle.ssh.internal.command.InteractionDelegate
import org.hidetake.gradle.ssh.internal.command.LineOutputStream

@TupleConstructor
@Slf4j
class DefaultShellContext implements CommandContext, ChannelObservable {
    final ChannelShell channel
    final OutputStream standardInput
    final LineOutputStream standardOutput

    private static final logger = Logging.getLogger(DefaultShellContext)

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
        new DefaultShellContext(channel, channel.outputStream, standardOutputStream)
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
