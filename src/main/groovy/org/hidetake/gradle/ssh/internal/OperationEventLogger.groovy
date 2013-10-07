package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import groovy.transform.TupleConstructor
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.hidetake.gradle.ssh.api.OperationEventListener
import org.hidetake.gradle.ssh.api.SessionSpec

/**
 * Handler for logging operation events.
 *
 * @author hidetake.org
 *
 */
@TupleConstructor
class OperationEventLogger implements OperationEventListener {
    final Logger logger
    final LogLevel logLevel

    @Override
    void beginOperation(String operation, Object... args) {
        log { "Begin an operation ${operation}(${(args*.toString()).join(', ')})" }
    }

    @Override
    void unmanagedChannelConnected(Channel channel, SessionSpec spec) {
        log { "Channel ${channel.id} has been opened (unmanaged)" }
    }

    @Override
    void managedChannelConnected(Channel channel, SessionSpec spec) {
        log { "Channel #${channel.id} has been opened" }
    }

    @Override
    void managedChannelClosed(Channel channel, SessionSpec spec) {
        log { "Channel #${channel.id} has been closed at status ${channel.exitStatus}" }
    }

    protected void log(Closure message) {
        if (logger.isEnabled(logLevel)) {
            logger.log(logLevel, message() as String)
        }
    }
}
